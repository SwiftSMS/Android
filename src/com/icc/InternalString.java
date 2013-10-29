package com.icc;

/**
 * Internal Strings used in the application that are not applicable to the String.xml resource
 * 
 * @author Rob Powell
 */
public class InternalString {

	/**
	 * This is the main preference key used to access preferences
	 */
	public static final String PREFS_KEY = "com.icc.app.prefs";

	/**
	 * This key is used to store the ID of the latest account added to ICC
	 */
	public static final String LATEST_ACCOUNT = "latestAccount";

	/**
	 * This key is used to store the ID of the Active account in ICC
	 */
	public static final String ACTIVE_ACCOUNT = "activeAccount";

	/**
	 * This is the character used to separate contacts when entering multiple recipients.
	 */
	public static final String CONTACT_SEPARATOR = ",";

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
}