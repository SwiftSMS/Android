package com.swift.tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.swift.R;
import com.swift.io.net.Operator;

/**
 * This class is an {@link AsyncTask} responsible for testing if the login details provided by the user are valid.
 */
public class VerifyTask extends AsyncTask<String, Integer, Boolean> {

	private final Operator operator;
	private final TextView doneButton;
	private final ImageView verifyImage;

	/**
	 * Create a new instance of the verify Task.
	 * 
	 * @param operator
	 *            The network operator used to test the account details.
	 * @param verifyImage
	 *            The verify image, rotates once task is complete should be stopped.
	 * @param buttonDone
	 *            The actionbar's Done button, used to enable or disable.
	 */
	public VerifyTask(final Operator operator, final ImageView verifyImage, final TextView buttonDone) {
		this.operator = operator;
		this.doneButton = buttonDone;
		this.verifyImage = verifyImage;
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
		final View layout = (View) this.verifyImage.getParent();
		if (result) {
			this.doneButton.setEnabled(true);
			layout.setBackgroundResource(R.drawable.green_highlight);
		} else {
			layout.setBackgroundResource(R.drawable.red_highlight);
		}
	}
}