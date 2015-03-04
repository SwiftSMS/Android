package com.swift.io.net;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.swift.model.Account;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Success;

public class Meteor extends Operator {

	private static final String SEND_SUCCESS_TEXT = "sentTrue";
	private static final String LOGIN_SUCCESS_TEXT = "Log Out";

	private static final String HOSTNAME = "https://www.mymeteor.ie/";
	private static final String LOGIN_URL = HOSTNAME + "go/mymeteor-login-manager";
	private static final String REMAINING_SMS_URL = HOSTNAME + "cfusion/meteor/Meteor_REST/service/freeSMS";
	private static final String CHARACTER_COUNT_URL = HOSTNAME + "go/freewebtext";
	private static final String SMS_URL = HOSTNAME + "mymeteorapi/index.cfm?event=smsAjax";

	private static final String GET_REQUEST_METHOD = "GET";

	private static final String JSON_REMAINING_FREE_SMS = "remainingFreeSMS";
	private static final String JSON_FREE_SMS = "FreeSMS";

	private static final String POST_ADD = "add";
	private static final String POST_AJAX_REQUEST = "ajaxRequest";
	private static final String POST_MESSAGE_TEXT = "messageText";
	private static final String POST_PASSWORD = "userpass";
	private static final String POST_REMOVE = "remove";
	private static final String POST_USERNAME = "username";

	private static final String POST_VALUE_ADD_RECIPIENT = "addEnteredMSISDNs";
	private static final String POST_VALUE_NO_ID = "0%7C";
	private static final String POST_VALUE_NONE = "-";
	private static final String POST_VALUE_SEND_SMS = "sendSMS";

	public Meteor(final Account account) {
		super(account);
	}

	@Override
	OperationResult doLogin() {
		final ConnectionManager loginManager = new ConnectionManager(Meteor.LOGIN_URL);
		loginManager.addPostHeader(Meteor.POST_USERNAME, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(Meteor.POST_PASSWORD, this.getAccount().getPassword());
		final String loginHtml = loginManager.connect();

		final boolean isSuccess = loginHtml.contains(LOGIN_SUCCESS_TEXT);
		return isSuccess ? Success.LOGGED_IN : Fail.LOGIN_FAILED;
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager(Meteor.REMAINING_SMS_URL, Meteor.GET_REQUEST_METHOD, false);
		final String smsHtml = manager.connect();

		try {
			final JSONObject smsJson = new JSONObject(smsHtml);
			final JSONObject freeSmsJson = smsJson.getJSONObject(Meteor.JSON_FREE_SMS);
			return freeSmsJson.getInt(Meteor.JSON_REMAINING_FREE_SMS);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	OperationResult doSend(final List<String> recipients, final String message) {
		this.addRecipients(recipients);

		final ConnectionManager sendManager = new ConnectionManager(Meteor.SMS_URL);
		sendManager.addPostHeader(Meteor.POST_AJAX_REQUEST, Meteor.POST_VALUE_SEND_SMS);
		sendManager.addPostHeader(Meteor.POST_MESSAGE_TEXT, message);
		final boolean isSent = sendManager.connect().contains(Meteor.SEND_SUCCESS_TEXT);

		return isSent ? Success.MESSAGE_SENT : Fail.MESSAGE_FAILED;
	}

	private void addRecipients(final List<String> recipients) {
		final StringBuilder sb = new StringBuilder();
		for (final String recipient : recipients) {
			sb.append(Meteor.POST_VALUE_NO_ID);
			sb.append(recipient);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1); // remove trailing comma
		}

		final ConnectionManager addManager = new ConnectionManager(Meteor.SMS_URL);
		addManager.addPostHeader(Meteor.POST_AJAX_REQUEST, Meteor.POST_VALUE_ADD_RECIPIENT);
		addManager.addPostHeader(Meteor.POST_REMOVE, Meteor.POST_VALUE_NONE);
		addManager.addPostHeader(Meteor.POST_ADD, sb.toString());
		addManager.connect();
	}

	@Override
	int doGetCharacterLimit() {
		final ConnectionManager manager = new ConnectionManager(Meteor.CHARACTER_COUNT_URL, Meteor.GET_REQUEST_METHOD, false);
		final String html = manager.connect();

		final String charsText = "charsLeft\" value='";
		final int startPos = html.indexOf(charsText) + charsText.length();
		final int endPos = html.indexOf("'", startPos);
		if (startPos > charsText.length()) {
			final String characterCount = html.substring(startPos, endPos);
			return Integer.valueOf(characterCount);
		} else {
			return -1;
		}
	}
}