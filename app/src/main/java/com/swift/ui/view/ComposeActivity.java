package com.swift.ui.view;

import static com.swift.InternalString.ACTIVE_ACCOUNT;
import static com.swift.InternalString.DEFAULT_CONTACT_SEPARATOR;
import static com.swift.InternalString.PREFS_KEY;
import static com.swift.InternalString.SMS_BODY;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.lucasr.twowayview.TwoWayView;

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
import android.webkit.CookieSyncManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.swift.R;
import com.swift.io.db.AccountDataSource;
import com.swift.io.db.IAccountDatabase;
import com.swift.io.net.Operator;
import com.swift.io.net.OperatorFactory;
import com.swift.model.Account;
import com.swift.tasks.MaxCharacterCountTask;
import com.swift.tasks.RemainingSmsTask;
import com.swift.tasks.SendTask;
import com.swift.tasks.results.OperationResult;
import com.swift.ui.view.anim.AnimationRunner;
import com.swift.ui.view.util.AccountSpinnerAdapter;
import com.swift.ui.view.util.CharacterCountTextWatcher;
import com.swift.ui.view.util.ContactSuggestionAdapter;
import com.swift.ui.view.util.ContactSuggestionClickListener;
import com.swift.ui.view.util.RecentContactsAdapter;
import com.swift.ui.view.util.RecentContactsClickListener;

public class ComposeActivity extends Activity implements Observer, ActionBar.OnNavigationListener {

	private static final int FADE_DURATION = 500;

	private static final String PERSISTED_RECIPIENT_BASE_KEY = "persisted_recipient_";
	private static final String PERSISTED_MESSAGE_BASE_KEY = "persisted_message_";

	private static final int ADD_ACCOUNT_REQUEST = 23;

	private CharacterCountTextWatcher charCountWatcher;
	private RecentContactsAdapter recentAdapter;
	private EditText messageEditText;
	private AutoCompleteTextView recipientEdittext;
	private MenuItem remaingSmsMenuItem;
	private ImageButton sendButton;
	private TextView notificationArea;
	private TwoWayView recentList;
	private View recentLayout;

	private Operator operator;
	private SharedPreferences preferences;
	private IAccountDatabase accountDatabase;

