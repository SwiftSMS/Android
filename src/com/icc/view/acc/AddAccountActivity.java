package com.icc.view.acc;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.icc.R;
import com.icc.acc.Account;
import com.icc.acc.Operator;
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
    private TextView textView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_account_activity);

        textView = (TextView)findViewById(R.id.main_text);
    }

    @Override
    protected void onResume() {

        accountDatabase = AccountDataSource.getInstance(this);

        final Account account = new Account("00353862101112","My O2 Account","hello", Operator.O2);

        final boolean addedSuccessfully = accountDatabase.addAccount(account);

        if(addedSuccessfully) {
            final List<Account> accountList = accountDatabase.getAllAccounts();
            textView.setText(accountList.get(0).getAccountName());
        }

        super.onResume();
    }
}