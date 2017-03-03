package org.swiftsms;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.Telephony.Sms.ADDRESS;
import static android.provider.Telephony.Sms.BODY;
import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.Sms.DATE;
import static android.provider.Telephony.Sms.THREAD_ID;

public class ConversationsActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 135;

    private static final String[] permissions = {
            Manifest.permission.READ_SMS
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
        final ConversationAdapter adapter = new ConversationAdapter(this, getAllConversations(this));

        listView.setAdapter(adapter);
    }

    public List<Conversation> getAllConversations(final Context context) {
        final List<Conversation> conversations = new ArrayList<>();

        final ContentResolver cr = context.getContentResolver();
        final Cursor c = cr.query(CONTENT_URI, new String[]{"DISTINCT thread_id", ADDRESS, BODY, DATE}, "address IS NOT NULL) GROUP BY (address", null, null);
        if (c != null) {
            int totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    final String number = c.getString(c.getColumnIndexOrThrow(ADDRESS));
                    final String message = c.getString(c.getColumnIndexOrThrow(BODY));
                    final Date date = new Date(c.getLong(c.getColumnIndexOrThrow(DATE)));
                    final String threadId = c.getString(c.getColumnIndexOrThrow(THREAD_ID));

                    conversations.add(new Conversation(number, message, date, threadId));

                    c.moveToNext();
                }
            }
            c.close();
        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }

        return conversations;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
