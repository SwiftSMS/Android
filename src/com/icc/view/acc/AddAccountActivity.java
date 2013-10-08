package com.icc.view.acc;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.icc.R;
import com.icc.acc.Account;
import com.icc.acc.Network;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;

/**
 * Activity to handle adding a new operator account to ICC
 * 
 * @author Rob Powell
 * @version 1.0
 */
public class AddAccountActivity extends Activity {

	private IAccountDatabase accountDatabase;
	private TextView textView;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_account_activity);

		this.textView = (TextView) this.findViewById(R.id.main_text);
	}

	@Override
	protected void onResume() {

		this.accountDatabase = AccountDataSource.getInstance(this);

		final Account account = new Account("00353862101112", "My O2 Account", "hello", Network.O2);

		final boolean addedSuccessfully = this.accountDatabase.addAccount(account);

		if (addedSuccessfully) {
			final List<Account> accountList = this.accountDatabase.getAllAccounts();
			this.textView.setText(accountList.get(0).getAccountName());
		}

		super.onResume();
	}
}