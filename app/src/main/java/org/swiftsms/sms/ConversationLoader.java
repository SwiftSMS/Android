package org.swiftsms.sms;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.swiftsms.models.Conversation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.Telephony.Sms.CONTENT_URI;
import static android.provider.Telephony.TextBasedSmsColumns.ADDRESS;
import static android.provider.Telephony.TextBasedSmsColumns.BODY;
import static android.provider.Telephony.TextBasedSmsColumns.DATE;
import static android.provider.Telephony.TextBasedSmsColumns.THREAD_ID;

public class ConversationLoader extends AsyncTaskLoader<List<Conversation>> {

    private static final String[] PROJECTION = {"DISTINCT thread_id", ADDRESS, BODY, DATE};
    private static final String SELECTION = "address IS NOT NULL) GROUP BY (address";

    private List<Conversation> mData;
    private ForceLoadContentObserver mObserver;

    public ConversationLoader(final Context context) {
        super(context);
        Log.i("TAG", "Loader created");
    }

    @Override
    protected void onStartLoading() {
        Log.i("TAG", "Loader onStartLoading");
        if (mData != null) {
            deliverResult(mData);
        }

        if (mObserver == null) {
            mObserver = new ForceLoadContentObserver();
            getContext().getContentResolver().registerContentObserver(CONTENT_URI, true, mObserver);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    public List<Conversation> loadInBackground() {
        Log.i("TAG", "Loader start loading in background");
        mData = getAllConversations();
        return mData;
    }

    private List<Conversation> getAllConversations() {
        final List<Conversation> conversations = new ArrayList<>();

        final ContentResolver cr = getContext().getContentResolver();
        final Cursor cursor = cr.query(CONTENT_URI, PROJECTION, SELECTION, null, null);
        Log.i("TAG", "Cursor=" + cursor);
        if (cursor != null) {
            Log.i("TAG", "Cursor size=" + cursor.getCount());
            while (cursor.moveToNext()) {
                conversations.add(buildConversation(cursor));
            }
            cursor.close();
        }

        return conversations;
    }

    private Conversation buildConversation(final Cursor c) {
        final String number = c.getString(c.getColumnIndexOrThrow(ADDRESS));
        final String message = c.getString(c.getColumnIndexOrThrow(BODY));
        final Date date = new Date(c.getLong(c.getColumnIndexOrThrow(DATE)));
        final String threadId = c.getString(c.getColumnIndexOrThrow(THREAD_ID));

        return new Conversation(number, message, date, threadId);
    }

    @Override
    public void deliverResult(final List<Conversation> data) {
        Log.i("TAG", "Loader delivering result");
        if (isReset()) {
            releaseResources(data);
            return;
        }

        final List<Conversation> oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null && oldData != data) {
            releaseResources(data);
        }
    }

    @Override
    protected void onStopLoading() {
        Log.i("TAG", "Loader onStopLoading");
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.i("TAG", "Loader being reset/destroyed");
        onStopLoading();

        if (mData != null) {
            releaseResources(mData);
            mData = null;
        }

        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

    @Override
    public void onCanceled(final List<Conversation> data) {
        Log.i("TAG", "Loader cancel run");
        super.onCanceled(data);
        releaseResources(data);
    }

    private void releaseResources(final List<Conversation> data) {
        data.clear();
    }
}
