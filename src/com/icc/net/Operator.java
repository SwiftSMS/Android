package com.icc.net;

import java.net.CookieHandler;
import java.net.CookieManager;

import com.icc.model.Account;

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
	 * This method is used to get the users remaining SMS count from an operators website. This method will perform any
	 * non-network-specific actions. Each sub-class of {@link Operator} will implement the specific get algorithm in the
	 * {@link #doGetRemainingSMS()} method.
	 * 
	 * @return the number of remaining SMS messages the user has or <code>-1</code> if it can't be determined.
	 */
	public final int getRemainingSMS() {
		this.login();
		final int smsCount = this.doGetRemainingSMS();
		if (smsCount == -1) {
			this.retryLogin();
			return this.doGetRemainingSMS();
		}
		return smsCount;
	}

	/**
	 * This method is responsible for performing the retrieval of the users remaining SMS count from the operator website.
	 * 
	 * @return The remaining SMS count or <code>-1</code> if it can't be determined.
	 */
	abstract int doGetRemainingSMS();

	/**
	 * This method is used for sending an SMS message through the operators website. This method will perform any
	 * non-network-specific send actions. Each sub-class of {@link Operator} will implement the specific send algorithm in the
	 * {@link #doSend(String, String)} method.
	 * 
	 * @param recipient
	 *            The phone number the message will be send to. Currently only one recipient per message.
	 * @param message
	 *            The message to send.
	 * @return <code>true</code> if the message was sent successfully else <code>false</code>
	 */
	public final boolean send(final String recipient, final String message) {
		this.login();
		final boolean sendStatus = this.doSend(recipient, message);
		if (!sendStatus) {
			this.retryLogin();
			return this.doSend(recipient, message);
		}
		return sendStatus;
	}

	/**
	 * This method is responsible for sending the actual SMS message through the operators website.
	 * 
	 * @param recipient
	 *            The phone number the message will be send to. Currently only one recipient per message.
	 * @param message
	 *            The message to send.
	 * @return <code>true</code> if the message was sent successfully else <code>false</code>
	 */
	abstract boolean doSend(final String recipient, final String message);

	/**
	 * This method returns the {@link Account} used to construct this instance of {@link Operator}. It contains the users
	 * details.
	 * 
	 * @return The users {@link Account}
	 */
	Account getAccount() {
		return this.account;
	}

	/**
	 * This method will removed the cached login and make a login attempt.
	 */
	private void retryLogin() {
		this.isLoggedIn = false;
		this.login();
	}
}