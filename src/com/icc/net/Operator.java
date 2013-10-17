package com.icc.net;

import java.net.CookieHandler;
import java.net.CookieManager;

import com.icc.acc.Account;

/**
 * This class contains all of the generic methods and actions needed for interaction with an operators website.
 */
public abstract class Operator {

	private final Account account;
	private boolean isLoggedIn = false;

	/**
	 * Create a new Operator using the provided account.
	 * 
	 * @param account
	 *            The users {@link Account}.
	 */
	public Operator(final Account account) {
		this.account = account;
		CookieHandler.setDefault(new CookieManager());
	}

	/**
	 * This method is used to login to an operators website. This method will perform any non-network-specific login actions.
	 * Each sub-class of {@link Operator} will implement the specific login algorithm in the {@link #doLogin()} method.
	 * 
	 * @return <code>true</code> if the login was successful else <code>false</code>
	 */
	public final boolean login() {
		if (!this.isLoggedIn) {
			this.isLoggedIn = this.doLogin();
		}
		return this.isLoggedIn;
	}

	/**
	 * This method is responsible for performing the operator specific login actions.
	 * 
	 * @return <code>true</code> if the login was successful else <code>false</code>
	 */
	abstract boolean doLogin();

	/**
	 * This method is responsible for retrieving the users remaining SMS count from the operator website.
	 * 
	 * @return The remaining SMS count.
	 */
	public abstract int getRemainingSMS();

	/**
	 * This method is responsible for sending the actual SMS message through the operators website.
	 * 
	 * @param recipient
	 *            The phone number the message will be send to. Currently only one recipient per message.
	 * @param message
	 *            The message to send.
	 * @return <code>true</code> if the message was sent successfully else <code>false</code>
	 */
	public abstract boolean send(final String recipient, final String message);

	/**
	 * This method returns the {@link Account} used to construct this instance of {@link Operator}. It contains the users
	 * details.
	 * 
	 * @return The users {@link Account}
	 */
	Account getAccount() {
		return this.account;
	}
}