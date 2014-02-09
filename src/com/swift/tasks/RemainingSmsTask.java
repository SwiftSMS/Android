package com.swift.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.swift.R;
import com.swift.io.net.Operator;

public class RemainingSmsTask extends AsyncTask<String, Integer, Integer> {

	private static final String PREFERENCE_KEY = "_remaining_sms";

	private final MenuItem actionBarItem;
	private final Operator operator;
	private final SharedPreferences preferences;

	private final String key;

	private final TextView textView;

	public RemainingSmsTask(final Context context, final Operator operator, final SharedPreferences preferences, final MenuItem actionBarRemainingSms) {
		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.textView = (TextView) inflater.inflate(R.layout.remaining_sms_view, null);
		this.operator = operator;
		this.preferences = preferences;
		this.actionBarItem = actionBarRemainingSms;
		this.key = this.operator.getAccount().getMobileNumber() + RemainingSmsTask.PREFERENCE_KEY;
	}

	@Override
	protected void onPreExecute() {
		final int savedRemaining = this.preferences.getInt(this.key, -1);
		if (savedRemaining == -1) {
			this.actionBarItem.setActionView(R.layout.progress_view);
		} else {
			this.setTitle(savedRemaining);
		}
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
		this.actionBarItem.setActionView(this.textView);
		if (remaining == -1) {
			this.textView.setText("?");
		} else {
			this.textView.setText(Integer.toString(remaining));
		}
	}
}