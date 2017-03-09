package org.swiftsms.views;

import android.Manifest;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.swiftsms.NetworkSelectionActivity;
import org.swiftsms.R;
import org.swiftsms.models.Conversation;
import org.swiftsms.sms.loaders.ConversationLoader;
import org.swiftsms.views.adapters.ConversationAdapter;
import org.swiftsms.views.listeners.ConversationItemClickListener;

import java.util.List;

public class ConversationsActivity extends AppCompatActivity implements LoaderCallbacks<List<Conversation>> {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 135;

    private static final String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS
    };

    private ListView listView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        listView = (ListView) findViewById(R.id.content_conversations);
        listView.setOnItemClickListener(new ConversationItemClickListener(this));

        if (verifyPermissions()) {
            showConversationsInList();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String permissions[], final int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showConversationsInList();
                }
            }
        }
    }

    private boolean verifyPermissions() {
        final int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void showConversationsInList() {
        getLoaderManager().initLoader(9835, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_add_account:
                openAddAccountActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openAddAccountActivity() {
        final Intent intent = new Intent(this, NetworkSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<List<Conversation>> onCreateLoader(final int id, final Bundle args) {
        return new ConversationLoader(this);
    }

    @Override
    public void onLoadFinished(final Loader<List<Conversation>> loader, final List<Conversation> conversations) {
        listView.setAdapter(new ConversationAdapter(this, conversations));
    }

    @Override
    public void onLoaderReset(final Loader<List<Conversation>> loader) {
        listView.setAdapter(null);
    }
}
