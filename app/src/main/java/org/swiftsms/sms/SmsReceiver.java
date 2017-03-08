package org.swiftsms.sms;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationCompat.MessagingStyle.Message;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;

import org.apache.commons.lang3.StringUtils;
import org.swiftsms.views.ThreadActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.app.Notification.DEFAULT_ALL;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;
import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.DATE;
import static android.provider.Telephony.TextBasedSmsColumns.DATE_SENT;
import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
import static android.provider.Telephony.TextBasedSmsColumns.TYPE;
import static android.support.v4.app.NotificationCompat.MessagingStyle;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        final Message message = buildConcatenatedMessage(messages);

        writeSmsToProvider(context, message);
        displayNotification(context, message);
    }

    private Message buildConcatenatedMessage(final SmsMessage[] messages) {
        String sender = null;
        String message = "";
        long timestamp = 0;

        for (final SmsMessage sms : messages) {
            sender = sms.getOriginatingAddress();
            message += sms.getMessageBody();
            timestamp = sms.getTimestampMillis();
        }

        return new Message(message, timestamp, sender);
    }

    private Uri writeSmsToProvider(final Context context, final Message message) {
        final ContentValues values = new ContentValues();
        values.put(ADDRESS, String.valueOf(message.getSender()));
        values.put(BODY, String.valueOf(message.getText()));
        values.put(TYPE, MESSAGE_TYPE_INBOX);
        values.put(DATE, System.currentTimeMillis());
        values.put(DATE_SENT, message.getTimestamp());

        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    private void displayNotification(final Context context, final Message sms) {
        final Notification notification = buildMessageNotification(context, sms);
        final int id = Integer.parseInt(StringUtils.substring(String.valueOf(sms.getSender()), -9));

        NotificationManagerCompat.from(context).notify(id, notification);
    }

    private Notification buildMessageNotification(final Context context, final Message sms) {
        final List<Message> messages = getUnreadMessages(context, String.valueOf(sms.getSender()));

        return new Builder(context)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(sms.getSender())
                .setContentText(sms.getText())
                .setContentIntent(buildPendingIntent(context, sms))
                .setDefaults(DEFAULT_ALL)
                .setAutoCancel(true)
                .setNumber(messages.size())
                .setStyle(buildMessageStyle(messages))
                .build();
    }

    private PendingIntent buildPendingIntent(final Context context, final Message sms) {
        final Intent intent = new Intent(context, ThreadActivity.class)
                .putExtra(ADDRESS, sms.getSender());

        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0, FLAG_UPDATE_CURRENT);
    }

    private MessagingStyle buildMessageStyle(final List<Message> messages) {
        final MessagingStyle style = new MessagingStyle("Reply");
        style.getMessages().addAll(messages);

        return style;
    }

    private List<Message> getUnreadMessages(final Context context, final String address) {
        final List<Message> unreadMessages = new ArrayList<>();
        final ContentResolver cr = context.getContentResolver();
        final Cursor cursor = cr.query(CONTENT_URI, new String[]{BODY, DATE}, "address=? and read!=?", new String[]{address, "1"}, "date_sent DESC LIMIT 5");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                unreadMessages.add(parseMessage(cursor, address));
            }
            cursor.close();
        }

        Collections.reverse(unreadMessages);
        return unreadMessages;
    }

    private Message parseMessage(final Cursor cursor, final String address) {
        final String message = cursor.getString(cursor.getColumnIndexOrThrow(BODY));
        final Date date = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DATE)));

        return new Message(message, date.getTime(), address);
    }

}
