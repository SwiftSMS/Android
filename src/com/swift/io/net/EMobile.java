package com.swift.io.net;

import java.util.List;

import com.swift.model.Account;
import com.swift.tasks.Status;

/**
 * Created by Rob Powell on 17/01/14.
 */
public class EMobile extends Operator {

	private static final String HOST_URL = "https://myaccount.emobile.ie";
	private static final String LOGIN_URL = "/go/myemobile-login-manager";
	private static final String POST_USER = "username";
	private static final String POST_PASS = "userpass";
	private static final String SUCCESS_LOGIN = "Log out";
	private static final String SEND_SUCCESS_TEXT = "sentTrue";

	private static final String SMS_CONSOLE = "/go/common/message-centre/web-sms/free-web-text";
	private static final String SMS_REMAINING_START_TEXT = "id=\"numfreesmstext\" value=\"";
	private static final String SMS_CHARS_LIMIT = "id=\"charsLeft\"  value=\"";
	private static final String AJAX_API = "/myemobileapi/index.cfm";
	private static final String AJAX_EVENT = "event";
	private static final String AJAX_REQUEST = "ajaxRequest";
	private static final String AJAX_FUNCTION = "func";
	private static final String AJAX_SMS_FUNCTION = "sendSMS";
	private static final String AJAX_MSISDNS_FUNCTION = "addEnteredMsisdns";
	private static final String POST_VALUE_ADD_RECIPIENT = "addEnteredMSISDNs";
	private static final String AJAX_SMS = "smsAjax";
	private static final String POST_MESSAGE_TEXT = "messageText";
	private static final String POST_ADD = "add";
	private static final String POST_REMOVE = "remove";
	private static final String POST_VALUE_NO_ID = "0%7C";

	public EMobile(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {

		final ConnectionManager loginManager = new ConnectionManager(EMobile.HOST_URL + EMobile.LOGIN_URL);
		loginManager.addPostHeader(EMobile.POST_USER, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(EMobile.POST_PASS, this.getAccount().getPassword());
		final String loginHtml = loginManager.connect();
		final boolean result = loginHtml.contains(SUCCESS_LOGIN);

		return result;
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager smsConsole = new ConnectionManager(EMobile.HOST_URL + EMobile.SMS_CONSOLE);

		final String smsConsoleHtml = smsConsole.connect();

		return this.getRemainingSmsFromHTML(smsConsoleHtml);
	}

	@Override
	Status doSend(final List<String> list, final String message) {

		this.addEnteredMSISDNs(list);
		final String resultHtml = this.sendMessage(message);
		final boolean isSent = resultHtml.contains(EMobile.SEND_SUCCESS_TEXT);
		return isSent ? Status.SUCCESS : Status.FAILED;
	}

	private void addEnteredMSISDNs(final List<String> recipients) {
		final StringBuilder sb = new StringBuilder(POST_VALUE_NO_ID);
		sb.append(recipients.remove(0));
		for (final String recipient : recipients) {
			sb.append(",");
			sb.append(POST_VALUE_NO_ID);
			sb.append(recipient);
		}

		final ConnectionManager addNumberRequest = new ConnectionManager(EMobile.HOST_URL + EMobile.AJAX_API);
		addNumberRequest.addPostHeader(EMobile.AJAX_EVENT, EMobile.AJAX_SMS);
		addNumberRequest.addPostHeader(EMobile.AJAX_FUNCTION, EMobile.AJAX_MSISDNS_FUNCTION);
		addNumberRequest.addPostHeader(EMobile.AJAX_REQUEST, EMobile.POST_VALUE_ADD_RECIPIENT);
		addNumberRequest.addPostHeader(EMobile.POST_ADD, sb.toString());
		addNumberRequest.addPostHeader(EMobile.POST_REMOVE, "-");
		addNumberRequest.connect();
	}

	private String sendMessage(final String message) {

		final ConnectionManager sendMessageRequest = new ConnectionManager(EMobile.HOST_URL + EMobile.AJAX_API);

		sendMessageRequest.addPostHeader(EMobile.AJAX_EVENT, EMobile.AJAX_SMS);
		sendMessageRequest.addPostHeader(EMobile.AJAX_FUNCTION, EMobile.AJAX_SMS_FUNCTION);
		sendMessageRequest.addPostHeader(EMobile.AJAX_REQUEST, EMobile.AJAX_SMS_FUNCTION);
		sendMessageRequest.addPostHeader(EMobile.POST_MESSAGE_TEXT, message);

		return sendMessageRequest.connect();
	}

	@Override
	int doGetCharacterLimit() {

		final ConnectionManager smsConsole = new ConnectionManager(EMobile.HOST_URL + EMobile.SMS_CONSOLE);

		final String smsConsoleHtml = smsConsole.connect();

		return this.getCharLimit(smsConsoleHtml);
	}

	private int getCharLimit(final String html) {

		int charLimit = -1;
		final String startText = EMobile.SMS_CHARS_LIMIT;

		final int index = html.indexOf(startText) + startText.length();

		final int startPos = index;
		final int endPos = index + 4;

		try {
			final String remainingCharsStr = html.substring(startPos, endPos).replaceAll("[</span>\"]", "");
			charLimit = Integer.parseInt(remainingCharsStr.trim());
		} catch (final Exception ex) {
		}

		return charLimit;
	}

	private int getRemainingSmsFromHTML(final String html) {

		int remainingSms = -1;
		final String startText = EMobile.SMS_REMAINING_START_TEXT;

		final int index = html.indexOf(startText) + startText.length();

		final int startPos = index;
		final int endPos = index + 4;

		try {
			final String remainingSmsStr = html.substring(startPos, endPos).replaceAll("[</span>\"]", "");
			remainingSms = Integer.parseInt(remainingSmsStr.trim());
		} catch (final Exception ex) {
		}

		return remainingSms;
	}
}