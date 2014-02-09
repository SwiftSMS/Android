package com.swift.io.net;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.swift.model.Account;
import com.swift.tasks.results.Failure;
import com.swift.tasks.results.OperationResult;

public class NewMeteor extends Operator {

	public NewMeteor(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		final ConnectionManager manager = new ConnectionManager("https://my.meteor.ie/meteor/transactional/login");
		manager.addPostHeader("username", this.getAccount().getMobileNumber());
		manager.addPostHeader("password", this.getAccount().getPassword());
		final String html = manager.connect();

		return html.contains("Logout");
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager("https://my.meteor.ie/webtext/mobileNumbers/+353857855532", "GET", false);
		final String rawJson = manager.connect();

		try {
			final JSONObject json = new JSONObject(rawJson);
			final JSONArray smsLimits = json.getJSONArray("limits");
			final JSONObject combined = smsLimits.getJSONObject(0);
			return combined.getInt("balance");
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	OperationResult doSend(final List<String> list, final String message) {
		return new Failure();
	}

	@Override
	int doGetCharacterLimit() {
		return 0;
	}

}
