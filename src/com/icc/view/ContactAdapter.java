package com.icc.view;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter implements Filterable {

	private final LayoutInflater layoutInflater;
	private ArrayList<String> items;
	private final Context context;

	public ContactAdapter(final Context context) {
		this.context = context;
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = new ArrayList<String>();
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
		textView.setText(this.items.get(position));

		return view;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(final CharSequence constraint, final FilterResults results) {
				ContactAdapter.this.items = (ArrayList<String>) results.values;
				if (results.count > 0) {
					ContactAdapter.this.notifyDataSetChanged();
				} else {
					ContactAdapter.this.notifyDataSetInvalidated();
				}
			}

			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				final String[] PROJECTION = new String[] { Contacts._ID, Contacts.DISPLAY_NAME };
				final Uri contentUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(constraint.toString()));

				final ContentResolver resolver = ContactAdapter.this.context.getContentResolver();
				final Cursor c = resolver.query(contentUri, PROJECTION, null, null, null);

				final ArrayList<String> values = new ArrayList<String>();
				while (c.moveToNext()) {
					final String cName = c.getString(1);
					values.add(cName);
				}

				final FilterResults result = new FilterResults();
				result.values = values;
				result.count = values.size();
				return result;
			}
		};
	}
}