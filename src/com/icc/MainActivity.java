package com.icc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.icc.acc.Account;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;
import com.icc.net.Meteor;
import com.icc.net.Operator;
import com.icc.view.acc.AddAccountActivity;

public class MainActivity extends Activity {

	private EditText messageEditText;
	private EditText recipientsEditText;
	private IAccountDatabase accountDatabase;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		this.preferences = this.getSharedPreferences(InternalString.PREFS_KEY, Context.MODE_PRIVATE);
		this.accountDatabase = AccountDataSource.getInstance(this);

		this.messageEditText = (EditText) this.findViewById(R.id.text_compose_message);
		this.recipientsEditText = (EditText) this.findViewById(R.id.text_compose_recipients);
	}

	public void sendMessage(final View view) {
		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(final String... params) {
				final int accountId = MainActivity.this.preferences.getInt(InternalString.LATEST_ACCOUNT, -1);
				final Account account = MainActivity.this.accountDatabase.getAccountById(accountId);
				// final Account account = new Account("user", "My Meteor", "pass", Network.METEOR);
				final Operator operator = new Meteor(account);
				operator.login();

				final String message = MainActivity.this.messageEditText.getText().toString();
				final String recipients = MainActivity.this.recipientsEditText.getText().toString();
				return operator.send(recipients, message);
			}

			@Override
			protected void onPostExecute(final String result) {
				final Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage(result);
				builder.setTitle("Server Message");
				builder.show();
			}
		}.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		if (item.getItemId() == R.id.action_add_account) {
			this.startActivity(new Intent(this, AddAccountActivity.class));
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
