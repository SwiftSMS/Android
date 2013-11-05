package com.icc.net;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.icc.model.Account;

public class Vodafone extends Operator {

	public Vodafone(final Account account) {
		super(account);
	}

	/**
	 * <pre>
	 * Steps to send
	 * 	1. Get session cookie
	 * 		- https://www.vodafone.ie/myv/services/login/Login.shtml
	 * 		  POST
	 * 			username=phone
	 * 			password=pin
	 * 		- Session cookie will be in the returned headers
	 * 		- Form to replicate logging in
	 * 			form/vodafone_login.html
	 * 	2. Send message
	 * 		- https://www.vodafone.ie/myv/messaging/webtext/Process.shtml
	 * 		  POST
	 * 			org.apache.struts.taglib.html.TOKEN=xxxx
	 * 			message=My Message
	 * 			recipients[0]=NUMBER
	 * 			recipients[1]=NUMBER
	 * 			recipients[2]=NUMBER
	 * 			recipients[3]=NUMBER
	 * 			recipients[4]=NUMBER
	 * 			jcaptcha_response=CAPTCHA
	 * 		- Form to replicate the adding
	 * 			form/vodafone_send.html
	 * </pre>
	 */

	@Override
	boolean doLogin() {
		final ConnectionManager loginManager = new ConnectionManager("https://www.vodafone.ie/myv/services/login/Login.shtml");
		loginManager.addPostHeader("username", this.getAccount().getMobileNumber());
		loginManager.addPostHeader("password", this.getAccount().getPassword());
		final String loginHtml = loginManager.doConnection();

		return loginHtml.contains("302");
	}

	@Override
	boolean doSend(final List<String> recipients, final String message) {
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/messaging/webtext/Process.shtml");
		manager.addPostHeader("org.apache.struts.taglib.html.TOKEN", this.getToken());
		manager.addPostHeader("message", message);
		manager.addPostHeader("futuretime", Boolean.toString(false));
		manager.addPostHeader("futuredate", Boolean.toString(false));
		for (int i = 0; i < recipients.size(); i++) {
			manager.addPostHeader("recipients[" + i + "]", recipients.get(i));
		}
		// manager.addPostHeader("jcaptcha_response", "MY CAPTCHA");
		final String html = manager.doConnection();

		return html.contains("Message sent!");
	}

	private String getToken() {
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/messaging/webtext/index.jsp",
				"GET", false);
		final String html = manager.doConnection();

		final String charsText = "org.apache.struts.taglib.html.TOKEN\" value=\"";
		final int startPos = html.indexOf(charsText) + charsText.length();
		final int endPos = html.indexOf("\"", startPos);
		if (startPos > charsText.length()) {
			return html.substring(startPos, endPos);
		}
		return "";
	}

	@Override
	int doGetRemainingSMS() {
		final ConnectionManager manager = new ConnectionManager("http://www.vodafone.ie/myv/dashboard/webtextdetails.shtml",
				"GET", false);
		final String smsHtml = manager.doConnection();

		try {
			final JSONObject smsJson = new JSONObject(smsHtml);
			final int totalSms = smsJson.getInt("total");
			final int usedSms = smsJson.getInt("used");
			return totalSms - usedSms;
		} catch (final JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	int doGetCharacterLimit() {
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/javascript/section.myv.webtext.js",
				"GET", false);
		final String html = manager.doConnection();

		final String charsText = "var char_limit = ";
		final int startPos = html.indexOf(charsText) + charsText.length();
		final int endPos = html.indexOf(";", startPos);
		if (startPos > charsText.length()) {
			final String characterCount = html.substring(startPos, endPos);
			return Integer.valueOf(characterCount);
		} else {
			return -1;
		}
	}
}