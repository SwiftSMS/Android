package com.icc.view;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.icc.InternalString;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;

import static com.icc.InternalString.PREFS_KEY;

/**
 * @author Rob Powell
 */
public class ManageAccountsActivity extends ListActivity {

    private IAccountDatabase accountDatabase;
    private AccountAdapter accountAdapter;
    private SharedPreferences preferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {

        this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        accountDatabase = AccountDataSource.getInstance(this);

        accountAdapter = new AccountAdapter(ManageAccountsActivity.this, accountDatabase.getAllAccounts());

        setListAdapter(accountAdapter);
        super.onResume();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        setActiveAccount(id);
        super.onListItemClick(l, v, position, id);
    }

    private void setActiveAccount(long id) {

        final int selectedAccountId = (int)id;

        final SharedPreferences.Editor editor = this.preferences.edit();
        editor.putInt(InternalString.ACTIVE_ACCOUNT, selectedAccountId);
        editor.apply();
    }

    private int getActiveAccount() {
        return this.preferences.getInt(InternalString.ACTIVE_ACCOUNT, -1);
    }
}