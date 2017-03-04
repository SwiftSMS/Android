package org.swiftsms.views;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.swiftsms.views.adapters.MessageAdapter;
import org.swiftsms.R;
import org.swiftsms.views.listeners.SendButtonListener;
import org.swiftsms.views.listeners.SendButtonTextWatcher;
import org.swiftsms.models.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.Telephony.Sms.BODY;
import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.Sms.DATE;
import static android.provider.Telephony.Sms.THREAD_ID;
import static android.provider.Telephony.Sms.TYPE;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;

public class ThreadActivity extends AppCompatActivity {

    private String address;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        address = getIntent().getExtras().getString(ADDRESS);

        setupMessageHistory();
        setupSendButton();
    }

    private void setupMessageHistory() {
        final String threadId = getIntent().getExtras().getString(THREAD_ID);
        final List<Message> messages = getAllMessagesForThread(this, threadId);

        final ListView view = (ListView) findViewById(R.id.thread_messages);
        view.setAdapter(new MessageAdapter(this, messages));
    }

    private void setupSendButton() {
        final ImageButton button = (ImageButton) findViewById(R.id.send_button);
        final EditText message = (EditText) findViewById(R.id.compose_message_edit);

        button.setEnabled(false);
        button.setClickable(false);
        button.setOnClickListener(new SendButtonListener(this, message, address));
        message.addTextChangedListener(new SendButtonTextWatcher(button));
    }

    public List<Message> getAllMessagesForThread(final Context context, final String threadId) {
        final List<Message> messages = new ArrayList<>();

        final ContentResolver cr = context.getContentResolver();
        final Cursor c = cr.query(CONTENT_URI, new String[]{BODY, DATE, TYPE}, "thread_id=?", new String[]{threadId}, "date ASC");
        if (c != null) {
            int totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    final String message = c.getString(c.getColumnIndexOrThrow(BODY));
                    final Date date = new Date(c.getLong(c.getColumnIndexOrThrow(DATE)));
                    final int type = c.getInt(c.getColumnIndexOrThrow(TYPE));

                    messages.add(new Message(message, date, type));

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
