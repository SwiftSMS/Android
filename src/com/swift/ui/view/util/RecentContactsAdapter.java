package com.swift.ui.view.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.swift.R;
import com.swift.model.Contact;

public class RecentContactsAdapter extends BaseAdapter implements ListAdapter {
	
	private final LayoutInflater layoutInflater;
	private final List<Contact> items = new ArrayList<Contact>();
	
	public RecentContactsAdapter(final Context context) {
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		items.add(new Contact("Sean", null, "0871255832", "Mobile"));
		items.add(new Contact("Colin", null, "0895856699", "Mobile"));
		items.add(new Contact("Marguerite", null, "+447047821254", "Mobile"));
		items.add(new Contact("Daddy", null, "+35387 124 5896", "Mobile"));
		items.add(new Contact("Mammy", null, "0035383 101 0892", "Mobile"));
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