package org.swiftsms;

import java.util.Date;

class Conversation {

    private final String number;
    private final String body;
    private final Date dateFormat;
    private final String type;

    Conversation(String number, String body, Date dateFormat, String type) {
        this.number = number;
        this.body = body;
        this.dateFormat = dateFormat;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "number='" + number + '\'' +
                ", body='" + body + '\'' +
                ", dateFormat=" + dateFormat +
                ", type='" + type + '\'' +
                '}';
    }
}
