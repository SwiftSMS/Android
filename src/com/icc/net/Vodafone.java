package com.icc.net;

import java.util.List;

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

		return loginHtml.contains("Sign out");
	}

	@Override
	boolean doSend(final List<String> recipients, final String message) {
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/messaging/webtext/Process.shtml");
		manager.addPostHeader("org.apache.struts.taglib.html.TOKEN", "MY TOKEN");
		manager.addPostHeader("message", message);
		for (int i = 0; i < recipients.size(); i++) {
			manager.addPostHeader("recipients[" + i + "]", recipients.get(i));
		}
		manager.addPostHeader("&jcaptcha_response", "MY CAPTCHA");
		final String html = manager.doConnection();

		return html.contains("Sent");
	}

	@Override
	int doGetRemainingSMS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int doGetCharacterLimit() {
		return 459;
	}
}