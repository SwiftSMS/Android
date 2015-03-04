package com.swift.io.net;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.webkit.CookieSyncManager;

import com.swift.model.Account;
import com.swift.tasks.Status;
import com.swift.tasks.results.Fail;
import com.swift.tasks.results.OperationResult;

/**
 * This class contains all of the generic methods and actions needed for interaction with an operators website.
 */
public abstract class Operator {

	public static final int DEFAULT_CHAR_LIMIT = 160;

	private final Account account;
	private int characterLimit = -1;

	/**
	 * Create a new Operator using the provided account.
	 * 
	 * @param account
	 *            The users {@link Account}.
	 */
	public Operator(final Account account) {
		this.account = account;
	}

	/**
	 * This method is used to login to an operators website. This method will perform any non-network-specific login actions.
	 * Each sub-class of {@link Operator} will implement the specific login algorithm in the {@link #doLogin()} method.
	 * 
	 * @return <code>true</code> if the login was successful else <code>false</code>
	 */
	public final OperationResult login() {
		CookieSyncManager.getInstance().sync();
		try {
			return this.doLogin();
		} catch (final NoInternetAccessException e) {
			return Fail.NO_INTERNET_CONNECTION;
		}
	}

	/**
	 * This method is responsible for performing the operator specific login actions.
	 * 
	 * @return <code>true</code> if the login was successful else <code>false</code>
	 */
	abstract OperationResult doLogin();

	/**
	 * This method is used to get the users remaining SMS count from an operators website. This method will perform any
	 * non-network-specific actions. Each sub-class of {@link Operator} will implement the specific get algorithm in the
	 * {@link #doGetRemainingSMS()} method.
	 * 
	 * @return the number of remaining SMS messages the user has or <code>-1</code> if it can't be determined.
	 */
	public final int getRemainingSMS() {
		try {
			final int smsCount = this.doGetRemainingSMS();
			if (smsCount == -1) {
				this.login();
				return this.doGetRemainingSMS();
			}
			return smsCount;
		} catch (final NoInternetAccessException e) {
			return -1;
		}
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
	public final OperationResult send(final List<String> list, final String message) {
		final List<String> msgParts = this.getParts(message, this.getCharacterLimit());
		OperationResult sendStatus = Fail.MESSAGE_FAILED;
		try {
			for (final String msgToSend : msgParts) {
				final String encodedMsg = Uri.encode(msgToSend);
				sendStatus = this.doSend(list, encodedMsg);
				if (sendStatus.getStatus() == Status.FAILED) {
					this.login();
					sendStatus = this.doSend(list, encodedMsg);
				}
			}
			return sendStatus;
		} catch (final NoInternetAccessException e) {
			return Fail.NO_INTERNET_CONNECTION;
		}
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
	private List<String> getParts(final String string, final int partitionSize) {
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
	abstract OperationResult doSend(final List<String> list, final String message);

	public final int getCharacterLimit() {
		try {
			if (this.characterLimit == -1) {
				this.characterLimit = this.doGetCharacterLimit();
				if (this.characterLimit == -1) {
					this.login();
					this.characterLimit = this.doGetCharacterLimit();
				}
			}
			return this.characterLimit == -1 ? DEFAULT_CHAR_LIMIT : this.characterLimit;
		} catch (final NoInternetAccessException e) {
			return DEFAULT_CHAR_LIMIT;
		}
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
}