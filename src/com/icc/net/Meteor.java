package com.icc.net;

import com.icc.acc.Account;

public class Meteor extends Operator {

	public Meteor(final Account account) {
		super(account);
	}

	/**
	 * <pre>
	 * Steps to send
	 * 	1. Get session cookie
	 * 		- https://www.mymeteor.ie/go/mymeteor-login-manager
	 * 		  POST
	 * 			username=phone
	 * 			userpass=pin
	 * 			login=
	 * 			returnTo=/
	 * 		- Session cookie should be in the returned headers
	 * 		  Example
	 * 			CFID=35626768
	 * 			CFTOKEN=13595949
	 * 	2. Add recipients
	 * 		- https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&CFID=35626768&CFTOKEN=13595949&func=addEnteredMsisdns
	 * 		  POST (i think)
	 * 			ajaxRequest=addEnteredMSISDNs
	 * 			remove=-
	 * 			add=NUMBER
	 * 		- Form to replicate the adding
	 * 			forms/meteor_addrecipient.html
	 * 	3. Send message
	 * 		- https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&CFID=35626768&CFTOKEN=13595949&func=sendSMS
	 * 		  POST (i think)
	 * 			ajaxRequest=sendSMS
	 * 			messageText=MESSAGE
	 * 		- Form to replicate the adding
	 * 			forms/meteor_send.html
	 * </pre>
	 */

	@Override
	public String login() {
		final ConnectionManager loginManager = new ConnectionManager("https://www.mymeteor.ie/go/mymeteor-login-manager");
		loginManager.addPostHeader("username", this.getAccount().getMobileNumber());
		loginManager.addPostHeader("userpass", this.getAccount().getPassword());

		return loginManager.doConnection();
	}

	@Override
	public String send(final String recipient, final String message) {
		final String addUrl = "https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax";
		final ConnectionManager addManager = new ConnectionManager(addUrl);
		addManager.addPostHeader("ajaxRequest", "addEnteredMSISDNs");
		addManager.addPostHeader("remove", "-");
		addManager.addPostHeader("add", "0%7C" + recipient);
		addManager.doConnection();

		final String sendUrl = "https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax";
		final ConnectionManager sendManager = new ConnectionManager(sendUrl);
		sendManager.addPostHeader("ajaxRequest", "sendSMS");
		sendManager.addPostHeader("messageText", message);
		return sendManager.doConnection();
	}
}