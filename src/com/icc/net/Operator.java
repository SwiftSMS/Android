package com.icc.net;

import java.net.CookieHandler;
import java.net.CookieManager;

import com.icc.acc.Account;

public abstract class Operator {

	private final Account account;

	public Operator(final Account account) {
		this.account = account;
		CookieHandler.setDefault(new CookieManager());
	}

	public abstract String login();

	public abstract String send(final String recipient, final String message);

	Account getAccount() {
		return this.account;
	}
}