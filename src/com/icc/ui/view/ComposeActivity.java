package com.icc.ui.view;

import static com.icc.InternalString.ACTIVE_ACCOUNT;
import static com.icc.InternalString.DEFAULT_CONTACT_SEPARATOR;
import static com.icc.InternalString.PREFS_KEY;
import static com.icc.InternalString.SMS_BODY;

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
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icc.R;
import com.icc.io.db.AccountDataSource;
import com.icc.io.db.IAccountDatabase;
import com.icc.io.net.Operator;
import com.icc.io.net.OperatorFactory;
import com.icc.model.Account;
import com.icc.tasks.MaxCharacterCountTask;
import com.icc.tasks.RecentContactsTask;
import com.icc.tasks.RemainingSmsTask;
import com.icc.tasks.SendTask;
import com.icc.ui.view.anim.AnimationRunner;
import com.icc.ui.view.util.AccountSpinnerAdapter;
import com.icc.ui.view.util.CharacterCountTextWatcher;
import com.icc.ui.view.util.ContactSuggestionAdapter;
import com.icc.ui.view.util.ContactSuggestionClickListener;
import com.icc.utils.Notification;

public class ComposeActivity extends Activity implements Observer, ActionBar.OnNavigationListener {

	private static final String PERSISTED_RECIPIENT_BASE_KEY = "persisted_recipient_";
	private static final String PERSISTED_MESSAGE_BASE_KEY = "persisted_message_";

	private static final int ADD_ACCOUNT_REQUEST = 23;

	private CharacterCountTextWatcher charCountWatcher;
	private EditText messageEditText;
	private AutoCompleteTextView recipientEdittext;
	private MenuItem remaingSmsMenuItem;
	private ImageButton sendButton;
	private RelativeLayout notificationArea;

