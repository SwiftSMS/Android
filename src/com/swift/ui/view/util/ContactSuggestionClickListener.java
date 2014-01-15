package com.swift.ui.view.util;

import static com.swift.InternalString.DEFAULT_CONTACT_SEPARATOR;
import static com.swift.InternalString.EMPTY_STRING;

import java.util.Observable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.swift.utils.ContactUtils;

/**
 * This class is used to manage allowing the user enter multiple contacts to the Recipients text box.
 */
public class ContactSuggestionClickListener extends Observable implements TextWatcher, OnItemClickListener {

	/** used to store the text the user had entered before clicking a contact suggestion. */
	private String oldText = EMPTY_STRING;
	private Editable editable;

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		// When an item from the contacts suggestion is clicked place the original contacts before the new one
		this.editable.insert(0, this.oldText);
		this.oldText = EMPTY_STRING;
		this.editable.append(DEFAULT_CONTACT_SEPARATOR);
	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
		this.oldText = EMPTY_STRING;
		if (this.isAllTextReplaced(start, s.length(), count, after)) {
			final String old = s.toString();
			if (ContactUtils.hasMultipleContacts(old)) {
				// only store the original text if the contact separator character is found
				this.oldText = ContactUtils.getAllButLastContacts(old);
			}
		}
	}

	/**
	 * This method is used to determine if all text is about to be replaced in an EditText. Should be called from
	 * {@link #beforeTextChanged(CharSequence, int, int, int)}.
	 * 
	 * @param start
	 *            The <code>start</code> parameter received in beforeTextChanged. It indicates the beginning position from where
	 *            the text is being replaced.
	 * @param length
	 *            The <code>length</code> of the <code>s</code> parameter received in beforeTextChanged. It indicates the length
	 *            of the string that will replace the text.
	 * @param count
	 *            The <code>count</code> parameter received in beforeTextChanged. It indicates the number of characters being
	 *            replaced.
	 * @param after
	 *            The <code>after</code> parameter received in beforeTextChanged. It indicates the length of the string once the
	 *            replace occurs.
	 * @return <code>true</code> if there is text in the EditText, all of the text will be replaced and it is not being replaced
	 *         by an empty string. <br />
	 *         Otherwise <code>false</code>.
	 */
	private boolean isAllTextReplaced(final int start, final int length, final int count, final int after) {
		return start == 0 && length > 0 && length == count && after != 0;
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		this.setChanged();
		this.notifyObservers();
	}

	@Override
	public void afterTextChanged(final Editable s) {
		this.editable = s;
	}
}
