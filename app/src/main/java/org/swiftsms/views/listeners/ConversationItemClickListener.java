package org.swiftsms.views.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import org.swiftsms.models.Conversation;
import org.swiftsms.views.ThreadActivity;

import static android.provider.Telephony.TextBasedSmsColumns.THREAD_ID;

/**
 * Created by sean on 03/03/17.
 */
public class ConversationItemClickListener implements AdapterView.OnItemClickListener {

    private final Context context;

    public ConversationItemClickListener(final Context context) {
        this.context = context;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final Conversation conversation = (Conversation) parent.getItemAtPosition(position);

        final Intent intent = new Intent(context, ThreadActivity.class);
        intent.putExtra(THREAD_ID, conversation.threadId);
        context.startActivity(intent);
    }
}
