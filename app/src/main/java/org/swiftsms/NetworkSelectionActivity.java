package org.swiftsms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

import org.swiftsms.views.adapters.NetworkSelectionAdapter;

public class NetworkSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_selection);
        setTitle("Select network");

        final GridView gridView = (GridView) findViewById(R.id.networks);
        gridView.setAdapter(new NetworkSelectionAdapter(this));
    }
}
