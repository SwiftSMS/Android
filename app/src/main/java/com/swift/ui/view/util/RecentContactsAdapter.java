package com.swift.ui.view.util;

import static com.swift.InternalString.SMS_ADDRESS;
import static com.swift.InternalString.SMS_DATE;
import static com.swift.InternalString.SMS_THREADS_CONTENT_URI;
import static com.swift.InternalString.THREAD_ID;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
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
		this.populateRecentContacts();
		this.notifyDataSetChanged();
	}

	private void populateRecentContacts() {
		this.items.clear();
		final List<Contact> contacts = this.getRecentContacts();
		this.items.addAll(contacts);
	}

	private List<Contact> getRecentContacts() {
		final String sortOrder = SMS_DATE + " DESC";
		final Cursor cursor = this.resolver.query(SMS_THREADS_CONTENT_URI, null, null, null, sortOrder);

		if (cursor != null) {
			return this.getRecentContacts(cursor);
		}
		return new ArrayList<Contact>();
	}

	private List<Contact> getRecentContacts(final Cursor cursor) {
		final List<Contact> contacts = new ArrayList<Contact>();

		while (cursor.moveToNext() && contacts.size() < 5) {
			final String cNumber = this.getContactNumber(cursor);

			if (ContactUtils.isNumber(cNumber)) {
				final String cName = this.getContactName(cNumber);
				contacts.add(new Contact(cName, null, cNumber, null));
			}
		}

		return contacts;
	}

	private String getContactNumber(final Cursor cursor) {
		if (this.isStockProvider(cursor)) {
			return this.getContactNumberStock(cursor);
		} else if (this.isSamsungProvider(cursor)) {
			return this.getContactNumberSamsung(cursor);
		}

		return null;
	}

	private boolean isStockProvider(final Cursor cursor) {
		return cursor.getColumnIndex(SMS_ADDRESS) != -1;
	}

	private String getContactNumberStock(final Cursor cursor) {
		final String rawNumber = cursor.getString(cursor.getColumnIndex(SMS_ADDRESS));

		return ContactUtils.removeIrishPrefix(rawNumber);
	}

	private boolean isSamsungProvider(final Cursor cursor) {
		return cursor.getColumnIndex(THREAD_ID) != -1;
	}

	private String getContactNumberSamsung(final Cursor cursor) {
		final String threadId = cursor.getString(cursor.getColumnIndex(THREAD_ID));
		final String rawNumber = this.getContactNumberFromThread(threadId);

		return ContactUtils.removeIrishPrefix(rawNumber);
	}

	private String getContactNumberFromThread(final String threadId) {
		final String[] projection = new String[] { SMS_ADDRESS };

		final Uri uri = Uri.withAppendedPath(SMS_THREADS_CONTENT_URI, threadId);
		final Cursor cursor = this.resolver.query(uri, projection, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			return cursor.getString(0);
		}
		return null;
	}

	private String getContactName(final String number) {
		final String[] projection = new String[] { PhoneLookup.DISPLAY_NAME };
		final Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

		final Cursor cursor = this.resolver.query(contactUri, projection, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			return cursor.getString(0);
		}
		return null;
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