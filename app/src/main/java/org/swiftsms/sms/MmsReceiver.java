package org.swiftsms.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("TAG", "New MMS received, we REALLY should write this to the SMS Provider");
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
