package com.icc;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.icc.net.Vodafone;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(final String... params) {
				final Vodafone m = new Vodafone("user", "pass");
				return m.login();
			}

			@Override
			protected void onPostExecute(final String result) {
				final TextView view = (TextView) MainActivity.this.findViewById(R.id.text_compose_message);
				view.setText(result);
			}
		}.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
