package com.icc.net;

public class Vodafone {

	private final String username;
	private final String password;

	public Vodafone(final String username, final String password) {
		this.username = username;
		this.password = password;
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

	public String login() {
		final ConnectionManager loginManager = new ConnectionManager("https://www.vodafone.ie/myv/services/login/Login.shtml");
		loginManager.addRequestHeader("username", this.username);
		loginManager.addRequestHeader("password", this.password);
		loginManager.doConnection();

		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/messaging/webtext/index.jsp");
		return manager.doConnection();
	}

	public String send(final String recipient, final String message) {
		final ConnectionManager manager = new ConnectionManager("https://www.vodafone.ie/myv/messaging/webtext/Process.shtml");
		manager.addRequestHeader("org.apache.struts.taglib.html.TOKEN", "MY TOKEN");
		manager.addRequestHeader("message", message);
		manager.addRequestHeader("recipients[0]", recipient);
		manager.addRequestHeader("&jcaptcha_response", "MY CAPTCHA");

		return manager.doConnection();
	}
}