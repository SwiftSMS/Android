package com.icc.view;

import static com.icc.InternalString.OPERATOR;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.icc.R;
import com.icc.model.Network;

public class NetworkSelectionActivity extends Activity implements OnItemClickListener {

	private static final int ADD_ACCOUNT_REQUESTCODE = 456;
	private ListView listView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_network_selection);

		this.listView = (ListView) this.findViewById(R.id.list_network_selection_items);
		this.listView.setAdapter(new NetworkSelectionAdapter(this.getActionBarThemedContext()));
		this.listView.setOnItemClickListener(this);

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
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long itemId) {
		final Intent intent = new Intent(this, AddAccountActivity.class);
		final Network network = (Network) this.listView.getItemAtPosition(position);
		intent.putExtra(OPERATOR, network.toString());
		this.startActivityForResult(intent, ADD_ACCOUNT_REQUESTCODE);
	}
}