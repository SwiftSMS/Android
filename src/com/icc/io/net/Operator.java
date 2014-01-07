package com.icc.io.net;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.icc.InternalString;
import com.icc.model.Account;
import com.icc.tasks.Status;

/**
 * This class contains all of the generic methods and actions needed for interaction with an operators website.
 */
public abstract class Operator {

	public static final int DEFAULT_CHAR_LIMIT = 160;

	private final Account account;
	private boolean isLoggedIn = false;
	private int characterLimit = -1;

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

	public void preSend(final Context context) {
	}

	/**
	 * This method is responsible for sending an SMS message through the operators website. This method will perform any
	 * non-network-specific send actions. Each sub-class of {@link Operator} will implement the specific send algorithm in the
	 * {@link #doSend(String, String)} method.
	 * 
	 * @param list
	 *            A list of phone numbers the message will be sent to.
	 * @param message
	 *            The message to send.
	 * @return <code>true</code> if the message was sent successfully else <code>false</code>
	 */
	public final Status send(final List<String> list, final String message) {
		this.login();
		final List<String> msgParts = getParts(message, this.getCharacterLimit());
		Status sendStatus = Status.FAILED;
		for (final String msgToSend : msgParts) {
			sendStatus = this.doSend(list, msgToSend);
			if (sendStatus == Status.FAILED) {
				this.retryLogin();
				sendStatus = this.doSend(list, msgToSend);
			}
		}
		return sendStatus;
	}

	/**
	 * This method is used to break a String (message) into parts with a maximum length of the partition size provided.
	 * 
	 * @param string
	 *            The message to be split into parts.
	 * @param partitionSize
	 *            The maximum size of each part.
	 * @return A list of strings broken into parts.
	 */
	private static List<String> getParts(final String string, final int partitionSize) {
		final List<String> parts = new ArrayList<String>();
		final int len = string.length();
		for (int i = 0; i < len; i += partitionSize) {
			parts.add(string.substring(i, Math.min(len, i + partitionSize)));
		}
		return parts;
	}

	/**
	 * This method is responsible for sending the actual SMS message through the operators website.
	 * 
	 * @param list
	 *            A list of phone numbers the message will be sent to.
	 * @param message
	 *            The message to send.
	 * @return <code>true</code> if the message was sent successfully else <code>false</code>
	 */
	abstract Status doSend(final List<String> list, final String message);

	public final int getCharacterLimit() {
		if (this.characterLimit == -1) {
			this.login();
			this.characterLimit = this.doGetCharacterLimit();
			if (this.characterLimit == -1) {
				this.retryLogin();
				this.characterLimit = this.doGetCharacterLimit();
			}
		}
		return this.characterLimit == -1 ? DEFAULT_CHAR_LIMIT : this.characterLimit;
	}

	abstract int doGetCharacterLimit();

	/**
	 * This method returns the {@link Account} used to construct this instance of {@link Operator}. It contains the users
	 * details.
	 * 
	 * @return The users {@link Account}
	 */
	public Account getAccount() {
		return this.account;
	}

	/**
	 * This method will removed the cached login and make a login attempt.
	 */
	private void retryLogin() {
		Log.d(InternalString.LOG_TAG, "retryLogin - Login expired");
		this.isLoggedIn = false;
		this.login();
	}
}