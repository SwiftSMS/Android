package org.swiftsms.sms.loaders;

import android.content.Context;
import android.database.Cursor;

import org.swiftsms.models.Conversation;

import java.util.Date;

import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;
import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.DATE;
import static android.provider.Telephony.TextBasedSmsColumns.THREAD_ID;

public class ConversationLoader extends ContentResolverLoader<Conversation> {

    private static final String[] PROJECTION = {"DISTINCT thread_id", ADDRESS, BODY, DATE};
    private static final String SELECTION = "address IS NOT NULL) GROUP BY (address";

    public ConversationLoader(final Context context) {
        super(context, CONTENT_URI, PROJECTION, SELECTION, null, null);
    }

    @Override
    protected Conversation parseCursor(final Cursor c) {
        final String number = c.getString(c.getColumnIndexOrThrow(ADDRESS));
        final String message = c.getString(c.getColumnIndexOrThrow(BODY));
        final Date date = new Date(c.getLong(c.getColumnIndexOrThrow(DATE)));
        final String threadId = c.getString(c.getColumnIndexOrThrow(THREAD_ID));

        return new Conversation(number, message, date, threadId);
    }
}
