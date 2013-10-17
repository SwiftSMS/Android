package com.icc.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.icc.model.Account;

public class Meteor extends Operator {

	private static final String SEND_SUCCESS_TEXT = "sentTrue";
	private static final String LOGIN_SUCCESS_TEXT = "Log Out";

	private static final String LOGIN_URL = "https://www.mymeteor.ie/go/mymeteor-login-manager";
	private static final String REMAINING_SMS_URL = "https://www.mymeteor.ie/cfusion/meteor/Meteor_REST/service/freeSMS";
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
		final ConnectionManager loginManager = new ConnectionManager(LOGIN_URL);
		loginManager.addPostHeader(POST_USERNAME, this.getAccount().getMobileNumber());
		loginManager.addPostHeader(POST_PASSWORD, this.getAccount().getPassword());
		final String loginHtml = loginManager.doConnection();

		return loginHtml.contains(LOGIN_SUCCESS_TEXT);
	}

	@Override
	public int getRemainingSMS() {
		this.login();
		final ConnectionManager manager = new ConnectionManager(REMAINING_SMS_URL, GET_REQUEST_METHOD, false);
		final String smsHtml = manager.doConnection();

		try {
			final JSONObject smsJson = new JSONObject(smsHtml);
			final JSONObject freeSmsJson = smsJson.getJSONObject(JSON_FREE_SMS);
			return freeSmsJson.getInt(JSON_REMAINING_FREE_SMS);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public boolean send(final String recipient, final String message) {
		this.addRecipient(recipient);

		final ConnectionManager sendManager = new ConnectionManager(SMS_URL);
		sendManager.addPostHeader(POST_AJAX_REQUEST, POST_VALUE_SEND_SMS);
		sendManager.addPostHeader(POST_MESSAGE_TEXT, message);
		final String html = sendManager.doConnection();

		return html.contains(SEND_SUCCESS_TEXT);
	}

	private void addRecipient(final String recipient) {
		final String addUrl = SMS_URL;
		final ConnectionManager addManager = new ConnectionManager(addUrl);
		addManager.addPostHeader(POST_AJAX_REQUEST, POST_VALUE_ADD_RECIPIENT);
		addManager.addPostHeader(POST_REMOVE, POST_VALUE_NONE);
		addManager.addPostHeader(POST_ADD, POST_VALUE_NO_ID + recipient);
		addManager.doConnection();
	}
}