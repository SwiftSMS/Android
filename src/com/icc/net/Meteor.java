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
		loginManager.addRequestHeader("username", this.getAccount().getMobileNumber());
		loginManager.addRequestHeader("userpass", this.getAccount().getPassword());
		loginManager.addRequestHeader("login", "");
		loginManager.addRequestHeader("returnTo", "/");
		return loginManager.doConnection();
	}

	@Override
	public String send(final String recipient, final String message) {
		// TODO Auto-generated method stub
		return null;
	}
}