	private Operator operator;
	private SharedPreferences preferences;
	private IAccountDatabase accountDatabase;
	private RemainingSmsTask task;
	private Context themedContext;
	private ListView recentContactsList;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_compose);

		this.themedContext = this.getActionBarThemedContextCompat();
		final ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
		this.accountDatabase = AccountDataSource.getInstance(this.themedContext);
		this.messageEditText = (EditText) this.findViewById(R.id.text_compose_message);
		this.sendButton = (ImageButton) this.findViewById(R.id.button_compose_send);
		this.notificationArea = (RelativeLayout) this.findViewById(R.id.layout_compose_notification_area);
		this.recipientEdittext = (AutoCompleteTextView) this.findViewById(R.id.text_compose_recipients);
		this.recentContactsList = (ListView) this.findViewById(R.id.list_compose_recent_contacts);
		final TextView characterCountTextView = (TextView) this.findViewById(R.id.label_compose_character_count);
		this.charCountWatcher = new CharacterCountTextWatcher(characterCountTextView);
		final ContactSuggestionClickListener itemClickTextWatcher = new ContactSuggestionClickListener();

		this.sendButton.setEnabled(false);
		this.recipientEdittext.setAdapter(new ContactSuggestionAdapter(this.themedContext, null));
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
		final String persistedMessage = this.preferences.getString(this.getMessagePrefKey(), null);
		final String persistedRecipient = this.preferences.getString(this.getRecipientPrefKey(), null);

		if (intentData != null) {
			final String smsto = intentData.getSchemeSpecificPart();
			this.recipientEdittext.setText(smsto + DEFAULT_CONTACT_SEPARATOR);
			final String smsBody = this.getIntent().getStringExtra(SMS_BODY);
			if (smsBody != null) {
				this.messageEditText.setText(smsBody);
			} else {
				this.messageEditText.setText(persistedMessage);
			}
			this.messageEditText.requestFocus();
		} else {
			this.recipientEdittext.setText(persistedRecipient);
			this.messageEditText.setText(persistedMessage);
			if (persistedRecipient != null && !persistedRecipient.isEmpty()) {
				this.messageEditText.requestFocus();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		final String message = this.messageEditText.getText().toString();
		final String recipient = this.recipientEdittext.getText().toString();

		final Editor editor = this.preferences.edit();
		editor.putString(this.getMessagePrefKey(), message);
		editor.putString(this.getRecipientPrefKey(), recipient);
		editor.apply();
	}

	/**
	 * This method gets the key that is used to store the draft message in {@link SharedPreferences}.
	 * <p>
	 * There is a different key based on the launch context.
	 * <ul>
	 * <li>A default key is used when the {@link Activity} is entered via the launcher.</li>
	 * <li>There is a unique key for each contact when the {@link Activity} is entered through a send {@link Intent}.</li>
	 * </ul>
	 * </p>
	 * 
	 * @return The {@link SharedPreferences} key to persist the message in the current launch context.
	 */
	private String getMessagePrefKey() {
		return PERSISTED_MESSAGE_BASE_KEY + this.getIntent().getData();
	}

	/**
	 * This method gets the key that is used to store the draft message recipients in {@link SharedPreferences}.
	 * 
	 * @see #getMessagePrefKey() for details on the key.
	 * 
	 * @return The {@link SharedPreferences} key to persist the message recipients in the current launch context.
	 */
	private String getRecipientPrefKey() {
		return PERSISTED_RECIPIENT_BASE_KEY + this.getIntent().getData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.getRecentContacts();

		final int accountId = this.preferences.getInt(ACTIVE_ACCOUNT, -1);
		if (accountId == -1) {
			this.startActivityForResult(new Intent(this.themedContext, NetworkSelectionActivity.class), ADD_ACCOUNT_REQUEST);
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

	private void getRecentContacts() {
		final Context context = this.getActionBarThemedContextCompat();
		final RecentContactsTask task = new RecentContactsTask(context, this.recentContactsList);
		task.execute();
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
		final AccountSpinnerAdapter adapter = new AccountSpinnerAdapter(this.themedContext, accounts);
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
	 * not be satisfied until the onResume has run after the {@link NetworkSelectionActivity} has completed successfully.
	 * </p>
	 */
	public void retrieveRemainingSmsCount() {
		if (this.operator != null && this.remaingSmsMenuItem != null) {
			if (this.task != null) {
				this.task.cancel(true);
			}
			this.task = new RemainingSmsTask(this.themedContext, this.operator, this.preferences, this.remaingSmsMenuItem);
			this.task.execute();
		}
	}

	/**
	 * This method queries the Network provides web API for the maximum character count of a message.
	 */
	private void getMaxCharacterCount() {
		final MaxCharacterCountTask task = new MaxCharacterCountTask(this.operator, this.preferences, this.charCountWatcher, this.messageEditText);
		task.execute();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == ADD_ACCOUNT_REQUEST) {
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

		final Editor editor = this.preferences.edit();
		editor.remove(this.getMessagePrefKey());
		editor.remove(this.getRecipientPrefKey());
		editor.apply();
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
			this.startActivity(new Intent(this.themedContext, NetworkSelectionActivity.class));
			break;
		case R.id.action_manage_account:
			this.startActivity(new Intent(this.themedContext, ManageAccountsActivity.class));
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

	/**
	 * Method used to display notifications on the main compose activity
	 * 
	 * @param paramNotification
	 *            Notification objects for different event's
	 */
	public void addNotification(final Notification paramNotification) {

		final int FADE_DURATION = 500;

		final TextView notificationText = (TextView) this.findViewById(R.id.text_compose_notification);

		final Animation fadeInAnim = new AlphaAnimation(0.0f, 1.0f);
		final Animation fadeOutAnim = new AlphaAnimation(1.0f, 0.0f);

		notificationText.setText(this.getResources().getString(paramNotification.getStringResource()));
		this.notificationArea.setBackgroundColor(this.getResources().getColor(paramNotification.getColourResource()));

		final Handler postAnimationHandler = new Handler();
		postAnimationHandler.post(new AnimationRunner(this.notificationArea, FADE_DURATION, fadeInAnim, View.VISIBLE));
		postAnimationHandler.postDelayed(new AnimationRunner(this.notificationArea, FADE_DURATION, fadeOutAnim, View.GONE), 2000);
	}
}