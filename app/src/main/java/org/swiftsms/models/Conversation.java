package org.swiftsms.models;

public class Conversation {

    public final String number;
    public final String body;
    public final int read;

    public Conversation(final String number, final String body, final int read) {
        this.number = number;
        this.body = body;
        this.read = read;
    }

}
