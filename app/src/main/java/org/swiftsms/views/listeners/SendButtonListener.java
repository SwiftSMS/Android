package org.swiftsms.views.listeners;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import org.swiftsms.sms.DeliveredIntentReceiver;
import org.swiftsms.sms.SentIntentReceiver;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;
import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS_PENDING;
import static android.provider.Telephony.TextBasedSmsColumns.TYPE;

/**
 * Created by sean on 04/03/17.
 */
public class SendButtonListener implements OnClickListener {

    public static final String EXTRA_URI = "uri";
    private static final int SENT_REQ_CODE = 367;
    private static final int DELIVERED_REQ_CODE = 645;

    private final Context context;
    private final EditText editText;
    private final String address;

    public SendButtonListener(final Context context, final EditText editText, final String address) {
        this.context = context;
        this.editText = editText;
        this.address = address;
    }

    @Override
    public void onClick(final View view) {
        final String message = editText.getText().toString();
        editText.setText(null);

        final Uri uri = writeSmsToProvider(message);
        SmsManager.getDefault().sendTextMessage(address, null, message, buildSentIntent(uri), buildDeliveredIntent(uri));
    }

    private Uri writeSmsToProvider(final String message) {
        final ContentValues values = new ContentValues();
        values.put(ADDRESS, address);
        values.put(BODY, message);
        values.put(TYPE, MESSAGE_TYPE_SENT);
        values.put(STATUS, STATUS_PENDING);

        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    private PendingIntent buildSentIntent(final Uri uri) {
        final Intent intent = new Intent(context, SentIntentReceiver.class);
        intent.putExtra(EXTRA_URI, String.valueOf(uri));
        return PendingIntent.getBroadcast(context, SENT_REQ_CODE, intent, FLAG_UPDATE_CURRENT);
    }

    private PendingIntent buildDeliveredIntent(final Uri uri) {
        final Intent intent = new Intent(context, DeliveredIntentReceiver.class);
        intent.putExtra(EXTRA_URI, String.valueOf(uri));
        return PendingIntent.getBroadcast(context, DELIVERED_REQ_CODE, intent, FLAG_UPDATE_CURRENT);
    }
}
