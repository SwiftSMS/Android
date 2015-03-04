package com.swift.ui.view.util;

import static com.swift.InternalString.DEFAULT_CONTACT_SEPARATOR;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.swift.model.Contact;

public class RecentContactsClickListener implements OnItemClickListener{

	private final EditText editText;

	public RecentContactsClickListener(final EditText textView) {
		this.editText = textView;
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final Contact clickedItem = (Contact) adapter.getItemAtPosition(position);

		this.editText.setText(clickedItem.toString());
		this.editText.append(DEFAULT_CONTACT_SEPARATOR);

		final View nextView = this.editText.focusSearch(View.FOCUS_DOWN);
		nextView.requestFocus();
	}
}