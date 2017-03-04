package org.swiftsms.models;

import java.util.Date;

/**
 * Created by sean on 03/03/17.
 */

public class Message {

    public final String message;
    public final Date date;
    public final int type;

    public Message(final String message, final Date date, final int type) {
        this.message = message;
        this.date = date;
        this.type = type;
    }
}
