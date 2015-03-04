package com.swift.ui.view;

import static com.swift.InternalString.OPERATOR;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.swift.model.Network;
import com.swift.ui.view.util.NetworkSelectionAdapter;

public class NetworkSelectionActivity extends ListActivity {

	private static final int ADD_ACCOUNT_REQUESTCODE = 456;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setListAdapter(new NetworkSelectionAdapter(this.getActionBarThemedContext()));
		this.setResult(RESULT_CANCELED);
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that simply returns the {@link android.app.Activity}
	 * if <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContext() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return this.getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (requestCode == ADD_ACCOUNT_REQUESTCODE) {
			if (resultCode == RESULT_OK) {
				this.setResult(RESULT_OK);
				this.finish();
			}
		}
	}

	@Override
	protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final Intent intent = new Intent(this, AddAccountActivity.class);
		final Network network = (Network) this.getListAdapter().getItem(position);
		intent.putExtra(OPERATOR, network.ordinal());
		this.startActivityForResult(intent, ADD_ACCOUNT_REQUESTCODE);
	}
}