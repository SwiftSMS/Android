package com.icc.net;

import com.icc.acc.Account;

public class Meteor extends Operator {

	private String cfId;
	private String cfToken;

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
		loginManager.addPostHeader("login", "");
		loginManager.addPostHeader("returnTo", "/");
		loginManager.doConnection();

		final ConnectionManager messageManager = new ConnectionManager("https://www.mymeteor.ie/go/freewebtext");
		messageManager.doConnection();
		final String htmlWithIds = messageManager.getResponseOutput();
		this.cfId = this.extractSessionVar("var CFID = ", ";", htmlWithIds);
		this.cfToken = this.extractSessionVar("var CFTOKEN = ", ";", htmlWithIds);
		return String.format("CFID = %s%nCFTOKEN = %s", this.cfId, this.cfToken);
	}

	private String extractSessionVar(final String startChars, final String endChars, final String text) {
		final int start = text.indexOf(startChars) + startChars.length();
		return text.substring(start, text.indexOf(endChars, start));
	}

	@Override
	public String send(final String recipient, final String message) {
		final String addUrl = String.format(
				"https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&CFID=%s&CFTOKEN=%s&func=addEnteredMsisdns",
				this.cfId, this.cfToken);
		final ConnectionManager addManager = new ConnectionManager(addUrl);
		addManager.addPostHeader("ajaxRequest", "addEnteredMSISDNs");
		addManager.addPostHeader("remove", "-");
		addManager.addPostHeader("add", "0%7C" + recipient);
		addManager.doConnection();

		final String sendUrl = String.format(
				"https://www.mymeteor.ie/mymeteorapi/index.cfm?event=smsAjax&CFID=%s&CFTOKEN=%s&func=sendSMS", this.cfId,
				this.cfToken);
		final ConnectionManager sendManager = new ConnectionManager(sendUrl);
		sendManager.addPostHeader("ajaxRequest", "sendSMS");
		sendManager.addPostHeader("messageText", message);
		sendManager.doConnection();
		return sendManager.getResponseOutput();
	}
}