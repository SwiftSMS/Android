package org.swiftsms.sms.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import org.swiftsms.models.Message;

import java.util.Date;

import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.DATE;
import static android.provider.Telephony.TextBasedSmsColumns.TYPE;

public class MessageLoader extends ContentResolverLoader<Message> {

    private static final Uri CONTENT_URI = Telephony.Sms.CONTENT_URI;
    private static final String[] PROJECTION = {BODY, DATE, TYPE};
    private static final String SELECTION = "address=?";
    private static final String SORT_ORDER = "date ASC";

    public MessageLoader(final Context context, final String address) {
        super(context, CONTENT_URI, PROJECTION, SELECTION, new String[]{address}, SORT_ORDER);
    }

    @Override
    protected Message parseCursor(final Cursor c) {
        final String message = c.getString(c.getColumnIndexOrThrow(BODY));
        final Date date = new Date(c.getLong(c.getColumnIndexOrThrow(DATE)));
        final int type = c.getInt(c.getColumnIndexOrThrow(TYPE));

        return new Message(message, date, type);

    }
}
