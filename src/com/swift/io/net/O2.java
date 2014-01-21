package com.swift.io.net;

import java.util.List;

import android.util.Log;

import com.swift.InternalString;
import com.swift.R;
import com.swift.model.Account;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.WarningResult;

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
		final ConnectionManager manager = new ConnectionManager("http://messaging.o2online.ie/o2om_smscenter_new.osp?SID=_", "GET", false);
		final String html = manager.connect();

		int remainingSmsCount = -1;
		final String startText = "spn_WebtextFree\">";
		final int startPos = html.indexOf(startText) + startText.length();
		final int endPos = html.indexOf("</span>", startPos);

		Log.d(InternalString.LOG_TAG, "doGetRemainingSMS - startPos = " + startPos);
		Log.d(InternalString.LOG_TAG, "doGetRemainingSMS - endPos = " + endPos);

		if (startPos > startText.length()) {
			remainingSmsCount = Integer.parseInt(html.substring(startPos, endPos));
		}
		// <input type="hidden" name="request_id" value="-3152757608710563281">
		Log.d(InternalString.LOG_TAG, "doGetRemainingSMS - remainingSmsCount = " + remainingSmsCount);
		return remainingSmsCount;
	}

	@Override
	OperationResult doSend(final List<String> list, final String message) {
		return new WarningResult(R.string.not_implemented);
	}

	@Override
	int doGetCharacterLimit() {
		return -1;
	}
}