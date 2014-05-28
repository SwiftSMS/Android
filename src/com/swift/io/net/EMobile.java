package com.swift.io.net;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.swift.model.Account;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Success;

/**
 * Created by Rob Powell on 17/01/14.
 * 
 * This class manages interfacing with the website of EMobile. Much the same as Meteor.
 */
public class EMobile extends Operator {

	private static final String HOST_URL = "https://myaccount.emobile.ie";
	private static final String LOGIN_URL = "/go/myemobile-login-manager";
	private static final String POST_USER = "username";
	private static final String POST_PASS = "userpass";
	private static final String SUCCESS_LOGIN = "Log out";
	private static final String SMS_REMAINING_URL = HOST_URL + "/cfusion/meteor/Meteor_REST/service/freeSMS";
	private static final String SMS_CONSOLE = "/go/common/message-centre/web-sms/free-web-text";
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
	private static final String GET_REQUEST_METHOD = "GET";

	private static final String JSON_REMAINING_FREE_SMS = "remainingFreeSMS";
	private static final String JSON_FREE_SMS = "FreeSMS";

	private int localRemainingSmsCount = -1;

	public EMobile(final Account account) {
		super(account);
	}

	@Override
	OperationResult doLogin() {

		final ConnectionManager loginManager = new ConnectionManager(EMobile.HOST_URL + EMobile.LOGIN_URL);
		loginManager.addPostHeader(EMobile.POST_USER, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(EMobile.POST_PASS, this.getAccount().getPassword());
		final String loginHtml = loginManager.connect();

		final boolean isSuccess = loginHtml.contains(SUCCESS_LOGIN);
		return isSuccess ? Success.LOGGED_IN : Fail.LOGIN_FAILED;
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager(SMS_REMAINING_URL, GET_REQUEST_METHOD, false);
		final String smsHtml = manager.connect();

		try {
			final JSONObject smsJson = new JSONObject(smsHtml);
			final JSONObject freeSmsJson = smsJson.getJSONObject(JSON_FREE_SMS);
			this.localRemainingSmsCount = freeSmsJson.getInt(JSON_REMAINING_FREE_SMS);
			return this.localRemainingSmsCount;
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	OperationResult doSend(final List<String> list, final String message) {
		final int expectedRemainingSms = this.localRemainingSmsCount - list.size();

		this.addEnteredMSISDNs(list);
		this.sendMessage(message);

		final boolean isSent = this.hasRemainingSmsDecremented(expectedRemainingSms);
		return isSent ? Success.MESSAGE_SENT : Fail.MESSAGE_FAILED;
	}

	private void addEnteredMSISDNs(final List<String> recipients) {
		final StringBuilder sb = new StringBuilder();
		for (final String recipient : recipients) {
			sb.append(POST_VALUE_NO_ID);
			sb.append(recipient);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1); // remove trailing comma

		final ConnectionManager addNumberRequest = new ConnectionManager(EMobile.HOST_URL + EMobile.AJAX_API);
		addNumberRequest.addPostHeader(EMobile.AJAX_EVENT, EMobile.AJAX_SMS);
		addNumberRequest.addPostHeader(EMobile.AJAX_FUNCTION, EMobile.AJAX_MSISDNS_FUNCTION);
		addNumberRequest.addPostHeader(EMobile.AJAX_REQUEST, EMobile.POST_VALUE_ADD_RECIPIENT);
		addNumberRequest.addPostHeader(EMobile.POST_ADD, sb.toString());
		addNumberRequest.addPostHeader(EMobile.POST_REMOVE, "-");
		addNumberRequest.connect();
	}

	private void sendMessage(final String message) {

		final ConnectionManager sendMessageRequest = new ConnectionManager(EMobile.HOST_URL + EMobile.AJAX_API);

		sendMessageRequest.addPostHeader(EMobile.AJAX_EVENT, EMobile.AJAX_SMS);
		sendMessageRequest.addPostHeader(EMobile.AJAX_FUNCTION, EMobile.AJAX_SMS_FUNCTION);
		sendMessageRequest.addPostHeader(EMobile.AJAX_REQUEST, EMobile.AJAX_SMS_FUNCTION);
		sendMessageRequest.addPostHeader(EMobile.POST_MESSAGE_TEXT, message);
		sendMessageRequest.connect();
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
		final int endPos = index + 4;

		try {
			final String remainingCharsStr = html.substring(index, endPos).replaceAll("[</span>\"]", "");
			charLimit = Integer.parseInt(remainingCharsStr.trim());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return charLimit;
	}

	private boolean hasRemainingSmsDecremented(final int expectedSmsRemaining) {
		for (int x = 0; x < 5; x++) {
			if (this.getRemainingSMS() == expectedSmsRemaining) {
				return true;
			}
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}