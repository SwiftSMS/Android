package com.icc.tasks;

import android.os.AsyncTask;
import android.widget.EditText;

import com.icc.net.Operator;
import com.icc.view.CharacterCountTextWatcher;

public class MaxCharacterCountTask extends AsyncTask<String, Integer, Integer> {
	private final Operator operator;
	private final CharacterCountTextWatcher charCountWatcher;
	private final EditText messageEditText;

	public MaxCharacterCountTask(final Operator operator, final CharacterCountTextWatcher charCountWatcher,
			final EditText messageEditText) {
		this.operator = operator;
		this.charCountWatcher = charCountWatcher;
		this.messageEditText = messageEditText;
	}

	@Override
	protected Integer doInBackground(final String... params) {
		return this.operator.getCharacterLimit();
	}

	@Override
	protected void onPostExecute(final Integer result) {
		this.charCountWatcher.setCharacterLimit(result);
		this.charCountWatcher.afterTextChanged(this.messageEditText.getEditableText());
	}
}
