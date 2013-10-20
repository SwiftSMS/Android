package com.icc.tasks;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.view.MenuItem;

import com.icc.net.Operator;

public class RemainingSmsTask extends AsyncTask<String, Integer, Integer> {

	private static final String PREFERENCE_KEY = "_remaining_sms";

	private final MenuItem actionBarItem;
	private final Operator operator;
	private final SharedPreferences preferences;

	private final String key;

	public RemainingSmsTask(final Operator operator, final SharedPreferences preferences, final MenuItem actionBarRemainingSms) {
		this.operator = operator;
		this.preferences = preferences;
		this.actionBarItem = actionBarRemainingSms;
		this.key = this.operator.getAccount().getMobileNumber() + RemainingSmsTask.PREFERENCE_KEY;
	}

	@Override
	protected void onPreExecute() {
		final int savedRemaining = this.preferences.getInt(this.key, -1);
		this.setTitle(savedRemaining);
	}

	@Override
	protected Integer doInBackground(final String... params) {
		return this.operator.getRemainingSMS();
	}

	@Override
	protected void onPostExecute(final Integer result) {
		this.setTitle(result);
		final Editor editor = this.preferences.edit();
		editor.putInt(this.key, result);
		editor.commit();
	}

	private void setTitle(final int remaining) {
		if (remaining == -1) {
			this.actionBarItem.setTitle("?");
		} else {
			this.actionBarItem.setTitle(Integer.toString(remaining));
		}
	}
}