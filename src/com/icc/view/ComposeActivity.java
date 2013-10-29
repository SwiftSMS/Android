package com.icc.view;

import static com.icc.InternalString.CONTACT_SEPARATOR;
import static com.icc.InternalString.LATEST_ACCOUNT;
import static com.icc.InternalString.PREFS_KEY;
import static com.icc.InternalString.SMS_BODY;
import static com.icc.InternalString.SPACE;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

public class ComposeActivity extends Activity {

	private static final int ADD_FIRST_ACCOUNT_REQUEST = 23;

	private Operator operator;
	private CharacterCountTextWatcher charCountWatcher;
	private EditText messageEditText;
	private SharedPreferences preferences;
	private AutoCompleteTextView recipientEdittext;
	private MenuItem remaingSmsMenuItem;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
		this.messageEditText = (EditText) this.findViewById(R.id.text_compose_message);
		this.recipientEdittext = (AutoCompleteTextView) this.findViewById(R.id.text_compose_recipients);
		final TextView characterCountTextView = (TextView) this.findViewById(R.id.label_compose_character_count);
		this.charCountWatcher = new CharacterCountTextWatcher(characterCountTextView);
		final ContactSuggestionClickListener itemClickTextWatcher = new ContactSuggestionClickListener();

		this.recipientEdittext.setAdapter(new ContactAdapter(this));
		this.recipientEdittext.setThreshold(1);
		this.recipientEdittext.addTextChangedListener(itemClickTextWatcher);
		this.recipientEdittext.setOnItemClickListener(itemClickTextWatcher);
		this.messageEditText.addTextChangedListener(this.charCountWatcher);

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

		final int accountId = this.preferences.getInt(LATEST_ACCOUNT, -1);
		if (accountId == -1) {
			this.startActivityForResult(new Intent(this, AddAccountActivity.class), ADD_FIRST_ACCOUNT_REQUEST);
		} else {
			if (this.operator == null) {
				final IAccountDatabase accountDatabase = AccountDataSource.getInstance(this);
				final Account account = accountDatabase.getAccountById(accountId);
				this.operator = OperatorFactory.getOperator(account);
				this.setTitle(account.getAccountName());
			}
			this.getRemainingSmsCount();
			this.getMaxCharacterCount();
		}
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
	 * This method queries the Network provides web API for the maximum character count of a message.
	 */
	private void getMaxCharacterCount() {
		final MaxCharacterCountTask task = new MaxCharacterCountTask(this.operator, this.preferences, this.charCountWatcher,
				this.messageEditText);
		task.execute();
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
	private void getRemainingSmsCount() {
		if (this.operator != null && this.remaingSmsMenuItem != null) {
			final RemainingSmsTask task = new RemainingSmsTask(this.operator, this.preferences, this.remaingSmsMenuItem);
			task.execute();
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

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		this.remaingSmsMenuItem = menu.findItem(R.id.action_remaining_sms);
		this.getRemainingSmsCount();
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
}
