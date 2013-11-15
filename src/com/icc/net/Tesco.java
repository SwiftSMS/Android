package com.icc.net;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

import com.icc.InternalString;
import com.icc.model.Account;

public class Tesco extends Operator {

	private final String auth;

	public Tesco(final Account account) {
		super(account);

		final String userPass = this.getAccount().getMobileNumber() + ":" + this.getAccount().getPassword();
		final String authUserPass = Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);
		this.auth = "Basic " + authUserPass;
	}

	@Override
	boolean doLogin() {
		final ConnectionManager manager = new ConnectionManager("https://app.tescomobile.ie/MyTM/restws/user/permissions/"
				+ this.getAccount().getMobileNumber(), "GET", false);
		manager.setRequestHeader("User-Agent", "MyTescoApp/1.1");
		manager.setRequestHeader("Authorization", this.auth);
		final String html = manager.connect();
		return html.contains("msisdn");
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager("https://app.tescomobile.ie/MyTM/restws/webtext/"
				+ this.getAccount().getMobileNumber() + "/balance", "GET", false);
		manager.setRequestHeader("User-Agent", "MyTescoApp/1.1");
		manager.setRequestHeader("Authorization", this.auth);
		final String rawJson = manager.connect();
		try {
			final JSONObject json = new JSONObject(rawJson);
			return json.getInt("nationalRemaining");
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	boolean doSend(final List<String> list, final String message) {
		final ConnectionManager manager = new ConnectionManager(
				"https://app.tescomobile.ie/MyTM/restws/webtext/0892088841/send");
		manager.setRequestHeader("User-Agent", "MyTescoApp/1.1");
		manager.setRequestHeader("Authorization", this.auth);

		// "{"message":"sdf","contacts":[],"groups":[],"msisdns":["0857855532"]}"
		final Map<String, Object> copyFrom = new LinkedHashMap<String, Object>();
		copyFrom.put("message", message);
		copyFrom.put("contacts", new JSONArray());
		copyFrom.put("groups", new JSONArray());
		copyFrom.put("msisdns", new JSONArray(list));

		final JSONObject requestJson = new JSONObject(copyFrom);
		Log.d(InternalString.LOG_TAG, "doSend - request json: " + requestJson.toString());
		manager.addRequestBody(requestJson.toString());
		final String rawJson = manager.connect();
		Log.d(InternalString.LOG_TAG, "doSend - returned json: " + rawJson.toString());

		try {
			final JSONObject json = new JSONObject(rawJson);
			return json.getBoolean("status");
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	int doGetCharacterLimit() {
		return 160;
	}

}
