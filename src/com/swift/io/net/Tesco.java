package com.swift.io.net;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Base64;

import com.swift.model.Account;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Success;

public class Tesco extends Operator {

	private static final String LOGIN_URL = "https://app.tescomobile.ie/MyTM/restws/user/permissions/";
	private static final String BASE_URL = "https://app.tescomobile.ie/MyTM/restws/webtext/";
	private static final String SEND_URL_POSTFIX = "/send";
	private static final String REMAIN_URL_POSTFIX = "/balance";

	private static final String JSON_MSISDN = "msisdn";
	private static final String JSON_NATIONAL_REMAINING = "nationalRemaining";
	private static final String JSON_TEXT = "text";
	private static final String JSON_MSISDNS = "msisdns";
	private static final String JSON_GROUPIDS = "groupIds";
	private static final String JSON_CONTACTIDS = "contactIds";

	private static final String GET = "GET";
	private static final String ACCEPT = "Accept";
	private static final String USER_AGENT = "User-Agent";
	private static final String AUTHORIZATION = "Authorization";
	private static final String ACCEPT_VALUE = "application/json";
	private static final String USER_AGENT_VALUE = "MyTescoApp/1.1";

	private final String auth;

	public Tesco(final Account account) {
		super(account);

		final String userPass = this.getAccount().getMobileNumber() + ":" + this.getAccount().getPassword();
		final String authUserPass = Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);
		this.auth = "Basic " + authUserPass;
	}

	@Override
	OperationResult doLogin() {
		final ConnectionManager manager = new ConnectionManager(LOGIN_URL + this.getAccount().getMobileNumber(), GET, false);
		manager.setRequestHeader(USER_AGENT, USER_AGENT_VALUE);
		manager.setRequestHeader(ACCEPT, ACCEPT_VALUE);
		manager.setRequestHeader(AUTHORIZATION, this.auth);
		final String html = manager.connect();

		final boolean isSuccess = html.contains(JSON_MSISDN);
		return isSuccess ? Success.LOGGED_IN : Fail.LOGIN_FAILED;
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager(BASE_URL + this.getAccount().getMobileNumber() + REMAIN_URL_POSTFIX, GET, false);
		manager.setRequestHeader(USER_AGENT, USER_AGENT_VALUE);
		manager.setRequestHeader(ACCEPT, ACCEPT_VALUE);
		manager.setRequestHeader(AUTHORIZATION, this.auth);
		final String rawJson = manager.connect();
		try {
			final JSONObject json = new JSONObject(rawJson);
			return json.getInt(JSON_NATIONAL_REMAINING);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	OperationResult doSend(final List<String> list, final String message) {
		final ConnectionManager manager = new ConnectionManager(BASE_URL + this.getAccount().getMobileNumber() + SEND_URL_POSTFIX);
		manager.setRequestHeader(USER_AGENT, USER_AGENT_VALUE);
		manager.setRequestHeader(ACCEPT, ACCEPT_VALUE);
		manager.setRequestHeader("Accept-Encoding", null);
		manager.setRequestHeader("Content-Type", ACCEPT_VALUE);
		manager.setRequestHeader(AUTHORIZATION, this.auth);

		final Map<String, Object> copyFrom = new LinkedHashMap<String, Object>();
		copyFrom.put(JSON_TEXT, Uri.decode(message));
		copyFrom.put(JSON_CONTACTIDS, new JSONArray());
		copyFrom.put(JSON_GROUPIDS, new JSONArray());
		copyFrom.put(JSON_MSISDNS, new JSONArray(list));

		final JSONObject requestJson = new JSONObject(copyFrom);
		manager.addRequestBody(requestJson.toString());
		final String rawJson = manager.connect();

		try {
			final JSONObject json = new JSONObject(rawJson);
			final boolean isSent = json.length() == 0;

			return isSent ? Success.MESSAGE_SENT : Fail.MESSAGE_FAILED;
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Fail.OPERATOR_CHANGED;
		}
	}

	@Override
	int doGetCharacterLimit() {
		return 160;
	}
}