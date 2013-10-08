package com.icc;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.icc.acc.Account;
import com.icc.acc.Network;
import com.icc.net.Meteor;
import com.icc.net.Operator;
import com.icc.view.acc.AddAccountActivity;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(final String... params) {
				final Account vodaAccount = new Account("user", "Work", "pass", Network.VODAFONE);
				final Operator operator = new Meteor(vodaAccount);
				return operator.login();
			}

			@Override
			protected void onPostExecute(final String result) {
				final TextView view = (TextView) MainActivity.this.findViewById(R.id.text_compose_message);
				view.setText(result);
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
