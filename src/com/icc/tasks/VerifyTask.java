package com.icc.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.icc.R;
import com.icc.net.Operator;

/**
 * This class is an {@link AsyncTask} responsible for testing if the login details provided by the user are valid.
 */
public class VerifyTask extends AsyncTask<String, Integer, Boolean> {

	private final Operator operator;
	private final ImageView verifyImage;
	private final int green;
	private final int red;

	/**
	 * Create a new instance of the verify Task.
	 * 
	 * @param context
	 *            The add account context used to display a {@link Toast} message.
	 * @param operator
	 *            The network operator used to test the account details.
	 * @param verifyImage
	 *            The verify image, rotates once task is complete should be stopped.
	 */
	public VerifyTask(final Activity context, final Operator operator, final ImageView verifyImage) {
		this.operator = operator;
		this.verifyImage = verifyImage;
		this.red = context.getResources().getColor(R.color.red);
		this.green = context.getResources().getColor(R.color.green);
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
		this.verifyImage.clearAnimation();
		if (result) {
			this.verifyImage.setColorFilter(this.green);
		} else {
			this.verifyImage.setColorFilter(this.red);
		}
	}
}