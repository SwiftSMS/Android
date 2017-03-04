package org.swiftsms.sms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class SentIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Log.i("TAG", "Message sent");
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Log.i("TAG", "Error sending message, no service");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Log.i("TAG", "Error sending message, radio is off");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Log.i("TAG", "Error sending message, no PDU provided??");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Log.i("TAG", "Error sending message, generic failure, error=" + intent.getExtras().getString("errorCode"));
                break;
            default:
                Log.i("TAG", "Error sending message, something else went wrong");
        }
    }
}
