package org.swiftsms;

import java.util.Date;

/**
 * Created by sean on 03/03/17.
 */

public class Message {

    final String message;
    final Date date;
    final int type;

    public Message(final String message, final Date date, final int type) {
        this.message = message;
        this.date = date;
        this.type = type;
    }
}
