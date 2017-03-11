package org.swiftsms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.swiftsms.models.Network;
import org.swiftsms.views.adapters.NetworkSelectionAdapter;

import static org.swiftsms.InternalString.OPERATOR;

public class NetworkSelectionActivity extends AppCompatActivity {

    private static final int ADD_ACCOUNT_REQUEST_CODE = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_selection);
        setTitle("Select network");

        final GridView gridView = (GridView) findViewById(R.id.networks);
        gridView.setAdapter(new NetworkSelectionAdapter(this));
        gridView.setOnItemClickListener(new NetworkOnItemClickListener(this));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == ADD_ACCOUNT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                this.setResult(RESULT_OK);
                this.finish();
            }
        }
    }

    private static class NetworkOnItemClickListener implements AdapterView.OnItemClickListener {

        private final Activity context;

        public NetworkOnItemClickListener(Activity context) {
            this.context = context;
        }

        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            final Intent intent = new Intent(context, AddAccountActivity.class);
            final Network network = (Network) parent.getItemAtPosition(position);
            intent.putExtra(OPERATOR, network.ordinal());
            context.startActivityForResult(intent, ADD_ACCOUNT_REQUEST_CODE);
        }
    }
}
