package com.icc.utils;

import com.icc.R;

/**
 * Notifications for ICC Activities
 *
 * @author Rob Powell
 */
public enum Notification {

    SMS_SEND_SUCCESSFUL(R.string.message_sent, R.color.green),
    SMS_SEND_FAILURE(R.string.message_failed_to_send, R.color.red);

    private final int stringResource;
    private final int colourResource;

    Notification(final int stringResource, final int colourResource) {
        this.stringResource = stringResource;
        this.colourResource = colourResource;
    }

    public int getStringResource() {
        return this.stringResource;
    }

    public int getColourResource() {
        return this.colourResource;
    }
}