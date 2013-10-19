package com.icc.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.icc.R;
import com.icc.model.Account;

/**
 * @author Rob Powell
 */
public class AccountAdapter extends BaseAdapter {

	private final List<Account> accounts;
	private static LayoutInflater layoutInflater;

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
	public View getView(final int i, final View view, final ViewGroup viewGroup) {
		View v = view;
		if (v == null) {
			v = AccountAdapter.layoutInflater.inflate(R.layout.manage_account_list_item, null);
		}

		v.findViewById(R.id.textview_manage_account_name);

		return v;
	}
}