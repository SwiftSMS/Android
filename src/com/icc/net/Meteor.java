package com.icc.net;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.icc.model.Account;

public class Meteor extends Operator {

	private static final String SEND_SUCCESS_TEXT = "sentTrue";
	private static final String LOGIN_SUCCESS_TEXT = "Log Out";

	private static final String LOGIN_URL = "https://www.mymeteor.ie/go/mymeteor-login-manager";
	private static final String REMAINING_SMS_URL = "https://www.mymeteor.ie/cfusion/meteor/Meteor_REST/service/freeSMS";
	private static final String CHARACTER_COUNT_URL = "https://www.mymeteor.ie/go/freewebtext";
	private static final String SMS_URL = "https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax";

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
	boolean doLogin() {
		final ConnectionManager loginManager = new ConnectionManager(Meteor.LOGIN_URL);
		loginManager.addPostHeader(Meteor.POST_USERNAME, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(Meteor.POST_PASSWORD, this.getAccount().getPassword());
		final String loginHtml = loginManager.doConnection();

		return loginHtml.contains(Meteor.LOGIN_SUCCESS_TEXT);
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager(Meteor.REMAINING_SMS_URL, Meteor.GET_REQUEST_METHOD, false);
		final String smsHtml = manager.doConnection();

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
	public boolean preSend(final Context context) {
		return false;
	}

	@Override
	boolean doSend(final List<String> recipients, final String message) {
		for (final String recipient : recipients) {
			this.addRecipient(recipient);
		}

		final ConnectionManager sendManager = new ConnectionManager(Meteor.SMS_URL);
		sendManager.addPostHeader(Meteor.POST_AJAX_REQUEST, Meteor.POST_VALUE_SEND_SMS);
		sendManager.addPostHeader(Meteor.POST_MESSAGE_TEXT, message);
		final String html = sendManager.doConnection();

		return html.contains(Meteor.SEND_SUCCESS_TEXT);
	}

	private void addRecipient(final String recipient) {
		final String addUrl = Meteor.SMS_URL;
		final ConnectionManager addManager = new ConnectionManager(addUrl);
		addManager.addPostHeader(Meteor.POST_AJAX_REQUEST, Meteor.POST_VALUE_ADD_RECIPIENT);
		addManager.addPostHeader(Meteor.POST_REMOVE, Meteor.POST_VALUE_NONE);
		addManager.addPostHeader(Meteor.POST_ADD, Meteor.POST_VALUE_NO_ID + recipient);
		addManager.doConnection();
	}

	@Override
	int doGetCharacterLimit() {
		final ConnectionManager manager = new ConnectionManager(Meteor.CHARACTER_COUNT_URL, Meteor.GET_REQUEST_METHOD, false);
		final String html = manager.doConnection();

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