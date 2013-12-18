package com.icc.utils;

/**
 * Notifications for ICC Activities
 *
 * @author Rob Powell
 */
public enum Notification {

    SMS_SEND_SUCCESSFUL("Message Sent!"), SMS_SEND_FAIL("Message not sent!");

    private final String notification;

    Notification(final String notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return this.notification;
    }
}
