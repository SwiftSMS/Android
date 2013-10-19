package com.icc.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class is responsible for counting the characters in an {@link EditText} and display that count in a {@link TextView}.
 */
public class CharacterCountTextWatcher implements TextWatcher {

	private final TextView characterCountTextView;
	private int maxCharacterCount = 0;

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
			if (s.length() > this.maxCharacterCount) {
				s.delete(this.maxCharacterCount, s.length());
			}

			final int charactersRemaining = this.maxCharacterCount - s.length();
			this.characterCountTextView.setText(Integer.toString(charactersRemaining));

			if (s.length() > 100) {
				this.characterCountTextView.setVisibility(View.VISIBLE);
			} else {
				this.characterCountTextView.setVisibility(View.GONE);
			}
		}
	}

	private boolean isMaxCharacterCountSet() {
		return this.maxCharacterCount > 0;
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
	}

	public void setCharacterLimit(final int result) {
		this.maxCharacterCount = result;
	}
}