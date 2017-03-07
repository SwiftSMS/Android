package org.swiftsms.sms;

import android.app.Notification;
import android.app.NotificationManager;
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
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;

import org.apache.commons.lang3.StringUtils;
import org.swiftsms.views.ConversationsActivity;
import org.swiftsms.views.ThreadActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;
import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.DATE;
import static android.provider.Telephony.TextBasedSmsColumns.DATE_SENT;
import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS;
import static android.provider.Telephony.TextBasedSmsColumns.TYPE;
import static android.support.v4.app.NotificationCompat.MessagingStyle;

public class SmsReceiver extends BroadcastReceiver {

    private static final String GROUP_KEY = "swiftsms";
    public static final int SUMMARY_ID = 84;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        for (final SmsMessage sms : messages) {
            writeSmsToProvider(context, sms);
            displayNotification(context, sms);
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

        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    private void displayNotification(final Context context, final SmsMessage sms) {
        final Notification notification = buildMessageNotification(context, sms);
        final Notification summary = buildSummaryNotification(context);
        final int id = Integer.parseInt(StringUtils.substring(sms.getDisplayOriginatingAddress(), -9));

        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, notification);
        manager.notify(SUMMARY_ID, summary);
    }

    private Notification buildMessageNotification(final Context context, final SmsMessage sms) {
        final Intent intent = new Intent(context, ThreadActivity.class)
                .putExtra(ADDRESS, sms.getOriginatingAddress());
        final PendingIntent pIntent = TaskStackBuilder.create(context)
                .addParentStack(ConversationsActivity.class)
                .addNextIntent(intent)
                .getPendingIntent(0, FLAG_UPDATE_CURRENT);

        return new Builder(context)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(sms.getOriginatingAddress())
                .setContentText(sms.getMessageBody())
                .setSubText(String.valueOf(sms.getTimestampMillis()))
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY)
                .setStyle(buildMessageStyle(context, sms))
                .build();
    }

    private MessagingStyle buildMessageStyle(final Context context, final SmsMessage sms) {
        final List<Message> messages = getUnreadMessages(context, sms.getOriginatingAddress());
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

    private Notification buildSummaryNotification(final Context context) {
        final Intent sIntent = new Intent(context, ConversationsActivity.class);
        final PendingIntent spIntent = TaskStackBuilder.create(context)
                .addNextIntent(sIntent)
                .getPendingIntent(0, FLAG_UPDATE_CURRENT);

        return new Builder(context)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setAutoCancel(true)
                .setContentIntent(spIntent)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .build();
    }
}
