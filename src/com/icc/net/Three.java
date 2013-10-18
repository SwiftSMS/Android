package com.icc.net;

import com.icc.model.Account;

/**
 * Created by Rob Powell on 04/10/13.
 */
public class Three extends Operator {

	public Three(final Account account) {
		super(account);
	}

	@Override
	boolean doLogin() {
		final ConnectionManager loginManager = new ConnectionManager("https://webtexts.three.ie/webtext/users/login");
		loginManager.addPostHeader("UserTelephoneNo", this.getAccount().getMobileNumber());
		loginManager.addPostHeader("UserPin", this.getAccount().getPassword());
		final String loginHtml = loginManager.doConnection();

		return loginHtml.contains("Sign out");
	}

	@Override
	boolean doSend(final String recipient, final String message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	int doGetRemainingSMS() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int doGetCharacterLimit() {
		return 160;
	}
}
