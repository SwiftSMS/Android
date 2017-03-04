package org.swiftsms.models;

import java.util.Date;

public class Conversation {

    public final String number;
    public final String body;
    public final Date dateFormat;
    public final String threadId;

    public Conversation(final String number, final String body, final Date dateFormat, final String threadId) {
        this.number = number;
        this.body = body;
        this.dateFormat = dateFormat;
        this.threadId = threadId;
    }

}
