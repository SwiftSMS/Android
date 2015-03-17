package com.swift.ui.view;

import static com.swift.InternalString.ACTIVE_ACCOUNT;
import static com.swift.InternalString.OPERATOR;
import static com.swift.InternalString.PREFS_KEY;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.swift.R;
import com.swift.io.db.AccountDataSource;
import com.swift.io.db.IAccountDatabase;
import com.swift.io.net.Operator;
import com.swift.io.net.OperatorFactory;
import com.swift.model.Account;
import com.swift.model.Network;
import com.swift.tasks.VerifyTask;
import com.swift.tasks.results.OperationResult;

/**
 * Activity to handle adding a new operator account to ICC
 * 
 * @author Rob Powell
 * @version 1.3
 */
public class AddAccountActivity extends Activity {

	private static int FAILED_DB_ADD = -1;
	private IAccountDatabase accountDatabase;
	private EditText textAccNumber, textAccName, textAccPassword;
	private ImageView imageShowPassword;
	private CheckBox checkActiveAccount;
	private SharedPreferences preferences;
	private TextView buttonDone;
	private LinearLayout buttonVerify;
	private ImageView buttonVerifyIcon;
	private Network selectedNetwork;
	private AsyncTask<String, Integer, OperationResult> verifyTask;

	private boolean isPasswordVisible = false;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		this.preferences = this.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_add_account);

		this.setupCustomActionBar();

		this.textAccName = (EditText) this.findViewById(R.id.text_acc_name);
		this.textAccNumber = (EditText) this.findViewById(R.id.text_acc_number);
		this.textAccPassword = (EditText) this.findViewById(R.id.text_acc_password);
		this.imageShowPassword = (ImageView) this.findViewById(R.id.image_acc_show_password);
		this.checkActiveAccount = (CheckBox) this.findViewById(R.id.checkBox_active_acc);
		this.buttonVerify = (LinearLayout) this.findViewById(R.id.actionbar_verify);
		this.buttonVerifyIcon = (ImageView) this.findViewById(R.id.actionbar_verify_icon);
		this.buttonDone = (TextView) this.findViewById(R.id.actionbar_done);
		this.accountDatabase = AccountDataSource.getInstance(this);

		final TextView labelSelectedNetwork = (TextView) this.findViewById(R.id.text_add_account_selected_network);
		final int operator = this.getIntent().getIntExtra(OPERATOR, -1);
		this.selectedNetwork = Network.values()[operator];
		labelSelectedNetwork.setText(this.selectedNetwork.toString());
		this.textAccNumber.setInputType(this.selectedNetwork.getInputType());

		final TextWatcher watcher = new UpdateButtonsTextWatcher();
		this.textAccNumber.addTextChangedListener(watcher);
		this.textAccPassword.addTextChangedListener(watcher);

		if (this.accountDatabase.isEmpty()) {
			this.checkActiveAccount.setEnabled(false);
			this.checkActiveAccount.setChecked(true);
		}

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
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
		actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}

	/**
	 * Set the enabled state or the {@link ActionBar} buttons based on the contents of the phone number & password fields.
	 */
	private void updateDoneButton() {
		this.buttonDone.setEnabled(false);

		final boolean isMessageEmpty = !this.textAccNumber.getText().toString().isEmpty();
		final boolean isRecipientsEmpty = !this.textAccPassword.getText().toString().isEmpty();
		this.buttonVerify.setEnabled(isMessageEmpty && isRecipientsEmpty);
		this.buttonVerify.setBackgroundColor(Color.TRANSPARENT);
	}

	public void addAccount(final View view) {
		final Account account = this.makeAccountFromUI();
		final int successfullyAddedId = this.accountDatabase.addAccount(account);

		if (successfullyAddedId != FAILED_DB_ADD) {
			if (this.checkActiveAccount.isChecked() || successfullyAddedId == 1) {
				final Editor editor = this.preferences.edit();
				editor.putInt(ACTIVE_ACCOUNT, successfullyAddedId);
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
		final String accountName = this.getAccountName(number);

		return new Account(number, accountName, password, this.selectedNetwork);
	}

	private String getAccountName(final String number) {
		final String enteredAccName = this.textAccName.getText().toString();

		String username = number;
		if (username.contains("@")) { // to handle email addresses
			username = username.split("@")[0];
		}
		final int length = username.length();
		final String numberLast4Digits = username.substring(length - Math.min(4, length));
		final String defaultAccName = this.selectedNetwork + " (" + numberLast4Digits + ")";

		return enteredAccName.isEmpty() ? defaultAccName : enteredAccName;
	}

	public void showPassword(final View view) {
		int start = textAccPassword.getSelectionStart();
		int end = textAccPassword.getSelectionEnd();

		if (isPasswordVisible) {
			textAccPassword.setTransformationMethod(new PasswordTransformationMethod());
			imageShowPassword.setImageResource(R.drawable.show_password);
		} else {
			imageShowPassword.setImageResource(R.drawable.hide_password);
			textAccPassword.setTransformationMethod(null);
		}

		textAccPassword.setSelection(start, end);
		isPasswordVisible ^= true;
	}

	public void verifyAccount(final View view) {
		this.cancelOngoingVerifyTask();
		final Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
		rotation.setRepeatCount(Animation.INFINITE);
		this.buttonVerifyIcon.startAnimation(rotation);

		final Account account = this.makeAccountFromUI();
		final Operator operator = OperatorFactory.getOperator(account);
		this.verifyTask = new VerifyTask(operator, this.buttonVerifyIcon, this.buttonDone).execute();
	}

	/**
	 * This method will cancel the ongoing {@link VerifyTask} if there is currently one running.
	 */
	private void cancelOngoingVerifyTask() {
		if (this.verifyTask != null) {
			this.verifyTask.cancel(true);
			this.verifyTask = null;
			this.buttonVerifyIcon.clearAnimation();
		}
	}

	/**
	 * {@link TextWatcher} to update the UI buttons when text in the required fields change.
	 */
	private class UpdateButtonsTextWatcher implements TextWatcher {
		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			AddAccountActivity.this.updateDoneButton();
			AddAccountActivity.this.cancelOngoingVerifyTask();
		}

		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
		}

		@Override
		public void afterTextChanged(final Editable s) {
		}
	}
}
