package com.swift.io.net;

import java.util.List;

import com.swift.R;
import com.swift.model.Account;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.WarningResult;
import com.swift.utils.HTMLParser;

public class O2 extends Operator {

	private String preWebtextUrl;

	public O2(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		final ConnectionManager preMgr = new ConnectionManager("https://www.o2online.ie/o2/my-o2/", "GET", false);
		preMgr.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0");
		preMgr.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		preMgr.setRequestHeader("Accept-Language", "en-gb,en;q=0.5");
		preMgr.setRequestHeader("Accept-Encoding", "gzip, deflate");
		final String preHtml = preMgr.connect();

		final String requestId = HTMLParser.parseHtml(preHtml, "request_id\" value=\"", "\">");

		final ConnectionManager manager = new ConnectionManager("https://www.o2online.ie/oam/server/auth_cred_submit");
		manager.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0");
		manager.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		manager.setRequestHeader("Accept-Language", "en-gb,en;q=0.5");
		manager.setRequestHeader("Accept-Encoding", "gzip, deflate");
		manager.addPostHeader("username", this.getAccount().getMobileNumber());
		manager.addPostHeader("password", this.getAccount().getPassword());
		manager.addPostHeader("request_id", requestId);
		final String html = manager.connect();

		this.preWebtextUrl = HTMLParser.parseHtml(html, "title=\"Send a Webtext\" href=\"", "\">");

		return html.contains("Welcome to My O2");
	}

	@Override
	int doGetRemainingSMS() {
		int remainingSMSCount = -1;
		if (this.preWebtextUrl != null) {
			final ConnectionManager manager = new ConnectionManager(this.getWebtextUrl(), "GET", false);
			manager.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0");
			manager.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			manager.setRequestHeader("Referer", this.preWebtextUrl);
			manager.setRequestHeader("Accept-Language", "en-gb,en;q=0.5");
			manager.setRequestHeader("Accept-Encoding", "gzip, deflate");
			final String html = manager.connect();

			final String prefix = "spn_WebtextFree\">";
			final String postfix = "</span>";
			final String remainingSMS = HTMLParser.parseHtml(html, prefix, postfix);

			if (remainingSMS != null) {
				remainingSMSCount = Integer.parseInt(remainingSMS);
			}
		}
		return remainingSMSCount;
	}

	private String getWebtextUrl() {
		final ConnectionManager manager = new ConnectionManager(this.preWebtextUrl, "GET", false);
		manager.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0");
		manager.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		manager.setRequestHeader("Accept-Language", "en-gb,en;q=0.5");
		manager.setRequestHeader("Accept-Encoding", "gzip, deflate");
		final String html = manager.connect();

		final String prefix = "name=\"frame_content\" src=\"";
		final String postfix = "\"";

		return "http://messaging.o2online.ie/" + HTMLParser.parseHtml(html, prefix, postfix);
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