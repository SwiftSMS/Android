package org.swiftsms;

import java.util.Date;

class Conversation {

    final String number;
    final String body;
    final Date dateFormat;
    final String threadId;

    Conversation(final String number, final String body, final Date dateFormat, final String threadId) {
        this.number = number;
        this.body = body;
        this.dateFormat = dateFormat;
        this.threadId = threadId;
    }

}
