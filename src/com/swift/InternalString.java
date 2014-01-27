package com.swift;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Internal Strings used in the application that are not applicable to the String.xml resource
 * 
 * @author Rob Powell
 */
public class InternalString {

	/**
	 * This is the main preference key used to access preferences
	 */
	public static final String PREFS_KEY = "com.swift.app.prefs";

	/**
	 * This key is used to store the ID of the Active account in ICC
	 */
	public static final String ACTIVE_ACCOUNT = "activeAccount";

	/**
	 * This is the default character used to separate contacts when entering multiple recipients.
	 */
	public static final String DEFAULT_CONTACT_SEPARATOR = ", ";

	/**
	 * This is a list of all possible contact separator characters
	 */
	public static final String[] CONTACT_SEPARATOR = new String[] { ",", ";" };

	/**
	 * The space character.
	 */
	public static final String SPACE = " ";

	/**
	 * An empty string. Does not contain spaces.
	 */
	public static final String EMPTY_STRING = "";

	/**
	 * The Uri Schema used to send a phone number when an Intent is sending a message.
	 */
	public static final String SMSTO = "smsto:";

	/**
	 * The key used to store the message body when an Intent is sending a message.
	 */
	public static final String SMS_BODY = "sms_body";

	/**
	 * A ': ' character
	 */
	public static final String COLON_SPACE = ": ";

	/**
	 * This is the SMS Content Provider {@link Uri} of the SMS sentbox.
	 */
	public static final String SMS_PROVIDER_SENTBOX_URI = "content://sms/sent";

	/**
	 * This is the key used to store the receiver of an SMS message in the SMS Content Provider.
	 */
	public static final String SMS_PROVIDER_MESSAGE_BODY = "body";

	/**
	 * This is the key used to store the message body of an SMS message in the SMS Content Provider.
	 */
	public static final String SMS_PROVIDER_MESSAGE_ADDRESS = "address";

	/**
	 * The message to enter in the logs when inserting a sent SMS to the SMS provider fails.
	 */
	public static final String SMS_PROVIDER_FAILURE = "Could not insert message to SMS provider";

	/**
	 * The message to log when changing the android.permission.WRITE_SMS permission programmatically fails.
	 */
	public static final String FAILED_TO_ENABLE_WRITE_PERMISSION = "Failed to enable WRITE_SMS permissions";

	/**
	 * The tag to use when logging messages.
	 */
	public static final String LOG_TAG = "com.swift.log";

	/**
	 * Key used to pass an operator object in an {@link Intent}'s {@link Bundle}.
	 */
	public static final String OPERATOR = "network_operator";
}