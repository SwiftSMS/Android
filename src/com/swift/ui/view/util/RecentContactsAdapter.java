package com.swift.ui.view.util;

import static com.swift.InternalString.LOG_TAG;
import static com.swift.InternalString.SMS_ADDRESS;
import static com.swift.InternalString.SMS_DATE;
import static com.swift.InternalString.SMS_THREADS_CONTENT_URI;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.swift.R;
import com.swift.model.Contact;
import com.swift.utils.ContactUtils;

public class RecentContactsAdapter extends BaseAdapter implements ListAdapter {

	private final LayoutInflater layoutInflater;
	private final List<Contact> items = new ArrayList<Contact>();
	private final ContentResolver resolver;

	public RecentContactsAdapter(final Context context) {
		this.resolver = context.getContentResolver();
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.populateRecentContacts();
	}

	public void refresh() {
		this.items.clear();
		this.populateRecentContacts();
		this.notifyDataSetChanged();
	}

	private void populateRecentContacts() {
		try {
			final String[] projection = new String[] { SMS_ADDRESS };
			final String sortOrder = SMS_DATE + " DESC";

			final Cursor cursor = this.resolver.query(SMS_THREADS_CONTENT_URI, projection, null, null, sortOrder);

			while (cursor.moveToNext() && this.items.size() < 5) {
				final String cNumber = ContactUtils.removeIrishPrefix(cursor.getString(0));

				if (ContactUtils.isNumber(cNumber)) {
					final String cName = this.getContactName(cNumber);
					this.items.add(new Contact(cName, null, cNumber, null));
				}
			}
		} catch (final Exception e) {
			Log.e(LOG_TAG, "", e);
		}
	}

	private String getContactName(final String number) {
		String name = null;

		final String[] projection = new String[] { PhoneLookup.DISPLAY_NAME };
		final Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

		final Cursor cursor = this.resolver.query(contactUri, projection, null, null, null);

		if (cursor.moveToFirst()) {
			final int columnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			name = cursor.getString(columnIndex);
		}
		return name;
	}

	@Override
	public int getCount() {
		return this.items.size();
	}

	@Override
	public Contact getItem(final int position) {
		return this.items.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = this.layoutInflater.inflate(R.layout.contact_suggest_dropdown, null);
		}

		final TextView nameTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_name);
		final TextView numberTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_number);
		nameTextView.setText(this.items.get(position).getName());
		numberTextView.setText(this.items.get(position).getNumber());

		return view;
	}
}