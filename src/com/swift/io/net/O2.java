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
		final ConnectionManager preMgr = new ConnectionManager("https://www.o2online.ie/o2/my-o2/", "GET", false);
		preMgr.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0");
		preMgr.setRequestHeader("Referer", "http://www.o2online.ie/o2/my-o2/");
		final String preHtml = preMgr.connect();
		preHtml.contains("Welcome to My O2");

		final ConnectionManager manager = new ConnectionManager("https://www.o2online.ie/oam/server/auth_cred_submit");
		manager.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0");
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
		final String webtextUrl = "http://messaging.o2online.ie/" + this.getWebtextUrl();

		final ConnectionManager manager = new ConnectionManager(webtextUrl, "GET", false);
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

	private String getWebtextUrl() {
		final ConnectionManager manager = new ConnectionManager(
				"http://messaging.o2online.ie/ssomanager.osp?APIID=AUTH-WEBSSO&TargetApp=o2om_smscenter_new.osp%3FMsgContentID%3D-1%26SID%3D_", "GET", false);
		final String html = manager.connect();

		final String prefix = "name=\"frame_content\" src=\"";
		final String postfix = "\"";

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