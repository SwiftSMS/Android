package com.swift.ui.view.util;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.widget.Filter;
import android.widget.Filterable;

import com.swift.model.Contact;
import com.swift.utils.ContactUtils;

public class ContactSuggestionAdapter extends BaseContactAdapter implements Filterable {

	public ContactSuggestionAdapter(final Context context, final List<Contact> contacts) {
		super(context, contacts);
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence constraint, final FilterResults results) {
				if (results.values != null) {
					ContactSuggestionAdapter.this.items = (ArrayList<Contact>) results.values;
				} else {
					ContactSuggestionAdapter.this.items = new ArrayList<Contact>();
				}
				if (results.count > 0) {
					ContactSuggestionAdapter.this.notifyDataSetChanged();
				} else {
					ContactSuggestionAdapter.this.notifyDataSetInvalidated();
				}
			}

			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				final String[] projection = new String[] { Contacts.DISPLAY_NAME, Contacts.PHOTO_THUMBNAIL_URI, Phone.NUMBER,
						Phone.TYPE, Phone.LABEL };
				final String selection = Data.MIMETYPE + " = ?";
				final String[] selectionArgs = new String[] { Phone.CONTENT_ITEM_TYPE };
				final String sortOrder = Contacts.SORT_KEY_PRIMARY;

				final String searchText = ContactUtils.getLastContact(constraint.toString());
				final Uri contentUri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(searchText));

				final ContentResolver resolver = ContactSuggestionAdapter.this.context.getContentResolver();
				final Cursor c = resolver.query(contentUri, projection, selection, selectionArgs, sortOrder);

				final ArrayList<Contact> values = new ArrayList<Contact>();
				while (c.moveToNext()) {
					final String cName = c.getString(0);
					final String cPhoto = c.getString(1);
					final String cNumber = c.getString(2);
					final int cLabelType = c.getInt(3);
					final String cCustomLabel = c.getString(4);

					final Resources res = ContactSuggestionAdapter.this.context.getResources();
					final String cNumberType = Phone.getTypeLabel(res, cLabelType, cCustomLabel).toString();

					values.add(new Contact(cName, cPhoto, cNumber, cNumberType));
				}

				final FilterResults result = new FilterResults();
				result.values = values;
				result.count = values.size();
				return result;
			}
		};
	}
}