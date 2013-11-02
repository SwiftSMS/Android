package com.icc.view;

import static com.icc.InternalString.ACTIVE_ACCOUNT;
import static com.icc.InternalString.CONTACT_SEPARATOR;
import static com.icc.InternalString.PREFS_KEY;
import static com.icc.InternalString.SMS_BODY;
import static com.icc.InternalString.SPACE;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.icc.R;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;
import com.icc.model.Account;
import com.icc.net.Operator;
import com.icc.net.OperatorFactory;
import com.icc.tasks.MaxCharacterCountTask;
import com.icc.tasks.RemainingSmsTask;
import com.icc.tasks.SendTask;

public class ComposeActivity extends Activity implements Observer, ActionBar.OnNavigationListener {

	private static final int ADD_FIRST_ACCOUNT_REQUEST = 23;

	private Operator operator;
	private CharacterCountTextWatcher charCountWatcher;
	private EditText messageEditText;
	private SharedPreferences preferences;
	private AutoCompleteTextView recipientEdittext;
	private MenuItem remaingSmsMenuItem;

	private ImageButton sendButton;

	private IAccountDatabase accountDatabase;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_compose);

		final ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
		this.accountDatabase = AccountDataSource.getInstance(this.getApplicationContext());
		this.messageEditText = (EditText) this.findViewById(R.id.text_compose_message);
		this.sendButton = (ImageButton) this.findViewById(R.id.button_compose_send);
		this.recipientEdittext = (AutoCompleteTextView) this.findViewById(R.id.text_compose_recipients);
		final TextView characterCountTextView = (TextView) this.findViewById(R.id.label_compose_character_count);
		this.charCountWatcher = new CharacterCountTextWatcher(characterCountTextView);
		final ContactSuggestionClickListener itemClickTextWatcher = new ContactSuggestionClickListener();

		this.sendButton.setEnabled(false);
		this.recipientEdittext.setAdapter(new ContactAdapter(this));
		this.recipientEdittext.setThreshold(1);
		itemClickTextWatcher.addObserver(this);
		this.recipientEdittext.addTextChangedListener(itemClickTextWatcher);
		this.recipientEdittext.setOnItemClickListener(itemClickTextWatcher);
		this.charCountWatcher.addObserver(this);
		this.messageEditText.addTextChangedListener(this.charCountWatcher);

		this.handleIntentData();
	}

	/**
	 * Handle the data passed to this {@link Activity} from it's {@link Intent}.
	 */
	private void handleIntentData() {
		final Uri intentData = this.getIntent().getData();
		if (intentData != null) {
			final String smsto = intentData.getSchemeSpecificPart();
			this.recipientEdittext.setText(smsto + CONTACT_SEPARATOR + SPACE);
			final String smsBody = this.getIntent().getStringExtra(SMS_BODY);
			if (smsBody != null) {
				this.messageEditText.setText(smsBody);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		final int accountId = this.preferences.getInt(ACTIVE_ACCOUNT, -1);
		if (accountId == -1) {
			this.startActivityForResult(new Intent(this, AddAccountActivity.class), ADD_FIRST_ACCOUNT_REQUEST);
		} else {
			if (this.operator == null || this.operator.getAccount().getId() != accountId) {
				final Account account = this.accountDatabase.getAccountById(accountId);
				this.operator = OperatorFactory.getOperator(account);
			}
			this.setupActionBar();
			this.retrieveRemainingSmsCount();
			this.getMaxCharacterCount();
		}
	}

	/**
	 * This method sets up the {@link ActionBar} dropdown list to contain all the accounts added to the App.
	 */
	private void setupActionBar() {
		// Set up the action bar to show a dropdown list.
		final int activeAccountId = this.preferences.getInt(ACTIVE_ACCOUNT, -1);
		final List<Account> accounts = this.accountDatabase.getAllAccounts();
		int activeAccountIndex = -1;
		for (int i = 0; i < accounts.size(); i++) {
			if (accounts.get(i).getId() == activeAccountId) {
				activeAccountIndex = i;
				break;
			}
		}
		final Context context = this.getActionBarThemedContextCompat();

		final AccountSpinnerAdapter adapter = new AccountSpinnerAdapter(context, accounts);
		final ActionBar actionBar = this.getActionBar();
		actionBar.setListNavigationCallbacks(adapter, this);
		actionBar.setSelectedNavigationItem(activeAccountIndex);
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that simply returns the {@link android.app.Activity}
	 * if <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return this.getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	/**
	 * This method queries the Network provides web API for the users remaining SMS count.
	 * <p>
	 * This method <b>must</b> be called from both {@link #onResume()} and {@link #onCreateOptionsMenu(Menu)}.<br />
	 * There is two prerequisites to execution for this method
	 * <ol>
	 * <li>An operator has been retrieved.</li>
	 * <li>The {@link MenuItem} that displays the count has been created.</li>
	 * </ol>
	 * In normal operation this method will not execute it's task until {@link #onCreateOptionsMenu(Menu)} calls it as then both
	 * prerequisites will be satisfied.<br />
	 * However in the case of the App being run for the first time onResume needs to call this method as the prerequisites will
	 * not be satisfied until the onResume has run after the {@link AddAccountActivity} has completed successfully.
	 * </p>
	 */
	public void retrieveRemainingSmsCount() {
		if (this.operator != null && this.remaingSmsMenuItem != null) {
			final RemainingSmsTask task = new RemainingSmsTask(this.operator, this.preferences, this.remaingSmsMenuItem);
			task.execute();
		}
	}

	/**
	 * This method queries the Network provides web API for the maximum character count of a message.
	 */
	private void getMaxCharacterCount() {
		final MaxCharacterCountTask task = new MaxCharacterCountTask(this.operator, this.preferences, this.charCountWatcher,
				this.messageEditText);
		task.execute();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == ADD_FIRST_ACCOUNT_REQUEST) {
			if (resultCode == Activity.RESULT_CANCELED) {
				this.finish();
			}
		}
	}

	/**
	 * This method handles the UI click of the send button.
	 * 
	 * @param view
	 *            The required View parameter.
	 */
	public void sendMessage(final View view) {
		final SendTask sendTask = new SendTask(this, this.operator);
		sendTask.execute();
	}

	/**
	 * Update the enabled state of the send button based on the recipients and message text boxes.
	 */
	public void updateSendButton() {
		final boolean isMessageEmpty = !this.messageEditText.getText().toString().isEmpty();
		final boolean isRecipientsEmpty = !this.recipientEdittext.getText().toString().isEmpty();
		this.sendButton.setEnabled(isMessageEmpty && isRecipientsEmpty);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		this.remaingSmsMenuItem = menu.findItem(R.id.action_remaining_sms);
		this.retrieveRemainingSmsCount();
		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_account:
			this.startActivity(new Intent(this, AddAccountActivity.class));
			break;
		case R.id.action_manage_account:
			this.startActivity(new Intent(this, ManageAccountsActivity.class));
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void update(final Observable observable, final Object data) {
		ComposeActivity.this.updateSendButton();
	}

	@Override
	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		final int accountId = (int) itemId;
		final Account account = this.accountDatabase.getAccountById(accountId);
		this.operator = OperatorFactory.getOperator(account);
		this.retrieveRemainingSmsCount();
		this.getMaxCharacterCount();

		final Editor editor = this.preferences.edit();
		editor.putInt(ACTIVE_ACCOUNT, accountId);
		editor.commit();
		return true;
	}
}