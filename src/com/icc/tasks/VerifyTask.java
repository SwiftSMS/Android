package com.icc.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.icc.net.Operator;

/**
 * This class is an {@link AsyncTask} responsible for testing if the login details provided by the user are valid.
 */
public class VerifyTask extends AsyncTask<String, Integer, Boolean> {

	private final Context context;
	private final Operator operator;

	/**
	 * Create a new instance of the verify Task.
	 * 
	 * @param context
	 *            The add account context used to display a {@link Toast} message.
	 * @param operator
	 *            The network operator used to test the account details.
	 */
	public VerifyTask(final Activity context, final Operator operator) {
		this.context = context;
		this.operator = operator;
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected Boolean doInBackground(final String... params) {
		return this.operator.login();
	}

	@Override
	protected void onPostExecute(final Boolean result) {
		if (result) {
			Toast.makeText(this.context, "Account verified successfully!", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this.context, "Could not verify account details!", Toast.LENGTH_LONG).show();
		}
	}

}
