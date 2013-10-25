package com.icc.utils;

import static com.icc.InternalString.CONTACT_SEPARATOR;
import static com.icc.InternalString.EMPTY_STRING;
import static com.icc.InternalString.SPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to help with entering multiple contacts in the UI Recipients EditText.
 */
public class MultipleContactUtilities {

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
	public static String getEnteredContacts(final String recipients) {
		if (hasMultipleContacts(recipients)) {
			final int lastComma = getPositionOfLastSeparator(recipients);
			return recipients.substring(0, lastComma + 1) + SPACE;
		}
		return EMPTY_STRING;
	}

	/**
	 * This method is used to get only the last entered contact from a string. It finds the last comma (,) in the string and
	 * returns anything after that. It does no validation that the contacts are valid.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts separated by commas (,).
	 * @return A String containing the last contact.
	 */
	public static String getLastContact(final String recipients) {
		if (hasMultipleContacts(recipients)) {
			final int lastComma = getPositionOfLastSeparator(recipients);
			return recipients.substring(lastComma).trim();
		}
		return recipients.trim();
	}

	/**
	 * This method is used to convert a String version of a list of contacts to an array of formatted numbers that can be used
	 * for sending an SMS.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts separated by commas (,).
	 * @return A list of numbers taken from the contacts in String.
	 */
	public static List<String> getEnteredContactsAsList(final String recipients) {
		final List<String> listOfRecip = new ArrayList<String>();
		final String[] tokens = recipients.trim().split(CONTACT_SEPARATOR);
		for (final String recipient : tokens) {
			final Pattern pattern = Pattern.compile(".*?(\\+?\\d+).*");
			final Matcher matcher = pattern.matcher(recipient);
			matcher.find();
			listOfRecip.add(matcher.group(1));
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

}