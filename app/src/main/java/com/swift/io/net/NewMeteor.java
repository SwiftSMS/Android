package com.swift.io.net;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.swift.model.Account;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Success;
import com.swift.utils.HTMLParser;

public class NewMeteor extends Operator {

	public NewMeteor(final Account account) {
		super(account);
	}

	@Override
	OperationResult doLogin() {
		final ConnectionManager manager = new ConnectionManager("https://my.meteor.ie/meteor/transactional/login");
		manager.addPostHeader("username", this.getAccount().getMobileNumber());
		manager.addPostHeader("password", this.getAccount().getPassword());
		final String html = manager.connect();

		final boolean isSuccess = html.contains("Logout");
		return isSuccess ? Success.LOGGED_IN : Fail.LOGIN_FAILED;
	}

	@Override
	int doGetRemainingSMS() {
		final String number = this.getAccountNumber();
		if (number != null) {
			final ConnectionManager manager = new ConnectionManager("https://my.meteor.ie/webtext/page/main?msisdn=" + number, "GET", false);
			final String html = manager.connect();

			final String remaining = HTMLParser.parseHtml(html, "\"or\": \"", "\",");
			if (remaining != null) {
				return Integer.parseInt(remaining);
			}
		}
		return -1;
	}

	private String getAccountNumber() {
		final ConnectionManager preManager = new ConnectionManager("https://my.meteor.ie/rest/secure/brand/3/portalUser/lines", "GET", false);
		final String preJson = preManager.connect();

		String number = null;
		try {
			final JSONObject json = new JSONObject(preJson);
			final JSONObject data = json.getJSONObject("data");
			final JSONArray listing = data.getJSONArray("pairingsList");
			final JSONObject account = listing.getJSONObject(0);
			number = account.getString("number");
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return number;
	}

	@Override
	OperationResult doSend(final List<String> list, final String message) {
		final ConnectionManager manager = new ConnectionManager("https://my.meteor.ie/webtext/gwtAppRequest");
		final JSONObject json = this.buildSendJson(list, message);
		manager.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
		manager.addRequestBody(json.toString());
		final String rawResult = manager.connect();

		final boolean isSent = rawResult.contains("\"S\":[true]");
		return isSent ? Success.MESSAGE_SENT : Fail.MESSAGE_FAILED;
	}

	private JSONObject buildSendJson(final List<String> list, final String message) {
		final JSONObject json = new JSONObject();
		try {
			final JSONArray msgArray = new JSONArray();
			msgArray.put(Uri.decode(message));
			msgArray.put(new JSONArray());
			msgArray.put(new JSONArray(list));

			final JSONObject inner = new JSONObject();
			inner.put("O", "mHdGltoQN30dWmsxAIiW63$wnLc=");
			inner.put("P", msgArray);

			final JSONArray innerArray = new JSONArray();
			innerArray.put(inner);

			json.put("F", "com.britebill.webtext.gwt.base.request.ApplicationRequestFactory");
			json.put("I", innerArray);
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	@Override
	int doGetCharacterLimit() {
		return 480;
	}
}