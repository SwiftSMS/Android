package org.swiftsms.views.listeners;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.swiftsms.sms.DeliveredIntentReceiver;
import org.swiftsms.sms.SentIntentReceiver;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by sean on 04/03/17.
 */
public class SendButtonListener implements View.OnClickListener {

    private final int SENT_REQ_CODE = 367;
    private final int DELIVERED_REQ_CODE = 645;

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

        SmsManager.getDefault().sendTextMessage(address, null, message, buildSentIntent(), buildDeliveredIntent());
    }

    private PendingIntent buildSentIntent() {
        final Intent intent = new Intent(context, SentIntentReceiver.class);
        return PendingIntent.getBroadcast(context, SENT_REQ_CODE, intent, FLAG_UPDATE_CURRENT);
    }

    private PendingIntent buildDeliveredIntent() {
        final Intent intent = new Intent(context, DeliveredIntentReceiver.class);
        return PendingIntent.getBroadcast(context, DELIVERED_REQ_CODE, intent, FLAG_UPDATE_CURRENT);
    }
}
