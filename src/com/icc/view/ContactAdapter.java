package com.icc.view;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter implements Filterable {

	private final String[] defaultItems = new String[] { "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight",
			"Nine", "Ten", "Eleven", "Twleve", "Thirteen", "Fourteen", "Fiftheen", "Sixteen", "Seventeen", "Eighteen",
			"Ninteen", "Twenty" };
	private final LayoutInflater layoutInflater;
	private ArrayList<String> items;

	public ContactAdapter(final Context context) {
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = new ArrayList<String>();
		for (final String s : this.defaultItems) {
			this.items.add(s);
		}
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
				final ArrayList<String> values = new ArrayList<String>();
				for (final String value : ContactAdapter.this.items) {
					if (value.contains(constraint)) {
						values.add(value);
					}
				}

				final FilterResults result = new FilterResults();
				result.values = values;
				result.count = values.size();
				return result;
			}
		};
	}
}