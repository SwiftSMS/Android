package com.icc.tasks;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.widget.ListView;

import com.icc.model.Contact;
import com.icc.ui.view.util.BaseContactAdapter;
import com.icc.utils.ContactUtils;

public class RecentContactsTask extends AsyncTask<String, Integer, List<Contact>> {

	private final Context context;
	private final ListView listView;

	public RecentContactsTask(final Context context, final ListView view) {
		this.context = context;
		this.listView = view;
	}

	@Override
	protected List<Contact> doInBackground(final String... arg0) {
		final String[] projection = new String[] { Contacts.DISPLAY_NAME, Contacts.PHOTO_THUMBNAIL_URI, Phone.NUMBER, Phone.TYPE,
				Phone.LABEL };
		final String selection = Data.MIMETYPE + " = ?";
		final String[] selectionArgs = new String[] { Phone.CONTENT_ITEM_TYPE };
		final String sortOrder = Contacts.SORT_KEY_PRIMARY;

		final String searchText = ContactUtils.getLastContact("Marguerite");
		final Uri contentUri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(searchText));

		final ContentResolver resolver = this.context.getContentResolver();
		final Cursor c = resolver.query(contentUri, projection, selection, selectionArgs, sortOrder);

		final ArrayList<Contact> values = new ArrayList<Contact>();
		while (c.moveToNext()) {
			final String cName = c.getString(0);
			final String cPhoto = c.getString(1);
			final String cNumber = c.getString(2);
			final int cLabelType = c.getInt(3);
			final String cCustomLabel = c.getString(4);

			final Resources res = this.context.getResources();
			final String cNumberType = Phone.getTypeLabel(res, cLabelType, cCustomLabel).toString();

			values.add(new Contact(cName, cPhoto, cNumber, cNumberType));
		}

		return values;
	}

	@Override
	protected void onPostExecute(final List<Contact> result) {
		this.listView.setAdapter(new BaseContactAdapter(this.context, result));
	}
}