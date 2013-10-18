package com.icc.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;

/**
 * @author Rob Powell
 */
public class ManageAccountsActivity extends Activity {

    private ListView listView;
    private IAccountDatabase accountDatabase;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        setContentView(listView);

        accountDatabase = AccountDataSource.getInstance(this);

        populateListView();
    }

    private void populateListView() {
        listView.setAdapter(new AccountAdapter(this, accountDatabase.getAllAccounts()));
    }
}