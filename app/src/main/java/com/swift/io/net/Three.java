package com.swift.io.net;

import java.util.List;

import android.net.Uri;

import com.swift.model.Account;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Success;
import com.swift.utils.ContactUtils;

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
	private static final String SMS_URL = "https://webtexts.three.ie/webtext/messages/send";
	private static final String POST_MESSAGE_TEXT = "data[Message][message]";
	private static final String POST_RECIPIENT_INDIVIDUAL = "data[Message][recipients_individual]";
	private static final String SMS_SEND_SUCCESS_TEXT = "Message sent!";
	private static final String SMS_REMAINING_END_TEXT = "(of 333)</p>";
	private static final String SMS_REMAINING_CHARS_START_TEXT = "'characterNumber'>";

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
			final String key = Uri.encode(POST_RECIPIENT_INDIVIDUAL + "[" + i + "]");
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

		int remainingChars = -1;
		final String startText = Three.SMS_REMAINING_CHARS_START_TEXT;

		final int index = html.indexOf(startText);

		final int startPos = index;
		final int endPos = index + 5;

		try {
			final String remainingCharsStr = html.substring(startPos, endPos).replaceAll("[</span>]", "");
			remainingChars = Integer.parseInt(remainingCharsStr.trim());
		} catch (final Exception ex) {
		}

		return remainingChars;
	}

	private int getRemainingSmsFromHTML(final String html) {

		int remainingTexts = -1;
		final String endText = Three.SMS_REMAINING_END_TEXT;

		final int index = html.indexOf(endText);

		final int startPos = index - 4;
		final int endPos = index;

		if (startPos > endText.length()) {
			try {
				final String remainingSmsString = html.substring(startPos, endPos).replaceAll("[<p>]", "");
				remainingTexts = Integer.parseInt(remainingSmsString.trim());
			} catch (final Exception ex) {
			}
		}

		return remainingTexts;
	}
}