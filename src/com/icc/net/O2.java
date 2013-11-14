package com.icc.net;

import java.util.List;

import android.util.Log;

import com.icc.InternalString;
import com.icc.model.Account;

public class O2 extends Operator {

	public O2(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		final ConnectionManager manager = new ConnectionManager("https://www.o2online.ie/oam/server/auth_cred_submit");
		manager.addPostHeader("username", this.getAccount().getMobileNumber());
		manager.addPostHeader("password", this.getAccount().getPassword());
		manager.addPostHeader("request_id", this.getRequestId());
		final String html = manager.connect();
		return html.contains("Redirect") && html.contains("o2online");
	}

	private String getRequestId() {
		final ConnectionManager manager = new ConnectionManager("https://www.o2online.ie/idm/login/redirect.jsp", "GET", false);
		final String html = manager.connect();

		String requestId = "";
		final String startText = "request_id\" value=\"";
		final int startPos = html.indexOf(startText) + startText.length();
		final int endPos = html.indexOf("\">", startPos);

		Log.d(InternalString.LOG_TAG, "getRequestId - startPos = " + startPos);
		Log.d(InternalString.LOG_TAG, "getRequestId - endPos = " + endPos);

		if (startPos > startText.length()) {
			requestId = html.substring(startPos, endPos);
		}
		// <input type="hidden" name="request_id" value="-3152757608710563281">
		Log.d(InternalString.LOG_TAG, "getRequestId - requestId = " + requestId);
		return requestId;
	}

	@Override
	int doGetRemainingSMS() {
		return -1;
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