package com.swift.io.net;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.swift.model.Account;
import com.swift.tasks.results.Failure;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Successful;
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
	private static final String SESSION_SID_PRE = "var GLOBAL_SESSION_ID = '";
	private static final String SESSION_SID_POST = "';";

	private static final String HTTP_COOKIE = "Cookie";
	private static final String COOKIE_O2_DOMAIN = "http://www.o2online.ie";
	private static final String COOKIE_MESSAGING_DOMAIN = "http://messaging.o2online.ie";
	private static final String COOKIE_1 = "ObSSOCookie";
	private static final String COOKIE_2 = "o2onlinewebserver";
	private static final String COOKIE_3 = "IMDataGivenName";
	private static final String COOKIE_4 = "o3sisCookie";
	private static final String COOKIE_EQUALS = "=";
	private static final String COOKIE_SEMI_COLON = "; ";

	private static final String SESSION_URL = "http://messaging.o2online.ie/ssomanager.osp?APIID=AUTH-WEBSSO&TargetApp=o2om_smscenter_new.osp%3FMsgContentID%3D-1%26SID%3D_";
	private static final String SMS_BASE_URL = MESSAGING_BASE_URL + "o2om_smscenter_new.osp?MsgContentID=-1&SID=_&SID=";
	private static final String SMS_PRE = "spn_WebtextFree\">";
	private static final String SMS_POST = "</span>";
	private static final String SEND_FORM_NAME = "name=\"form_WebtextSave\"";
	private static final String SEND_RURL_PRE = "name=\"RURL\" value=\"";
	private static final String SEND_REF_PRE = "name=\"REF\" value=\"";
	private static final String POST_TEXT = "\"";

	private String sessionId;
	private String sendRef;
	private String sendRUrl;

	private String remainingSMS;

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
		this.prepareWebtextSession();
		return html.contains(LOGIN_SUCCESS);
	}

	private void prepareWebtextSession() {
		ConnectionManager manager = new ConnectionManager(SESSION_URL, GET, false);
		String html = manager.connect();
		this.sessionId = HTMLParser.parseHtml(html, SESSION_SID_PRE, SESSION_SID_POST);

		manager = new ConnectionManager(SMS_BASE_URL + this.sessionId, GET, false);
		manager.setRequestHeader(HTTP_COOKIE, this.getCookieHeader());
		html = manager.connect();
		html = html.substring(html.indexOf(SEND_FORM_NAME));
		this.sendRef = HTMLParser.parseHtml(html, SEND_REF_PRE, POST_TEXT);
		this.sendRUrl = HTMLParser.parseHtml(html, SEND_RURL_PRE, POST_TEXT);
		this.remainingSMS = HTMLParser.parseHtml(html, SMS_PRE, SMS_POST);
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

	@Override
	int doGetRemainingSMS() {
		return this.remainingSMS == null ? -1 : Integer.parseInt(this.remainingSMS);
	}

	@Override
	OperationResult doSend(final List<String> recipients, final String message) {
		final StringBuilder sb = new StringBuilder();
		for (final String recipient : recipients) {
			sb.append(recipient);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1); // remove trailing comma

		final ConnectionManager manager = new ConnectionManager("http://messaging.o2online.ie/smscenter_send.osp");
		manager.setRequestHeader(HTTP_COOKIE, this.getCookieHeader());
		manager.addPostHeader("SID", this.sessionId);
		manager.addPostHeader("MsgContentID", "-1");
		manager.addPostHeader("SMSTo", sb.toString());
		manager.addPostHeader("SMSText", message);
		manager.addPostHeader("FlagDLR", "1");
		manager.addPostHeader("RepeatStartDate", "2014,01,08,18,30,00");
		manager.addPostHeader("RepeatEndDate", "2014,01,08,18,30,00");
		manager.addPostHeader("RepeatType", "0");
		manager.addPostHeader("RepeatEndType", "0");
		manager.addPostHeader("FolderID", "0");
		manager.addPostHeader("REF", this.sendRef);
		manager.addPostHeader("RURL", this.sendRUrl);
		final String rawJson = manager.connect();

		boolean isSent = false;
		try {
			final JSONObject json = new JSONObject(rawJson);
			isSent = json.getBoolean("isSuccess");
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return isSent ? new Successful() : new Failure();
	}

	@Override
	int doGetCharacterLimit() {
		return 160;
	}
}