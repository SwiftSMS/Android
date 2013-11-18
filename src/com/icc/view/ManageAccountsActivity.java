package com.icc.view;

import static com.icc.InternalString.PREFS_KEY;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.icc.InternalString;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;

/**
 * @author Rob Powell
 */
public class ManageAccountsActivity extends ListActivity {

	private IAccountDatabase accountDatabase;
	private AccountAdapter accountAdapter;
	private SharedPreferences preferences;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
		this.accountDatabase = AccountDataSource.getInstance(this);

		this.accountAdapter = new AccountAdapter(this, this.preferences, this.accountDatabase.getAllAccounts());
		this.setListAdapter(this.accountAdapter);

		super.onResume();
	}

	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long itemId) {
		this.setActiveAccount(itemId);
		super.onListItemClick(l, v, position, itemId);
	}

	private void setActiveAccount(final long id) {
		final int selectedAccountId = (int) id;
		this.accountAdapter.notifyDataSetChanged();

		final SharedPreferences.Editor editor = this.preferences.edit();
		editor.putInt(InternalString.ACTIVE_ACCOUNT, selectedAccountId);
		editor.apply();
	}
}