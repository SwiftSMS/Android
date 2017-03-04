package org.swiftsms.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeliveredIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("TAG", "Message delivered");
        Log.i("TAG", "We should update the SMS DB");
    }
}
