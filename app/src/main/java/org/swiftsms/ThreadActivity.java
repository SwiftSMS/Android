package org.swiftsms;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.DATE;
import static android.provider.Telephony.TextBasedSmsColumns.THREAD_ID;

public class ThreadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        final String threadId = getIntent().getExtras().getString(THREAD_ID);

        final List<String> messages = getAllMessagesForThread(this, threadId);

        final ListView view = (ListView) findViewById(R.id.thread_messages);
        view.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages));
    }

    public List<String> getAllMessagesForThread(final Context context, final String threadId) {
        final List<String> messages = new ArrayList<>();

        final ContentResolver cr = context.getContentResolver();
        final Cursor c = cr.query(CONTENT_URI, new String[]{BODY, DATE}, "thread_id=?", new String[]{threadId}, null);
        if (c != null) {
            int totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    final String message = c.getString(c.getColumnIndexOrThrow(BODY));
                    final Date date = new Date(c.getLong(c.getColumnIndexOrThrow(DATE)));

                    messages.add(message);

                    c.moveToNext();
                }
            }
            c.close();
        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }

        return messages;
    }
}
