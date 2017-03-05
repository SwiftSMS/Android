package org.swiftsms.sms.loaders;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public abstract class ContentResolverLoader<T> extends AsyncTaskLoader<List<T>> {

    private final Uri mContentUri;
    private final String[] mProjection;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final String mSortOrder;

    private List<T> mData;
    private ForceLoadContentObserver mObserver;

    public ContentResolverLoader(final Context context, final Uri contentUri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        super(context);
        mContentUri = contentUri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        }

        if (mObserver == null) {
            mObserver = new ForceLoadContentObserver();
            getContext().getContentResolver().registerContentObserver(mContentUri, true, mObserver);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    public List<T> loadInBackground() {
        mData = getAllMessages();
        return mData;
    }

    private List<T> getAllMessages() {
        final List<T> messages = new ArrayList<>();

        final ContentResolver cr = getContext().getContentResolver();
        final Cursor cursor = cr.query(mContentUri, mProjection, mSelection, mSelectionArgs, mSortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                messages.add(parseCursor(cursor));
            }
            cursor.close();
        }

        return messages;
    }

    protected abstract T parseCursor(final Cursor c);

    @Override
    public void deliverResult(final List<T> data) {
        if (isReset()) {
            releaseResources(data);
            return;
        }

        final List<T> oldData = mData;
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
        cancelLoad();
    }

    @Override
    protected void onReset() {
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
    public void onCanceled(final List<T> data) {
        super.onCanceled(data);
        releaseResources(data);
    }

    private void releaseResources(final List<T> data) {
        data.clear();
    }
}
