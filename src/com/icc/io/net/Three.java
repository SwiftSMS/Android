package com.icc.io.net;

import java.util.Iterator;
import java.util.List;

import com.icc.model.Account;

/**
 * Created by Rob Powell on 04/10/13.
 */
public class Three extends Operator {

    // login strings/ values
    private static final String LOGIN_URL = "https://webtexts.three.ie/webtext/users/login";
    private static final String POST_USER = "data[User][telephoneNo]";
    private static final String POST_PASS = "data[User][pin]";
    private static final String SUCCESS_LOGIN = "Logout";

    // sms related strings values
    private static final String POST_SMS_URL = "https://webtexts.three.ie/webtext/messages/send";
    private static final String POST_MESSAGE_TEXT = "data[Message][message]";
    private static final String POST_RECIPIENT_INDIVIDUAL = "data[Message][recipients_individual]";
    private static final String SMS_SEND_SUCCESS_TEXT = "Message sent!";

    // contact/recipients
    private static final String RECIPIENTS_SEPARATOR = ",";

    //
    private static final String GET_REQUEST_METHOD = "GET";


	public Three(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		final ConnectionManager loginManager = new ConnectionManager(Three.LOGIN_URL);
		loginManager.addPostHeader(Three.POST_USER, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(Three.POST_PASS, this.getAccount().getPassword());
		final String loginHtml = loginManager.connect();

		return loginHtml.contains(SUCCESS_LOGIN);
	}

	@Override
	boolean doSend(final List<String> recipients, final String message) {

        final ConnectionManager smsSendManager = new ConnectionManager(Three.POST_SMS_URL);

        smsSendManager.addPostHeader(Three.POST_RECIPIENT_INDIVIDUAL, parseRecipients(recipients));
        smsSendManager.addPostHeader(Three.POST_MESSAGE_TEXT, message);
        final String html = smsSendManager.connect();

        return html.contains(Three.SMS_SEND_SUCCESS_TEXT);
	}

    private String parseRecipients(List<String> recipients) {

        String recipientsString = "";

        Iterator<String> recipientIterator = recipients.iterator();

        while(recipientIterator.hasNext()) {
            recipientsString += recipientIterator.next();

            if(recipientIterator.hasNext())
                recipientsString += RECIPIENTS_SEPARATOR;
        }
        return recipientsString;
    }

    @Override
	int doGetRemainingSMS() {

        final ConnectionManager manager = new ConnectionManager(POST_SMS_URL,
                Three.GET_REQUEST_METHOD, false);

        final String html = manager.connect();

		return 0;
	}

	@Override
	int doGetCharacterLimit() {
		return 160;
    }
}