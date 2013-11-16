package com.icc.view;

import static com.icc.InternalString.PREFS_KEY;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.icc.InternalString;
import com.icc.R;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;
import com.icc.model.Account;
import com.icc.model.Network;
import com.icc.net.Operator;
import com.icc.net.OperatorFactory;
import com.icc.tasks.VerifyTask;

/**
 * Activity to handle adding a new operator account to ICC
 * 
 * @author Rob Powell
 * @version 1.3
 */
public class AddAccountActivity extends Activity {

	private static int FAILED_DB_ADD = -1;
	private IAccountDatabase accountDatabase;
	private TextView textAccNumber, textAccName, textAccPassword;
	private CheckBox checkActiveAccount;
	private SharedPreferences preferences;
	private TextView buttonDone;
	private LinearLayout buttonVerify;
	private ImageView buttonVerifyIcon;
	private Network selectedNetwork;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		this.preferences = this.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_add_account);

		this.setupCustomActionBar();

		this.textAccName = (TextView) this.findViewById(R.id.text_acc_name);
		this.textAccNumber = (TextView) this.findViewById(R.id.text_acc_number);
		this.textAccPassword = (TextView) this.findViewById(R.id.text_acc_password);
		this.checkActiveAccount = (CheckBox) this.findViewById(R.id.checkBox_active_acc);
		this.buttonVerify = (LinearLayout) this.findViewById(R.id.actionbar_verify);
		this.buttonVerifyIcon = (ImageView) this.findViewById(R.id.actionbar_verify_icon);
		this.buttonDone = (TextView) this.findViewById(R.id.actionbar_done);

		final TextView labelSelectedNetwork = (TextView) this.findViewById(R.id.text_add_account_selected_network);
		this.selectedNetwork = Network.valueOf(this.getIntent().getStringExtra(InternalString.OPERATOR).toUpperCase(Locale.UK));
		labelSelectedNetwork.setText(this.selectedNetwork.toString());

		final TextWatcher watcher = new UpdateButtonsTextWatcher();
		this.textAccNumber.addTextChangedListener(watcher);
		this.textAccPassword.addTextChangedListener(watcher);

		this.updateDoneButton();
		this.setResult(RESULT_CANCELED);
	}

	/**
	 * Setup the custom {@link ActionBar} for adding accounts.
	 */
	private void setupCustomActionBar() {
		final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_cancel, null);
		// Show the custom action bar view and hide the normal Home icon and title.
		final ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
		actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
	}

	/**
	 * Set the enabled state or the {@link ActionBar} buttons based on the contents of the phone number & password fields.
	 */
	private void updateDoneButton() {
		final boolean isMessageEmpty = !this.textAccNumber.getText().toString().isEmpty();
		final boolean isRecipientsEmpty = !this.textAccPassword.getText().toString().isEmpty();
		this.buttonDone.setEnabled(isMessageEmpty && isRecipientsEmpty);
		this.buttonVerify.setEnabled(isMessageEmpty && isRecipientsEmpty);
	}

	@Override
	protected void onResume() {
		this.accountDatabase = AccountDataSource.getInstance(this);
		super.onResume();
	}

	public void addAccount(final View view) {
		final Account account = this.makeAccountFromUI();
		final int successfullyAddedId = this.accountDatabase.addAccount(account);

		if (successfullyAddedId != FAILED_DB_ADD) {
			Toast.makeText(this, "Accounted Added", Toast.LENGTH_SHORT).show();
			if (this.checkActiveAccount.isChecked() || successfullyAddedId == 1) {
				final Editor editor = this.preferences.edit();
				editor.putInt(InternalString.ACTIVE_ACCOUNT, successfullyAddedId);
				editor.commit();
			}
		}

		this.setResult(Activity.RESULT_OK);
		this.finish();
	}

	/**
	 * This method pulls data from the UI components and tries to construct an account object using the details.
	 * 
	 * @return An {@link Account} using the details entered on the UI.
	 */
	private Account makeAccountFromUI() {
		final String number = this.textAccNumber.getText().toString();
		final String password = this.textAccPassword.getText().toString();

		final String numberLast4Digits = number.substring(number.length() - Math.min(4, number.length()));
		final String defaultAccName = this.selectedNetwork + " (" + numberLast4Digits + ")";
		final String enteredAccName = this.textAccName.getText().toString();
		final String accountName = enteredAccName.isEmpty() ? defaultAccName : enteredAccName;

		return new Account(number, accountName, password, this.selectedNetwork);
	}

	public void verifyAccount(final View view) {
		final Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		this.buttonVerifyIcon.startAnimation(rotation);

		final Account account = this.makeAccountFromUI();
		final Operator operator = OperatorFactory.getOperator(account);
		new VerifyTask(this, operator, this.buttonVerifyIcon).execute();
	}

	/**
	 * {@link TextWatcher} to update the UI buttons when text in the required fields change.
	 */
	private class UpdateButtonsTextWatcher implements TextWatcher {
		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			AddAccountActivity.this.updateDoneButton();
		}

		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
		}

		@Override
		public void afterTextChanged(final Editable s) {
		}
	}
}