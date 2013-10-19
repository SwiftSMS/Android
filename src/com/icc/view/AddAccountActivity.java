package com.icc.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.icc.InternalString;
import com.icc.R;
import com.icc.db.AccountDataSource;
import com.icc.db.IAccountDatabase;
import com.icc.model.Account;
import com.icc.model.Network;

/**
 * Activity to handle adding a new operator account to ICC
 * 
 * @author Rob Powell
 * @version 1.3
 */
public class AddAccountActivity extends Activity {

	private static int FAILED_DB_ADD = -1;
	private IAccountDatabase accountDatabase;
	private TextView textAccNumber, textAccName, textAccPassword, textViewNetwork;
	private CheckBox checkActiveAccount;
	private SharedPreferences preferences;
	private Network selectedNetwork = null;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		this.preferences = this.getSharedPreferences(InternalString.PREFS_KEY, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.add_account_activity);

		AddAccountActivity.this.textAccName = (TextView) this.findViewById(R.id.text_acc_name);
		AddAccountActivity.this.textAccNumber = (TextView) this.findViewById(R.id.text_acc_number);
		AddAccountActivity.this.textAccPassword = (TextView) this.findViewById(R.id.text_acc_password);
		AddAccountActivity.this.textViewNetwork = (TextView) this.findViewById(R.id.text_selected_network);
		AddAccountActivity.this.checkActiveAccount = (CheckBox) this.findViewById(R.id.checkBox_active_acc);

		this.handleNetworkSelection();
	}

	@Override
	protected void onResume() {

		AddAccountActivity.this.accountDatabase = AccountDataSource.getInstance(this);

		super.onResume();
	}

	private void handleNetworkSelection() {

		final Dialog dialog = new Dialog(AddAccountActivity.this);
		final ListView listView = new ListView(this);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
				AddAccountActivity.this.selectedNetwork = Network.values()[i];
				AddAccountActivity.this.textViewNetwork.setText(AddAccountActivity.this.selectedNetwork + " Network");
				dialog.dismiss();
			}
		});
		listView.setAdapter(this.getNetworkAdapter());
		dialog.setTitle("Select Network");
		dialog.setContentView(listView);
		dialog.show();
	}

	private ArrayAdapter<Network> getNetworkAdapter() {
		return new ArrayAdapter<Network>(this, android.R.layout.simple_list_item_1, Network.values());
	}

	public void addAccount(final View view) {
		final String number = AddAccountActivity.this.textAccNumber.getText().toString();
		final String password = AddAccountActivity.this.textAccPassword.getText().toString();

		final String defaultAccName = this.selectedNetwork + " (" + number.substring(number.length() - 4) + ")";
		final String enteredAccName = AddAccountActivity.this.textAccName.getText().toString();
		final String accountName = enteredAccName.equals("") ? defaultAccName : enteredAccName;

		final Account account = new Account(number, accountName, password, this.selectedNetwork);
		final int successfullyAddedId = this.accountDatabase.addAccount(account);

		if (successfullyAddedId != AddAccountActivity.FAILED_DB_ADD) {
			Toast.makeText(this, "Accounted Added", Toast.LENGTH_SHORT).show();
			final Editor editor = this.preferences.edit();
			editor.putInt(InternalString.LATEST_ACCOUNT, successfullyAddedId);

			if (this.checkActiveAccount.isChecked()) {
				editor.putInt(InternalString.ACTIVE_ACCOUNT, successfullyAddedId);
			}
			editor.commit();
		}

		this.setResult(Activity.RESULT_OK);
		this.finish();
	}
}