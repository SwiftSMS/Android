package com.icc.view;

import static com.icc.InternalString.PREFS_KEY;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;

import com.icc.InternalString;
import com.icc.R;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;
import com.icc.model.Account;

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
		this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
		this.accountDatabase = AccountDataSource.getInstance(this);

		this.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		this.getListView().setMultiChoiceModeListener(new MultiChoiceListener());

		this.accountAdapter = new AccountAdapter(this, this.preferences, this.accountDatabase.getAllAccounts());
		this.setListAdapter(this.accountAdapter);
	}

	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long itemId) {
		this.setActiveAccount(itemId);
		super.onListItemClick(l, v, position, itemId);
	}

	private void setActiveAccount(final long id) {
		final int selectedAccountId = (int) id;

		final SharedPreferences.Editor editor = this.preferences.edit();
		editor.putInt(InternalString.ACTIVE_ACCOUNT, selectedAccountId);
		editor.apply();

		this.accountAdapter.notifyDataSetChanged();
	}

	public class MultiChoiceListener implements MultiChoiceModeListener {

		private final List<Account> selectedAccounts = new ArrayList<Account>();

		@Override
		public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
			if (item.getItemId() == R.id.action_manage_accounts_delete) {
				if (item.getItemId() == R.id.action_manage_accounts_delete) {
					for (final Account acc : this.selectedAccounts) {
						ManageAccountsActivity.this.accountDatabase.removeAccount(acc);
						ManageAccountsActivity.this.accountAdapter.accounts.remove(acc);
					}
					mode.finish();
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
			final MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.manage_context, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(final ActionMode mode) {
		}

		@Override
		public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
			return false;
		}

		@Override
		public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id, final boolean checked) {
			final Account account = (Account) ManageAccountsActivity.this.getListView().getItemAtPosition(position);
			if (checked) {
				this.selectedAccounts.add(account);
			} else {
				this.selectedAccounts.remove(account);
			}
		}
	}
}