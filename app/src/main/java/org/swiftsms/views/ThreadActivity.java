package org.swiftsms.views;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.swiftsms.R;
import org.swiftsms.models.Message;
import org.swiftsms.sms.loaders.MessageLoader;
import org.swiftsms.views.adapters.MessageAdapter;
import org.swiftsms.views.listeners.SendButtonListener;
import org.swiftsms.views.listeners.SendButtonTextWatcher;

import java.util.List;

import static android.provider.Telephony.Sms.THREAD_ID;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;

public class ThreadActivity extends AppCompatActivity implements LoaderCallbacks<List<Message>> {

    private String address;
    private ListView listView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        address = getIntent().getExtras().getString(ADDRESS);
        listView = (ListView) findViewById(R.id.thread_messages);

        setTitle(address);
        setupMessageHistory();
        setupSendButton();
    }

    private void setupMessageHistory() {
        getLoaderManager().initLoader(743, null, this);
    }

    private void setupSendButton() {
        final ImageButton button = (ImageButton) findViewById(R.id.send_button);
        final EditText message = (EditText) findViewById(R.id.compose_message_edit);

        button.setEnabled(false);
        button.setClickable(false);
        button.setOnClickListener(new SendButtonListener(this, message, address));
        message.addTextChangedListener(new SendButtonTextWatcher(button));
    }

    @Override
    public Loader<List<Message>> onCreateLoader(int id, Bundle args) {
        final String threadId = getIntent().getExtras().getString(THREAD_ID);
        return new MessageLoader(this, threadId);
    }

    @Override
    public void onLoadFinished(Loader<List<Message>> loader, List<Message> data) {
        listView.setAdapter(new MessageAdapter(this, data));
    }

    @Override
    public void onLoaderReset(Loader<List<Message>> loader) {
        listView.setAdapter(null);
    }
}
