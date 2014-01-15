package com.swift.tasks;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.EditText;

import com.swift.io.net.Operator;
import com.swift.ui.view.util.CharacterCountTextWatcher;

public class MaxCharacterCountTask extends AsyncTask<String, Integer, Integer> {

	private static final String PREFERENCE_KEY = "_max_character_count";

	private final Operator operator;
	private final CharacterCountTextWatcher charCountWatcher;
	private final EditText messageEditText;
	private final SharedPreferences preferences;

	private final String key;

	public MaxCharacterCountTask(final Operator operator, final SharedPreferences preferences,
			final CharacterCountTextWatcher charCountWatcher, final EditText messageEditText) {
		this.operator = operator;
		this.preferences = preferences;
		this.charCountWatcher = charCountWatcher;
		this.messageEditText = messageEditText;
		this.key = this.operator.getAccount().getMobileNumber() + MaxCharacterCountTask.PREFERENCE_KEY;
	}

	@Override
	protected Integer doInBackground(final String... params) {
		final int storedCharCount = this.preferences.getInt(this.key, -1);
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
		editor.putInt(this.key, result);
		editor.commit();
	}
}
