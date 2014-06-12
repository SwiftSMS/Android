package com.swift.io.net;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.swift.R;
import com.swift.model.Account;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;
import com.swift.tasks.results.Success;
import com.swift.utils.HTMLParser;

public class O2 extends Operator {

	private static final String MESSAGING_BASE_URL = "http://messaging.o2online.ie/";

	private static final String HTTP_REFERER = "Referer";
	private static final String HTTP_COOKIE = "Cookie";
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

	private static final String COOKIE_O2_DOMAIN = "http://www.o2online.ie";
	private static final String COOKIE_MESSAGING_DOMAIN = "http://messaging.o2online.ie";
	private static final String COOKIE_EQUALS = "=";
	private static final String COOKIE_SEMI_COLON = "; ";

	private static final String SESSION_URL = MESSAGING_BASE_URL + "ssomanager.osp?APIID=AUTH-WEBSSO&TargetApp=o2om_smscenter_new.osp%3FMsgContentID%3D-1%26SID%3D_";
	private static final String SMS_BASE_URL = MESSAGING_BASE_URL + "o2om_smscenter_new.osp?MsgContentID=-1&SID=_&SID=";
	private static final String SMS_PRE = "spn_WebtextFree\">";
	private static final String SMS_POST = "</span>";

	private static final String SEND_URL = MESSAGING_BASE_URL + "smscenter_send.osp";
	private static final String SEND_POST_SID = "SID";
	private static final String SEND_POST_TO = "SMSTo";
	private static final String SEND_POST_TEXT = "SMSText";
	private static final String SEND_SUCCESS = "isSuccess : true";
	
	private static final String VERIFY_TEXT = "Webtext Security Process";
	private static final String VERIFY_SEND_URL = LOGIN_PRE_URL + "webtext-security-process/send-verification-handler.php";
	private static final String VERIFY_POST_URL = LOGIN_PRE_URL + "webtext-security-process/security-code-verification-handler.php";
	private static final String VERIFY_CODE = "SecurityCode";

	private String remainingSMS;
	private String sessionId;
	private boolean isRoaming;

	private Context context;
	private Handler handler;
	private LayoutInflater inflater;
	private EditText verificationEditText;

	public O2(final Account account) {
		super(account);
	}

	@Override
	OperationResult doLogin() {
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

		final boolean isSuccess = html.contains(LOGIN_SUCCESS);
		return isSuccess ? Success.LOGGED_IN : Fail.LOGIN_FAILED;
	}

	private void prepareWebtextSession() {
		ConnectionManager manager = new ConnectionManager(SESSION_URL, GET, false);
		String html = manager.connect();

		if (html.contains(VERIFY_TEXT)) {
			this.isRoaming = true;
		} else {
			this.sessionId = HTMLParser.parseHtml(html, SESSION_SID_PRE, SESSION_SID_POST);
	
			manager = new ConnectionManager(SMS_BASE_URL + this.sessionId, GET, false);
			manager.setRequestHeader(HTTP_COOKIE, this.getCookieHeader());
			html = manager.connect();
			this.remainingSMS = HTMLParser.parseHtml(html, SMS_PRE, SMS_POST);
		}
	}

	private String getCookieHeader() {
		final URI oUri = URI.create(COOKIE_O2_DOMAIN);
		final URI mUri = URI.create(COOKIE_MESSAGING_DOMAIN);
		final StringBuilder cookieHeader = new StringBuilder();
		final CookieManager cookieMgr = (CookieManager) CookieHandler.getDefault();

		final List<HttpCookie> cookies = new ArrayList<HttpCookie>(cookieMgr.getCookieStore().get(oUri));
		cookies.addAll(cookieMgr.getCookieStore().get(mUri));
		for (int i = 0; i < cookies.size(); i++) {
			if (i != 0) {
				cookieHeader.append(COOKIE_SEMI_COLON);
			}
			final HttpCookie cookie = cookies.get(i);
			cookieHeader.append(cookie.getName());
			cookieHeader.append(COOKIE_EQUALS);
			cookieHeader.append(cookie.getValue());
		}
		return cookieHeader.toString();
	}

	@Override
	int doGetRemainingSMS() {
		return this.remainingSMS == null ? -1 : Integer.parseInt(this.remainingSMS);
	}

	@Override
	public void preSend(final Context context) {
		this.context = context;
		this.handler = new Handler(context.getMainLooper());
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	OperationResult doSend(final List<String> recipients, final String message) {
		this.handleRoaming();
		final ConnectionManager manager = this.buildSendManager(recipients, message);
		final String rawJson = manager.connect();

		final boolean isSent = rawJson.contains(SEND_SUCCESS);
		return isSent ? Success.MESSAGE_SENT : Fail.MESSAGE_FAILED;
	}

	private void handleRoaming() {
		if (this.isRoaming) {
			ConnectionManager manager = new ConnectionManager(VERIFY_SEND_URL, GET, false);
			manager.setRequestHeader(HTTP_COOKIE, this.getCookieHeader());
			manager.connect();

			final String code = this.getVerificationCode();
			manager = new ConnectionManager(VERIFY_POST_URL);
			manager.setRequestHeader(HTTP_COOKIE, this.getCookieHeader());
			manager.addPostHeader(VERIFY_CODE, code);
			final String html = manager.connect();

			prepareWebtextSession();
		}
	}

	private String getVerificationCode() {
		this.displayVerificationDialog();
		while (this.verificationEditText == null || !this.verificationEditText.isShown()) { // wait until dialog appears
			this.waitFor(100);
		}
		while (this.verificationEditText.isShown()) { // wait until dialog is dismissed
			this.waitFor(100);
		}
		return this.verificationEditText.getText().toString();
	}

	private void displayVerificationDialog() {
		this.handler.post(new Runnable() {
			@Override
			public void run() {
				final View layout = O2.this.inflater.inflate(R.layout.verification_code_dialog, null);
				O2.this.verificationEditText = (EditText) layout.findViewById(R.id.text_verification_code);

				final Builder builder = new AlertDialog.Builder(O2.this.context);
				builder.setTitle(R.string.enter_verification_code);
				builder.setView(layout);
				builder.setPositiveButton(R.string.ok, null);

				final AlertDialog dialog = builder.create();
				dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				dialog.show();
			}
		});
	}

	private void waitFor(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private ConnectionManager buildSendManager(final List<String> recipients, final String message) {
		final StringBuilder sb = new StringBuilder();
		for (final String recipient : recipients) {
			sb.append(recipient);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1); // remove trailing comma

		final ConnectionManager manager = new ConnectionManager(SEND_URL);
		manager.setRequestHeader(HTTP_COOKIE, this.getCookieHeader());
		manager.setRequestHeader(HTTP_REFERER, SMS_BASE_URL + this.sessionId);
		manager.addPostHeader(SEND_POST_SID, this.sessionId);
		manager.addPostHeader(SEND_POST_TO, sb.toString());
		manager.addPostHeader(SEND_POST_TEXT, message);
		return manager;
	}

	@Override
	int doGetCharacterLimit() {
		return 160;
	}
}