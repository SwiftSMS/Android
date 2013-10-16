package com.icc.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.icc.R;
import com.icc.net.Meteor;

/**
 * This class is an {@link AsyncTask} responsible for sending a web text using the provided operator.
 */
public class SendTask extends AsyncTask<String, Integer, String> {

	private final EditText messageEditText;
	private final ProgressBar progressBar;
	private final EditText recipientsEditText;
	private final Context activity;
	private final Meteor operator;

	/**
	 * Create a new instance of the sending Task.
	 * 
	 * @param activity
	 *            The compose activity sending the message. Used to get the UI elements.
	 * @param operator
	 *            The network operator used to send the message.
	 */
	public SendTask(final Activity activity, final Meteor operator) {
		this.activity = activity;
		this.operator = operator;
		this.messageEditText = (EditText) activity.findViewById(R.id.text_compose_message);
		this.progressBar = (ProgressBar) activity.findViewById(R.id.progressbar_compose);
		this.recipientsEditText = (EditText) activity.findViewById(R.id.text_compose_recipients);
	}

	@Override
	protected void onPreExecute() {
		this.progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected String doInBackground(final String... params) {
		this.publishProgress(1);
		this.operator.login();
		this.publishProgress(2);

		final String message = this.messageEditText.getText().toString();
		final String recipients = this.recipientsEditText.getText().toString();
		this.publishProgress(3);
		return this.operator.send(recipients, message);
	}

	@Override
	protected void onProgressUpdate(final Integer... values) {
		this.progressBar.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(final String result) {
		this.publishProgress(4);
		final Builder builder = new AlertDialog.Builder(this.activity);
		builder.setMessage(result);
		builder.setTitle("Server Message");
		builder.show();
		this.progressBar.setVisibility(View.GONE);
	}

}
