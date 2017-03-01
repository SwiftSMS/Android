package org.swiftsms;

import java.util.Date;

class Conversation {

    final String number;
    final String body;
    final Date dateFormat;
    final String type;

    Conversation(String number, String body, Date dateFormat, String type) {
        this.number = number;
        this.body = body;
        this.dateFormat = dateFormat;
        this.type = type;
    }

}
