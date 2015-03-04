package com.swift.ui.view.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.swift.R;
import com.swift.model.Contact;

public class BaseContactAdapter extends BaseAdapter {

	final LayoutInflater layoutInflater;
	List<Contact> items;
	final Context context;

	public BaseContactAdapter(final Context context, final List<Contact> contacts) {
		this.context = context;
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = contacts != null ? contacts : new ArrayList<Contact>();
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
			view = this.layoutInflater.inflate(R.layout.contact_suggest_dropdown, null);
		}

		final TextView nameTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_name);
		final ImageView photoImageView = (ImageView) view.findViewById(R.id.image_contact_suggestion_photo);
		final TextView numberTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_number);
		final TextView numberTypeTextView = (TextView) view.findViewById(R.id.text_contact_suggestion_label);
		nameTextView.setText(this.items.get(position).getName());
		photoImageView.setImageURI(this.items.get(position).getPhoto());
		numberTextView.setText(this.items.get(position).getNumber());
		numberTypeTextView.setText(this.items.get(position).getNumberType());

		return view;
	}
}