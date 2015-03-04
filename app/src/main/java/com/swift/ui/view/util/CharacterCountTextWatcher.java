package com.swift.ui.view.util;

import java.util.Observable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class is responsible for counting the characters in an {@link EditText} and display that count in a {@link TextView}.
 */
public class CharacterCountTextWatcher extends Observable implements TextWatcher {

	private final TextView characterCountTextView;
	private int maxCharCount = 0;

	/**
	 * Create the character counting {@link TextWatcher}.
	 * 
	 * @param characterCountTextView
	 *            This is the {@link TextView} used to display the character count.
	 */
	public CharacterCountTextWatcher(final TextView characterCountTextView) {
		this.characterCountTextView = characterCountTextView;
	}

	@Override
	public void afterTextChanged(final Editable s) {
		if (this.isMaxCharacterCountSet()) {
			int wrappedCharCount = s.length() % this.maxCharCount;
			wrappedCharCount = (wrappedCharCount == 0) ? this.maxCharCount : wrappedCharCount;
			final int charactersRemaining = this.maxCharCount - wrappedCharCount;
			final StringBuilder countText = new StringBuilder(Integer.toString(charactersRemaining));
			if (s.length() > this.maxCharCount) {
				final int numMsgs = (s.length() / this.maxCharCount) + 1;
				countText.append(" / " + numMsgs);
			}
			this.characterCountTextView.setText(countText.toString());

			if (s.length() > 100) {
				this.characterCountTextView.setVisibility(View.VISIBLE);
			} else {
				this.characterCountTextView.setVisibility(View.GONE);
			}
		}
	}

	private boolean isMaxCharacterCountSet() {
		return this.maxCharCount > 0;
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		this.setChanged();
		this.notifyObservers();
	}

	public void setCharacterLimit(final int result) {
		this.maxCharCount = result;
	}
}