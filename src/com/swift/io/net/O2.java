package com.swift.io.net;

import java.util.List;

import com.swift.R;
import com.swift.model.Account;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.WarningResult;
import com.swift.utils.HTMLParser;

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

		final String prefix = "request_id\" value=\"";
		final String postfix = "\">";
		return HTMLParser.parseHtml(html, prefix, postfix);
	}

	@Override
	int doGetRemainingSMS() {
		final String sessionId = this.getSessionId();
		final String url = "http://messaging.o2online.ie/o2om_smscenter_new.osp?MsgContentID=-1&SID=_&SID=" + sessionId;

		final ConnectionManager manager = new ConnectionManager(url, "GET", false);
		final String html = manager.connect();

		final String prefix = "spn_WebtextFree\">";
		final String postfix = "</span>";
		final String remainingSMS = HTMLParser.parseHtml(html, prefix, postfix);

		int remainingSMSCount = -1;
		if (remainingSMS != null) {
			remainingSMSCount = Integer.parseInt(remainingSMS);
		}
		return remainingSMSCount;
	}

	private String getSessionId() {
		final ConnectionManager manager = new ConnectionManager("http://messaging.o2online.ie/ssomanager.osp?APIID=AUTH-WEBSSO", "GET", false);
		final String html = manager.connect();

		final String prefix = "GLOBAL_SESSION_ID = '";
		final String postfix = "';";

		return HTMLParser.parseHtml(html, prefix, postfix);
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