package com.icc.tasks;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.widget.ListView;

import com.icc.model.Contact;
import com.icc.ui.view.util.BaseContactAdapter;

public class RecentContactsTask extends AsyncTask<String, Integer, List<Contact>> {

	private final Context context;
	private final ListView listView;

	public RecentContactsTask(final Context context, final ListView view) {
		this.context = context;
		this.listView = view;
	}

	@Override
	protected List<Contact> doInBackground(final String... arg0) {
		final String[] projection = new String[] { Contacts.DISPLAY_NAME, Phone.NUMBER };
		final String sortOrder = String.format("%s DESC LIMIT 5", Contacts.LAST_TIME_CONTACTED);

		final ContentResolver resolver = this.context.getContentResolver();
		final Cursor c = resolver.query(Phone.CONTENT_URI, projection, null, null, sortOrder);

		final ArrayList<Contact> values = new ArrayList<Contact>();
		while (c.moveToNext()) {
			final String cName = c.getString(0);
			final String cNumber = c.getString(1);

			values.add(new Contact(cName, "", cNumber, null));
		}
		return values;
	}

	@Override
	protected void onPostExecute(final List<Contact> result) {
		this.listView.setAdapter(new BaseContactAdapter(this.context, result));
	}
}