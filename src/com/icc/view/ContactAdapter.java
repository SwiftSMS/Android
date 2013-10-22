package com.icc.view;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.icc.model.Contact;

public class ContactAdapter extends BaseAdapter implements Filterable {

	private final LayoutInflater layoutInflater;
	private ArrayList<Contact> items;
	private final Context context;

	public ContactAdapter(final Context context) {
		this.context = context;
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = new ArrayList<Contact>();
	}

	@Override
	public int getCount() {
		return this.items.size();
	}

	@Override
	public Object getItem(final int position) {
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
			view = this.layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
		}

		final TextView textView = (TextView) view;
		textView.setText(this.items.get(position).getName() + " (" + this.items.get(position).getNumber() + ")");

		return view;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence constraint, final FilterResults results) {
				if (results.values != null) {
					ContactAdapter.this.items = (ArrayList<Contact>) results.values;
				} else {
					ContactAdapter.this.items = new ArrayList<Contact>();
				}
				if (results.count > 0) {
					ContactAdapter.this.notifyDataSetChanged();
				} else {
					ContactAdapter.this.notifyDataSetInvalidated();
				}
			}

			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				final String[] projection = new String[] { Contacts.DISPLAY_NAME, Phone.NUMBER, Phone.TYPE };
				final String selection = Data.MIMETYPE + " = ?";
				final String[] selectionArgs = new String[] { Phone.CONTENT_ITEM_TYPE };
				final String sortOrder = Contacts.DISPLAY_NAME + " ASC";
				final Uri contentUri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(constraint.toString()));

				final ContentResolver resolver = ContactAdapter.this.context.getContentResolver();
				final Cursor c = resolver.query(contentUri, projection, selection, selectionArgs, sortOrder);

				final ArrayList<Contact> values = new ArrayList<Contact>();
				while (c.moveToNext()) {
					final String cName = c.getString(0);
					final String cNumber = c.getString(1);
					final String cNumberType = c.getString(2);
					final Contact contact = new Contact(cName, cNumber, cNumberType);
					values.add(contact);
				}

				final FilterResults result = new FilterResults();
				result.values = values;
				result.count = values.size();
				return result;
			}
		};
	}
}