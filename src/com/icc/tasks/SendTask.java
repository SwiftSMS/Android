package com.icc.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.icc.R;
import com.icc.net.Operator;
import com.icc.utils.MultipleContactUtilities;

/**
 * This class is an {@link AsyncTask} responsible for sending a web text using the provided operator.
 */
public class SendTask extends AsyncTask<String, Integer, Boolean> {

	private final EditText messageEditText;
	private final ProgressBar progressBar;
	private final EditText recipientsEditText;
	private final Context activity;
	private final Operator operator;

	/**
	 * Create a new instance of the sending Task.
	 * 
	 * @param activity
	 *            The compose activity sending the message. Used to get the UI elements.
	 * @param operator
	 *            The network operator used to send the message.
	 */
	public SendTask(final Activity activity, final Operator operator) {
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
	protected Boolean doInBackground(final String... params) {
		if (this.operator.login()) {
			final String message = this.messageEditText.getText().toString();
			final String recipients = this.recipientsEditText.getText().toString();

			return this.operator.send(MultipleContactUtilities.getEnteredContactsAsList(recipients), message);
		}
		return false;
	}

	@Override
	protected void onPostExecute(final Boolean result) {
		this.progressBar.setVisibility(View.GONE);
		if (result) {
			Toast.makeText(this.activity, "Message sent!", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this.activity, "Message failed to send!", Toast.LENGTH_LONG).show();
		}
	}

}
