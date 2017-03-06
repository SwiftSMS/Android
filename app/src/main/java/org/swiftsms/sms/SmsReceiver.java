package org.swiftsms.sms;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;
import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.DATE;
import static android.provider.Telephony.TextBasedSmsColumns.DATE_SENT;
import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS;
import static android.provider.Telephony.TextBasedSmsColumns.TYPE;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (final SmsMessage sms : messages) {
            writeSmsToProvider(context, sms);
        }
    }

    private Uri writeSmsToProvider(final Context context, final SmsMessage message) {
        final ContentValues values = new ContentValues();
        values.put(ADDRESS, message.getOriginatingAddress());
        values.put(BODY, message.getMessageBody());
        values.put(TYPE, MESSAGE_TYPE_INBOX);
        values.put(STATUS, message.getStatusOnIcc());
        values.put(DATE, System.currentTimeMillis());
        values.put(DATE_SENT, message.getTimestampMillis());
        // values.put(THREAD_ID, threadId);

        return context.getContentResolver().insert(CONTENT_URI, values);
    }
}
