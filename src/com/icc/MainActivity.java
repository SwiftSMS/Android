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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.icc.acc.Account;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;
import com.icc.net.Meteor;
import com.icc.view.acc.AddAccountActivity;

public class MainActivity extends Activity {

	private EditText messageEditText;
	private EditText recipientsEditText;
	private IAccountDatabase accountDatabase;
	private SharedPreferences preferences;
	private ProgressBar progressBar;
	private Meteor operator;
	private TextView remainingSMSTextView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		this.preferences = this.getSharedPreferences(InternalString.PREFS_KEY, Context.MODE_PRIVATE);
		this.accountDatabase = AccountDataSource.getInstance(this);

		this.messageEditText = (EditText) this.findViewById(R.id.text_compose_message);
		this.progressBar = (ProgressBar) this.findViewById(R.id.progressbar_compose);
		this.recipientsEditText = (EditText) this.findViewById(R.id.text_compose_recipients);
		this.remainingSMSTextView = (TextView) this.findViewById(R.id.label_compose_remaining_sms);

		final int accountId = this.preferences.getInt(InternalString.LATEST_ACCOUNT, -1);
		if (accountId == -1) {
			Toast.makeText(this, "No account, please add one.", Toast.LENGTH_LONG).show();
		} else {
			final Account account = this.accountDatabase.getAccountById(accountId);
			this.operator = new Meteor(account);
			this.getRemainingSMS();
		}
	}

	private void getRemainingSMS() {
		new AsyncTask<String, Integer, Integer>() {
			@Override
			protected Integer doInBackground(final String... params) {
				return MainActivity.this.operator.getRemainingSMS();
			}

			@Override
			protected void onPostExecute(final Integer result) {
				MainActivity.this.remainingSMSTextView.setText(result + " remaining");
			}
		}.execute();
	}

	public void sendMessage(final View view) {
		if (this.operator == null) {
			Toast.makeText(this, "No account, please add one.", Toast.LENGTH_LONG).show();
			return;
		}
		new AsyncTask<String, Integer, String>() {
			@Override
			protected void onPreExecute() {
				MainActivity.this.progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			protected String doInBackground(final String... params) {
				this.publishProgress(1);
				MainActivity.this.operator.login();
				this.publishProgress(2);

				final String message = MainActivity.this.messageEditText.getText().toString();
				final String recipients = MainActivity.this.recipientsEditText.getText().toString();
				this.publishProgress(3);
				return MainActivity.this.operator.send(recipients, message);
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				MainActivity.this.progressBar.setProgress(values[0]);
			}

			@Override
			protected void onPostExecute(final String result) {
				this.publishProgress(4);
				final Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage(result);
				builder.setTitle("Server Message");
				builder.show();
				MainActivity.this.progressBar.setVisibility(View.GONE);
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
