package com.icc.tasks;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.EditText;

import com.icc.net.Operator;
import com.icc.view.CharacterCountTextWatcher;

public class MaxCharacterCountTask extends AsyncTask<String, Integer, Integer> {

	private static final String PREFERENCE_KEY = "_max_character_count";

	private final Operator operator;
	private final CharacterCountTextWatcher charCountWatcher;
	private final EditText messageEditText;
	private final SharedPreferences preferences;

	public MaxCharacterCountTask(final Operator operator, final SharedPreferences preferences,
			final CharacterCountTextWatcher charCountWatcher, final EditText messageEditText) {
		this.operator = operator;
		this.preferences = preferences;
		this.charCountWatcher = charCountWatcher;
		this.messageEditText = messageEditText;
	}

	@Override
	protected Integer doInBackground(final String... params) {
		final String operatorName = this.operator.getClass().getSimpleName();
		final int storedCharCount = this.preferences.getInt(operatorName + MaxCharacterCountTask.PREFERENCE_KEY, -1);
		if (storedCharCount != -1) {
			return storedCharCount;
		}
		return this.operator.getCharacterLimit();
	}

	@Override
	protected void onPostExecute(final Integer result) {
		this.charCountWatcher.setCharacterLimit(result);
		this.charCountWatcher.afterTextChanged(this.messageEditText.getEditableText());
		final Editor editor = this.preferences.edit();
		editor.putInt("meteor_max_character_count", result);
		editor.commit();
	}
}
