package com.icc.net;

import com.icc.acc.Account;

/**
 * Created by Rob Powell on 04/10/13.
 */
public class Three extends Operator {

	public Three(final Account account) {
		super(account);
	}

	@Override
	public boolean login() {
		final ConnectionManager loginManager = new ConnectionManager("https://webtexts.three.ie/webtext/users/login");
		loginManager.addPostHeader("UserTelephoneNo", this.getAccount().getMobileNumber());
		loginManager.addPostHeader("UserPin", this.getAccount().getPassword());
		final String loginHtml = loginManager.doConnection();

		return loginHtml.contains("Sign out");
	}

	@Override
	public String send(final String recipient, final String message) {
		return null;
	}
}