	private CookieManager cookieMgr;
	private RemainingSmsTask task;
	private Context themedContext;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_compose);

		this.themedContext = this.getActionBarThemedContextCompat();
		final ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		this.cookieMgr = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(this.cookieMgr);
		CookieSyncManager.createInstance(this.themedContext);

		this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
		this.accountDatabase = AccountDataSource.getInstance(this.themedContext);
		this.messageEditText = (EditText) this.findViewById(R.id.text_compose_message);
		this.sendButton = (ImageButton) this.findViewById(R.id.button_compose_send);
		this.notificationArea = (TextView) this.findViewById(R.id.text_compose_notification);
		this.recipientEdittext = (AutoCompleteTextView) this.findViewById(R.id.text_compose_recipients);
		this.recentList = (TwoWayView) this.findViewById(R.id.list_compose_recent);
		this.recentLayout = this.findViewById(R.id.layout_compose_recent);

		final TextView characterCountTextView = (TextView) this.findViewById(R.id.label_compose_character_count);
		this.charCountWatcher = new CharacterCountTextWatcher(characterCountTextView);
		final ContactSuggestionClickListener itemClickTextWatcher = new ContactSuggestionClickListener();
		itemClickTextWatcher.addObserver(this);
		this.recentAdapter = new RecentContactsAdapter(this.themedContext);

		this.sendButton.setEnabled(false);
		this.recipientEdittext.setAdapter(new ContactSuggestionAdapter(this.themedContext, null));
		this.recipientEdittext.setThreshold(1);
		this.recipientEdittext.addTextChangedListener(itemClickTextWatcher);
		this.recipientEdittext.setOnItemClickListener(itemClickTextWatcher);
		this.charCountWatcher.addObserver(this);
		this.messageEditText.addTextChangedListener(this.charCountWatcher);
		this.recentList.setAdapter(this.recentAdapter);
		this.recentList.setHorizontalScrollBarEnabled(false);
		this.recentList.setOnItemClickListener(new RecentContactsClickListener(this.recipientEdittext));

		this.handleIntentData();
	}

	/**
	 * Handle the data passed to this {@link Activity} from it's {@link Intent}.
	 */
	private void handleIntentData() {
		final Intent intent = this.getIntent();
		final String iAction = intent.getAction();
		final String iType = intent.getType();

		if (iAction.equals(Intent.ACTION_SEND) && iType != null && iType.equals("text/plain")) {
			this.handleShareIntent();
		} else if (iAction.equals(Intent.ACTION_SEND) || iAction.equals(Intent.ACTION_SENDTO)) {
			this.handleSendIntent();
		} else if (iAction.equals(Intent.ACTION_MAIN)) {
			this.handleMainIntent();
		}
	}

	private void handleShareIntent() {
		final Intent intent = this.getIntent();

		this.messageEditText.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
	}

	private void handleSendIntent() {
		final Uri iData = this.getIntent().getData();
		final String persistedMessage = this.preferences.getString(this.getMessagePrefKey(), null);

		final String smsto = iData.getSchemeSpecificPart();
		this.recipientEdittext.setText(smsto + DEFAULT_CONTACT_SEPARATOR);
		this.messageEditText.setText(persistedMessage);

		final String smsBody = this.getIntent().getStringExtra(SMS_BODY);
		if (smsBody != null) {
			this.messageEditText.setText(smsBody);
		}
		this.messageEditText.requestFocus();
	}

	private void handleMainIntent() {
		final String persistedMessage = this.preferences.getString(this.getMessagePrefKey(), null);
		final String persistedRecipient = this.preferences.getString(this.getRecipientPrefKey(), null);

		this.recipientEdittext.setText(persistedRecipient);
		this.messageEditText.setText(persistedMessage);
		if (persistedRecipient != null && !persistedRecipient.isEmpty()) {
			this.messageEditText.requestFocus();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
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
	 * The key is unique based on the launch context.
	 * <ul>
	 * <li>A default key is used when the {@link Activity} is entered via the launcher.</li>
	 * <li>When the {@link Activity} is started from an {@link Intent} a unique key derived from the {@link Intent} is used.</li>
	 * </ul>
	 * </p>
	 * 
	 * @return The {@link SharedPreferences} key to persist the message in the current launch context.
	 */
	private String getMessagePrefKey() {
		final Intent intent = this.getIntent();
		return PERSISTED_MESSAGE_BASE_KEY + intent.getAction() + intent.getType() + intent.getData();
	}

	/**
	 * This method gets the key that is used to store the draft message recipients in {@link SharedPreferences}.
	 * 
	 * @see #getMessagePrefKey() for details on the key.
	 * 
	 * @return The {@link SharedPreferences} key to persist the message recipients in the current launch context.
	 */
	private String getRecipientPrefKey() {
		final Intent intent = this.getIntent();
		return PERSISTED_RECIPIENT_BASE_KEY + intent.getAction() + intent.getType() + intent.getData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.recentAdapter.refresh();
		this.hideRecentList();
		CookieSyncManager.getInstance().startSync();

		final int accountId = this.preferences.getInt(ACTIVE_ACCOUNT, -1);
		if (accountId == -1) {
			this.startActivityForResult(new Intent(this.themedContext, NetworkSelectionActivity.class), ADD_ACCOUNT_REQUEST);
		} else {
			this.setupActionBar();
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
	 * This method <b>must</b> be called from both {@link #onResume()} and {@link #onCreateOptionsMenu(Menu)}.
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
		this.updateSendButton();
		this.updateRecentList();
	}

	private void updateRecentList() {
		if (this.recipientEdittext.length() > 0) {
			if (this.recentLayout.getVisibility() == View.VISIBLE) {
				final Animation fadeOutAnim = new AlphaAnimation(1, 0);
				this.runOnUiThread(new AnimationRunner(this.recentLayout, FADE_DURATION, fadeOutAnim, View.GONE));
			}
		} else {
			if (this.recentLayout.getVisibility() == View.GONE) {
				if (this.recentAdapter.getCount() != 0) {
					final Animation fadeInAnim = new AlphaAnimation(0, 1);
					this.runOnUiThread(new AnimationRunner(this.recentLayout, FADE_DURATION, fadeInAnim, View.VISIBLE));
				}
			}
		}
	}

	private void hideRecentList() {
		if (this.recentAdapter.getCount() == 0) {
			this.recentLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		final int accountId = (int) itemId;
		if (this.operator == null || this.operator.getAccount().getId() != accountId) {
			final Account account = this.accountDatabase.getAccountById(accountId);
			this.operator = OperatorFactory.getOperator(account);
			this.cookieMgr.getCookieStore().removeAll();
		}
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
	 *            OperationResult objects for different event's
	 */
	public void addNotification(final OperationResult paramNotification) {
		this.notificationArea.setText(this.getResources().getString(paramNotification.getStringResource()));
		this.notificationArea.setBackgroundColor(this.getResources().getColor(paramNotification.getColourResource()));

		final Animation fadeInAnim = new AlphaAnimation(0, 1);
		final Animation fadeOutAnim = new AlphaAnimation(1, 0);

		final Handler postAnimationHandler = new Handler();
		postAnimationHandler.post(new AnimationRunner(this.notificationArea, FADE_DURATION, fadeInAnim, View.VISIBLE));
		postAnimationHandler.postDelayed(new AnimationRunner(this.notificationArea, FADE_DURATION, fadeOutAnim, View.GONE), 2000);
	}
}