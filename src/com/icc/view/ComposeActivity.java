package com.icc.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.icc.InternalString;
import com.icc.R;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;
import com.icc.model.Account;
import com.icc.net.Operator;
import com.icc.net.OperatorFactory;

public class ComposeActivity extends Activity {

	private Operator operator;
	private TextView remainingSMSTextView;
	private CharacterCountTextWatcher charCountWatcher;
	private EditText messageEditText;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		this.remainingSMSTextView = (TextView) this.findViewById(R.id.label_compose_remaining_sms);
		this.messageEditText = (EditText) this.findViewById(R.id.text_compose_message);

		final TextView characterCountTextView = (TextView) this.findViewById(R.id.label_compose_character_count);
		this.charCountWatcher = new CharacterCountTextWatcher(characterCountTextView);
		this.messageEditText.addTextChangedListener(this.charCountWatcher);
	}

	@Override
	protected void onResume() {
		super.onResume();

		final SharedPreferences preferences = this.getSharedPreferences(InternalString.PREFS_KEY, Context.MODE_PRIVATE);
		final int accountId = preferences.getInt(InternalString.LATEST_ACCOUNT, -1);

		if (accountId == -1) {
			this.startActivityForResult(new Intent(this, AddAccountActivity.class), 0);
		} else {
			final IAccountDatabase accountDatabase = AccountDataSource.getInstance(this);
			final Account account = accountDatabase.getAccountById(accountId);
			this.operator = OperatorFactory.getOperator(account);
			this.getRemainingSMS();
			this.getMaxCharacterCount();
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_CANCELED) {
			this.finish();
		}
	}

	/**
	 * This method queries the Network provides web API for the users remaining number of SMS messages and updates the UI with
	 * the information.
	 */
	private void getRemainingSMS() {
		new AsyncTask<String, Integer, Integer>() {
			@Override
			protected Integer doInBackground(final String... params) {
				return ComposeActivity.this.operator.getRemainingSMS();
			}

			@Override
			protected void onPostExecute(final Integer result) {
				ComposeActivity.this.remainingSMSTextView.setText(result + " remaining");
			}
		}.execute();
	}

	/**
	 * This method queries the Network provides web API for the maximum character count of a message.
	 */
	private void getMaxCharacterCount() {
		new AsyncTask<String, Integer, Integer>() {
			@Override
			protected Integer doInBackground(final String... params) {
				return ComposeActivity.this.operator.getCharacterLimit();
			}

			@Override
			protected void onPostExecute(final Integer result) {
				ComposeActivity.this.charCountWatcher.setCharacterLimit(result);
				ComposeActivity.this.charCountWatcher.afterTextChanged(ComposeActivity.this.messageEditText.getEditableText());
			}
		}.execute();
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
