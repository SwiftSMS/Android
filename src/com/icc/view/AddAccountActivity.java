package com.icc.view;

import static com.icc.InternalString.PREFS_KEY;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.icc.net.Operator;
import com.icc.net.OperatorFactory;
import com.icc.tasks.VerifyTask;

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
		this.preferences = this.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_add_account);

		final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_cancel, null);
		// Show the custom action bar view and hide the normal Home icon and title.
		final ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		this.textAccName = (TextView) this.findViewById(R.id.text_acc_name);
		this.textAccNumber = (TextView) this.findViewById(R.id.text_acc_number);
		this.textAccPassword = (TextView) this.findViewById(R.id.text_acc_password);
		this.textViewNetwork = (TextView) this.findViewById(R.id.text_selected_network);
		this.checkActiveAccount = (CheckBox) this.findViewById(R.id.checkBox_active_acc);

		this.handleNetworkSelection();
	}

	@Override
	protected void onResume() {
		this.accountDatabase = AccountDataSource.getInstance(this);
		super.onResume();
	}

	private void handleNetworkSelection() {
		final Dialog dialog = new Dialog(this);
		final ListView listView = new ListView(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
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
		final Account account = this.makeAccountFromUI();
		final int successfullyAddedId = this.accountDatabase.addAccount(account);

		if (successfullyAddedId != FAILED_DB_ADD) {
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

	/**
	 * This method pulls data from the UI components and tries to construct an account object using the details.
	 * 
	 * @return An {@link Account} using the details entered on the UI.
	 */
	private Account makeAccountFromUI() {
		final String number = this.textAccNumber.getText().toString();
		final String password = this.textAccPassword.getText().toString();

		final String numberLast4Digits = number.substring(number.length() - Math.min(4, number.length()));
		final String defaultAccName = this.selectedNetwork + " (" + numberLast4Digits + ")";
		final String enteredAccName = this.textAccName.getText().toString();
		final String accountName = enteredAccName.isEmpty() ? defaultAccName : enteredAccName;

		return new Account(number, accountName, password, this.selectedNetwork);
	}

	public void verifyAccount(final View view) {
		final Account account = this.makeAccountFromUI();
		final Operator operator = OperatorFactory.getOperator(account);
		new VerifyTask(this, operator).execute();
	}
}