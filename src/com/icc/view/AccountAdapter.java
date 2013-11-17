package com.icc.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.icc.R;
import com.icc.model.Account;

/**
 * Custom Adapter used to hold and display ICC Account's
 * 
 * @author Rob Powell
 */
public class AccountAdapter extends BaseAdapter {

	final List<Account> accounts;
	static LayoutInflater layoutInflater;

	public AccountAdapter(final Context context, final List<Account> accounts) {
		this.accounts = accounts;
		AccountAdapter.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return this.accounts.size();
	}

	@Override
	public Object getItem(final int i) {
		return this.accounts.get(i);
	}

	@Override
	public long getItemId(final int i) {
		return this.accounts.get(i).getId();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = AccountAdapter.layoutInflater.inflate(R.layout.layout_network_selection_item, null);
		}
		final Account account = this.accounts.get(position);

		final TextView textViewAccountName = (TextView) view.findViewById(R.id.text_network_selection_operator_name);
		textViewAccountName.setText(account.getAccountName());
		final ImageView imageView = (ImageView) view.findViewById(R.id.image_network_selection_operator);
		imageView.setImageResource(account.getOperator().getLogo());

		// if (account.getId() == 1) {
		// final View selectedIndicator = view.findViewById(R.id.view_network_selection_selected_indicator);
		// selectedIndicator.setVisibility(View.VISIBLE);
		// }

		return view;
	}
}