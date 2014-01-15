package com.swift.ui.view.util;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.swift.model.Account;

/**
 * This class is used to display a list of Accounts in an {@link Activity}s {@link ActionBar} when it's using a dropdown or
 * {@link Spinner} for navigation.
 */
public class AccountSpinnerAdapter extends AccountAdapter {

	public AccountSpinnerAdapter(final Context context, final List<Account> accounts) {
		super(context, null, accounts);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null);
		}

		final TextView textViewAccountName = (TextView) view.findViewById(android.R.id.text1);
		textViewAccountName.setText(this.getAccounts().get(position).getAccountName());

		return view;
	}
}