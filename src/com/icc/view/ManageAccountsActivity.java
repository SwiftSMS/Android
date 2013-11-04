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

        this.preferences = this.getSharedPreferences(PREFS_KEY, MODE_PRIVATE);
        accountDatabase = AccountDataSource.getInstance(this);
    }

    @Override
    protected void onResume() {

        int activeAccountId = this.preferences.getInt(InternalString.ACTIVE_ACCOUNT, -1);

        accountAdapter = new AccountAdapter(ManageAccountsActivity.this, accountDatabase.getAllAccounts());

        setListAdapter(accountAdapter);
        super.onResume();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }
}