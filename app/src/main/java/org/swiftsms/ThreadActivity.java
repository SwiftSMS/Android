package org.swiftsms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import static android.provider.Telephony.TextBasedSmsColumns.THREAD_ID;

public class ThreadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        final String threadId = getIntent().getExtras().getString(THREAD_ID);

        final TextView view = (TextView) findViewById(R.id.textView);
        view.setText(String.format("The thread is: %s", threadId));
    }
}
