package com.swift.ui.view.util;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.swift.InternalString;
import com.swift.R;
import com.swift.model.Account;

/**
 * Custom Adapter used to hold and display ICC Account's
 * 
 * @author Rob Powell
 */
public class AccountAdapter extends BaseAdapter {

	private final List<Account> accounts;
	private final SharedPreferences prefs;
	static LayoutInflater layoutInflater;

	public AccountAdapter(final Context context, final SharedPreferences preferences, final List<Account> accounts) {
		this.accounts = accounts;
		this.prefs = preferences;
		AccountAdapter.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Removes the first occurrence of the specified account object from this List. Effectively removing the account from the
	 * UI.
	 * 
	 * @param account
	 *            the account object to remove.
	 * 
	 * @return <code>true</code> if account was removed, <code>false</code> otherwise.
	 */
	public boolean removeAccount(final Account account) {
		return this.accounts.remove(account);
	}

	List<Account> getAccounts() {
		return this.accounts;
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
			view = AccountAdapter.layoutInflater.inflate(R.layout.layout_operator_list_item, null);
		}
		final Account account = this.accounts.get(position);

		final TextView textViewAccountName = (TextView) view.findViewById(R.id.text_network_selection_operator_name);
		final TextView textAccountUsername = (TextView) view.findViewById(R.id.text_network_selection_account_username);
		final ImageView imageView = (ImageView) view.findViewById(R.id.image_network_selection_operator);
		textViewAccountName.setText(account.getAccountName());
		textAccountUsername.setVisibility(View.VISIBLE);
		textAccountUsername.setText(account.getMobileNumber());
		imageView.setImageResource(account.getOperator().getLogo());

		final View selectedIndicator = view.findViewById(R.id.view_network_selection_selected_indicator);
		if (account.getId() == this.prefs.getInt(InternalString.ACTIVE_ACCOUNT, -1)) {
			selectedIndicator.setSelected(true);
		} else {
			selectedIndicator.setSelected(false);
		}

		return view;
	}
}