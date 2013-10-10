package com.icc.view.acc;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.icc.InternalString;
import com.icc.R;
import com.icc.acc.Account;
import com.icc.acc.Network;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;

import java.util.List;

/**
 * Activity to handle adding a new operator account to ICC
 * 
 * @author Rob Powell
 * @version 1.0
 */
public class AddAccountActivity extends Activity {

	private IAccountDatabase accountDatabase;
	private TextView textAccNumber, textAccName, textAccPassword;
    private Spinner networkSpinner;
    private SharedPreferences preferences;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
        preferences = this.getSharedPreferences(InternalString.PREFS_KEY, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_account_activity);

        AddAccountActivity.this.textAccName = (TextView)this.findViewById(R.id.text_acc_name);
        AddAccountActivity.this.textAccNumber = (TextView) this.findViewById(R.id.text_acc_number);
        AddAccountActivity.this.textAccPassword = (TextView) this.findViewById(R.id.text_acc_password);
	}

	@Override
	protected void onResume() {

        AddAccountActivity.this.networkSpinner = (Spinner)findViewById(R.id.spinner_acc_network);
        AddAccountActivity.this.networkSpinner.setAdapter(getNetworkAdapter());

        AddAccountActivity.this.accountDatabase = AccountDataSource.getInstance(this);

		super.onResume();
	}

    private ArrayAdapter getNetworkAdapter() {

        return new ArrayAdapter(this, android.R.layout.simple_list_item_1, Network.values());
    }

    public void addAccount(final View view) {

        final String number = AddAccountActivity.this.textAccNumber.getText().toString();
        final String accountName = AddAccountActivity.this.textAccName.getText().toString();
        final String password = AddAccountActivity.this.textAccPassword.getText().toString();
        final Network network = (Network)AddAccountActivity.this.networkSpinner.getSelectedItem();

        final Account account = new Account(number, accountName, password, network);

        final boolean successfullyAdded = this.accountDatabase.addAccount(account);

        if(successfullyAdded) {
            Toast.makeText(this,"Accounted Added",Toast.LENGTH_SHORT).show();
            preferences.edit().putInt(InternalString.ACCOUNT_LATEST, getLatestAccount().getId());
        }
    }

    private Account getLatestAccount() {

        final List<Account> accounts = accountDatabase.getAllAccounts();

        Account latestAccount = null;
        long createdTime = 0;

        for(Account account : accounts) {
            if(account.getTimeStamp() > createdTime){
                latestAccount = account;
                createdTime = account.getTimeStamp();
            }
        }

        return latestAccount;
    }
}