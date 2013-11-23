package com.icc.utils;

import static com.icc.InternalString.CONTACT_SEPARATOR;
import static com.icc.InternalString.EMPTY_STRING;
import static com.icc.InternalString.SPACE;

import java.util.ArrayList;
import java.util.List;

import android.telephony.PhoneNumberUtils;

/**
 * A utility class to help with entering multiple contacts in the UI Recipients EditText.
 */
public class ContactUtils {

	/**
	 * This method determines if there is more than one contact entered in a String. This is determined by the separator
	 * character. If the separator character exists in the string it is assumed more than one contact exists in the string.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts separated by commas (,).
	 * @return <code>true</code> if there is more than one contact, otherwise <code>false</code>.
	 */
	public static boolean hasMultipleContacts(final String recipients) {
		return getPositionOfLastSeparator(recipients) != -1;
	}

	/**
	 * This method is used to get only the fully entered contacts from a string. It returns everything up to the last comma (,)
	 * in the string. It does no validation that the contacts are valid.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts separated by commas (,).
	 * @return A String containing all the fully entered contacts.
	 */
	public static String getAllButLastContacts(final String recipients) {
		if (hasMultipleContacts(recipients)) {
			final int lastComma = getPositionOfLastSeparator(recipients);
			return recipients.substring(0, lastComma + 1) + SPACE;
		}
		return EMPTY_STRING;
	}

	/**
	 * This method returns the text that follows the last contact separator in the string. If no separators exist in the string
	 * the string itself will be returned.
	 * 
	 * @param recipients
	 *            The string to search.
	 * @return The last contact in the string.
	 */
	public static String getLastContact(final String recipients) {
		if (hasMultipleContacts(recipients)) {
			final int lastComma = getPositionOfLastSeparator(recipients);
			return recipients.substring(lastComma + 1).trim();
		}
		return recipients;
	}

	/**
	 * This method is used to convert a String version of a list of contacts to an array of formatted numbers that can be used
	 * for sending an SMS.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts separated by commas (,).
	 * @return A list of numbers taken from the contacts in String.
	 */
	public static List<String> getContactsAsList(final String recipients) {
		final List<String> listOfRecip = new ArrayList<String>();
		final String[] tokens = trimSeparators(recipients).split(CONTACT_SEPARATOR);
		for (final String unformattedRecipient : tokens) {
			final String recipient = PhoneNumberUtils.stripSeparators(unformattedRecipient);
			listOfRecip.add(recipient);
		}
		return listOfRecip;
	}

	/**
	 * This method finds the position of the last comma (,) in a string.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts separated by commas (,).
	 * @return The position of the last comma in this string.
	 */
	private static int getPositionOfLastSeparator(final String recipients) {
		return recipients.lastIndexOf(CONTACT_SEPARATOR);
	}

	/**
	 * This method is used to remove unneeded contact separator characters (comma) from a String. It will remove all contact
	 * separator characters from the string if they are 'empty'.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts separated by commas (,).
	 * @return A String containing the recipients without trailing commas (,).
	 */
	public static String trimSeparators(final String recipients) {
		final StringBuilder result = new StringBuilder();
		final String[] tokens = recipients.trim().split(CONTACT_SEPARATOR);
		for (final String token : tokens) {
			final String recipient = token.trim();
			if (!recipient.equals(EMPTY_STRING)) {
				if (result.length() > 0) {
					result.append(CONTACT_SEPARATOR + SPACE);
				}
				result.append(recipient);
			}
		}
		return result.toString();
	}
}