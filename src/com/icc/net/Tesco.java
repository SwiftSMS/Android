package com.icc.net;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

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
		return false;
	}

	@Override
	int doGetCharacterLimit() {
		return -1;
	}

}
