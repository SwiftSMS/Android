package org.swiftsms.sms;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import static android.provider.Telephony.TextBasedSmsColumns.STATUS;
import static android.provider.Telephony.TextBasedSmsColumns.STATUS_COMPLETE;
import static org.swiftsms.views.listeners.SendButtonListener.EXTRA_URI;

public class DeliveredIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Uri uri = Uri.parse(intent.getStringExtra(EXTRA_URI));

        final ContentValues values = new ContentValues();
        values.put(STATUS, STATUS_COMPLETE);
        context.getContentResolver().update(uri, values, null, null);
    }

}
