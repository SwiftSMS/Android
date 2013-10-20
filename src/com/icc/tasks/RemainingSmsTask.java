package com.icc.tasks;

import android.os.AsyncTask;
import android.view.MenuItem;

import com.icc.net.Operator;

public class RemainingSmsTask extends AsyncTask<String, Integer, Integer> {
	private final MenuItem actionBarItem;
	private final Operator operator;

	public RemainingSmsTask(final Operator operator, final MenuItem actionBarRemainingSms) {
		this.operator = operator;
		this.actionBarItem = actionBarRemainingSms;
	}

	@Override
	protected Integer doInBackground(final String... params) {
		return this.operator.getRemainingSMS();
	}

	@Override
	protected void onPostExecute(final Integer result) {
		this.actionBarItem.setTitle(result.toString());
	}
}