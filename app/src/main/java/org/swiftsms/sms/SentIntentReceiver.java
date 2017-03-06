package org.swiftsms.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

import static android.provider.Telephony.TextBasedSmsColumns.ERROR_CODE;
import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_FAILED;
import static android.provider.Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS_FAILED;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS_PENDING;
import static android.provider.Telephony.TextBasedSmsColumns.TYPE;
import static org.swiftsms.views.listeners.SendButtonListener.EXTRA_URI;

public class SentIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Uri uri = Uri.parse(intent.getStringExtra(EXTRA_URI));

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                markSmsAsSuccessful(context, uri);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                markSmsAsFailed(context, uri, "Error sending message, no service");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                markSmsAsFailed(context, uri, "Error sending message, radio is off");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                markSmsAsFailed(context, uri, "Error sending message, no PDU provided");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                final String error = intent.getStringExtra("errorCode");
                markSmsAsFailed(context, uri, error != null ? error : "Error sending message, no idea why");
                break;
            default:
                markSmsAsFailed(context, uri, "Error sending message, unknown result code");
        }
    }

    private int markSmsAsSuccessful(final Context context, final Uri uri) {
        return updateSmsWithStatus(context, uri, MESSAGE_TYPE_SENT, STATUS_PENDING, null);
    }

    private int markSmsAsFailed(final Context context, final Uri uri, final String error) {
        return updateSmsWithStatus(context, uri, MESSAGE_TYPE_FAILED, STATUS_FAILED, error);
    }

    private int updateSmsWithStatus(final Context context, final Uri uri, final int type, final int status, final String error) {
        final ContentValues values = new ContentValues();
        values.put(TYPE, type);
        values.put(STATUS, status);
        values.put(ERROR_CODE, error);

        return context.getContentResolver().update(uri, values, null, null);
    }
}
