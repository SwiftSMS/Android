package com.swift.ui.view.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.swift.R;
import com.swift.model.Network;

/**
 * Adapter to display a list of Network Operators.
 */
public class NetworkSelectionAdapter extends BaseAdapter {

	private final Network[] accounts = Network.values();
	private final LayoutInflater layoutInflater;

	public NetworkSelectionAdapter(final Context context) {
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return this.accounts.length;
	}

	@Override
	public Object getItem(final int i) {
		return this.accounts[i];
	}

	@Override
	public long getItemId(final int i) {
		return this.accounts[i].ordinal();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = this.layoutInflater.inflate(R.layout.layout_operator_list_item, null);
		}

		final TextView textViewAccountName = (TextView) view.findViewById(R.id.text_network_selection_operator_name);
		final ImageView imageView = (ImageView) view.findViewById(R.id.image_network_selection_operator);
		textViewAccountName.setText(this.accounts[position].toString());
		imageView.setImageResource(this.accounts[position].getLogo());
		imageView.setClickable(false);

		return view;
	}
}