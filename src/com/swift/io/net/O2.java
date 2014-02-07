package com.swift.io.net;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.swift.R;
import com.swift.model.Account;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.WarningResult;
import com.swift.utils.HTMLParser;

public class O2 extends Operator {

	private static final String MESSAGING_BASE_URL = "http://messaging.o2online.ie/";

	private static final String GET = "GET";

	private static final String LOGIN_PRE_URL = "https://www.o2online.ie/o2/my-o2/";
	private static final String PRE_REQUEST_ID = "request_id\" value=\"";
	private static final String POST_REQUEST_ID = "\">";

	private static final String LOGIN_URL = "https://www.o2online.ie/oam/server/auth_cred_submit";
	private static final String LOGIN_POST_USER = "username";
	private static final String LOGIN_POST_PASS = "password";
	private static final String LOGIN_POST_REQUEST_ID = "request_id";
	private static final String LOGIN_SUCCESS = "Welcome to My O2";

	private static final String HTTP_COOKIE = "Cookie";
	private static final String COOKIE_O2_DOMAIN = "http://www.o2online.ie";
	private static final String COOKIE_MESSAGING_DOMAIN = "http://messaging.o2online.ie";
	private static final String COOKIE_1 = "ObSSOCookie";
	private static final String COOKIE_2 = "o2onlinewebserver";
	private static final String COOKIE_3 = "IMDataGivenName";
	private static final String COOKIE_4 = "o3sisCookie";
	private static final String COOKIE_EQUALS = "=";
	private static final String COOKIE_SEMI_COLON = "; ";

	private static final String SMS_PRE = "spn_WebtextFree\">";
	private static final String SMS_POST = "</span>";
	private static final String SMS_PRE_URL = "http://messaging.o2online.ie/ssomanager.osp?APIID=AUTH-WEBSSO&TargetApp=o2om_smscenter_new.osp%3FMsgContentID%3D-1%26SID%3D_";
	private static final String SMS_PRE_PRE = "name=\"frame_content\" src=\"";
	private static final String SMS_PRE_POST = "\"";

	public O2(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		final ConnectionManager preMgr = new ConnectionManager(LOGIN_PRE_URL, GET, false);
		String html = preMgr.connect();

		if (!html.contains(LOGIN_SUCCESS)) {
			final String requestId = HTMLParser.parseHtml(html, PRE_REQUEST_ID, POST_REQUEST_ID);

			final ConnectionManager manager = new ConnectionManager(LOGIN_URL);
			manager.addPostHeader(LOGIN_POST_USER, this.getAccount().getMobileNumber());
			manager.addPostHeader(LOGIN_POST_PASS, this.getAccount().getPassword());
			manager.addPostHeader(LOGIN_POST_REQUEST_ID, requestId);
			html = manager.connect();
		}
		return html.contains(LOGIN_SUCCESS);
	}

	@Override
	int doGetRemainingSMS() {
		int remainingSMSCount = -1;
		final ConnectionManager manager = new ConnectionManager(this.getWebtextUrl(), GET, false);
		manager.setRequestHeader(HTTP_COOKIE, this.getCookieHeader());
		final String html = manager.connect();

		final String prefix = SMS_PRE;
		final String postfix = SMS_POST;
		final String remainingSMS = HTMLParser.parseHtml(html, prefix, postfix);

		if (remainingSMS != null) {
			remainingSMSCount = Integer.parseInt(remainingSMS);
		}
		return remainingSMSCount;
	}

	private String getCookieHeader() {
		final URI oUri = URI.create(COOKIE_O2_DOMAIN);
		final URI mUri = URI.create(COOKIE_MESSAGING_DOMAIN);
		final StringBuilder cookieHeader = new StringBuilder();
		final CookieManager m = (CookieManager) CookieHandler.getDefault();

		final List<HttpCookie> cookies = new ArrayList<HttpCookie>(m.getCookieStore().get(oUri));
		cookies.addAll(m.getCookieStore().get(mUri));
		for (final HttpCookie cookie : cookies) {
			if (cookie.getName().equals(COOKIE_1) || cookie.getName().equals(COOKIE_2) || cookie.getName().equals(COOKIE_3)
					|| cookie.getName().equals(COOKIE_4)) {
				cookieHeader.append(cookie.getName());
				cookieHeader.append(COOKIE_EQUALS);
				cookieHeader.append(cookie.getValue());
				cookieHeader.append(COOKIE_SEMI_COLON);
			}
		}
		cookieHeader.delete(cookieHeader.length() - 2, cookieHeader.length());
		return cookieHeader.toString();
	}

	private String getWebtextUrl() {
		final ConnectionManager manager = new ConnectionManager(SMS_PRE_URL, GET, false);
		final String html = manager.connect();

		final String prefix = SMS_PRE_PRE;
		final String postfix = SMS_PRE_POST;

		return MESSAGING_BASE_URL + HTMLParser.parseHtml(html, prefix, postfix);
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