package com.swift.io.net;

import java.util.List;

import android.net.Uri;

import com.swift.model.Account;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Success;
import com.swift.utils.ContactUtils;
import com.swift.utils.HTMLParser;

/**
 * Created by Rob Powell on 04/10/13.
 */
public class Three extends Operator {

	// login strings/ values
	private static final String LOGIN_URL = "https://webtexts.three.ie/users/login";
	private static final String POST_USER = "msisdn";
	private static final String POST_PASS = "pin";
	private static final String SUCCESS_LOGIN = "Logged in as";

	// sms related strings values
	private static final String SMS_URL = "https://webtexts.three.ie/messages/send";
	private static final String POST_MESSAGE_TEXT = "message";
	private static final String POST_RECIPIENT_INDIVIDUAL = "recipients_contacts[]";
	private static final String SMS_SEND_SUCCESS_TEXT = "Message sent";
	private static final String SMS_REMAINING_START_TEXT = "user-crumb-1\"><b>";
	private static final String SMS_REMAINING_END_TEXT = "/333";
	private static final String SMS_REMAINING_CHARS_START_TEXT = "'characterNumber'>";
	private static final String SMS_REMAINING_CHARS_END_TEXT = "</span>";

	private static final String GET_REQUEST_METHOD = "GET";
	private static final int MAX_MSG_RECIPIENTS = 3;

	public Three(final Account account) {
		super(account);
	}

	@Override
	OperationResult doLogin() {
		final ConnectionManager loginManager = new ConnectionManager(Three.LOGIN_URL);
		loginManager.addPostHeader(Three.POST_USER, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(Three.POST_PASS, this.getAccount().getPassword());
		final String loginHtml = loginManager.connect();

		final boolean isSuccess = loginHtml.contains(SUCCESS_LOGIN);
		return isSuccess ? Success.LOGGED_IN : Fail.LOGIN_FAILED;
	}

	@Override
	OperationResult doSend(final List<String> recipients, final String message) {

		OperationResult isSent = Fail.MESSAGE_FAILED;

		final List<List<String>> splitRecipients = ContactUtils.chopped(recipients, MAX_MSG_RECIPIENTS);

		for (final List<String> sendableRecipients : splitRecipients) {
			isSent = this.sendMessage(sendableRecipients, message);
		}

		return isSent;
	}

	private OperationResult sendMessage(final List<String> recipients, final String message) {
		final ConnectionManager sendMessageManager = this.createMessageManager(recipients, message);

		final boolean isSent = sendMessageManager.connect().contains(SMS_SEND_SUCCESS_TEXT);

		return isSent ? Success.MESSAGE_SENT : Fail.MESSAGE_FAILED;
	}

	private ConnectionManager createMessageManager(final List<String> recipients, final String message) {

		final ConnectionManager manager = new ConnectionManager(SMS_URL);

		manager.addPostHeader(POST_MESSAGE_TEXT, message);

		for (int i = 0; i < recipients.size(); i++) {
			final String key = Uri.encode(POST_RECIPIENT_INDIVIDUAL);
			final String value = Uri.encode(recipients.get(i));
			manager.addPostHeader(key, value);
		}

		return manager;
	}

	@Override
	int doGetRemainingSMS() {

		final ConnectionManager manager = new ConnectionManager(SMS_URL, Three.GET_REQUEST_METHOD, false);

		final String html = manager.connect();

		return this.getRemainingSmsFromHTML(html);
	}

	@Override
	int doGetCharacterLimit() {
		final ConnectionManager manager = new ConnectionManager(SMS_URL, Three.GET_REQUEST_METHOD, false);

		final String html = manager.connect();

		return this.getRemainingCharactersHTML(html);
	}

	private int getRemainingCharactersHTML(final String html) {
		return HTMLParser.parseIntFromHtml(html, SMS_REMAINING_CHARS_START_TEXT, SMS_REMAINING_CHARS_END_TEXT);
	}

	private int getRemainingSmsFromHTML(final String html) {
		return HTMLParser.parseIntFromHtml(html, SMS_REMAINING_START_TEXT, SMS_REMAINING_END_TEXT);
	}
}