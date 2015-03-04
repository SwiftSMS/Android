package com.swift.utils;

import static com.swift.InternalString.CONTACT_SEPARATOR;
import static com.swift.InternalString.DEFAULT_CONTACT_SEPARATOR;
import static com.swift.InternalString.EMPTY_STRING;
import static com.swift.InternalString.SPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class to help with entering multiple contacts in the UI Recipients EditText.
 */
public class ContactUtils {

	private static final String NON_NUMBER_PATTERN = "[^\\d\\+]";
	private final static String SEPARATOR_PATTERN;
	private final static String IRISH_PREFIXS = "^(\\+353|00353|353)";
	private final static String ZERO = "0";

	static {
		final StringBuilder pattern = new StringBuilder();
		pattern.append("[");
		for (final String s : CONTACT_SEPARATOR) {
			pattern.append(s);
		}
		pattern.append("]");
		SEPARATOR_PATTERN = pattern.toString();
	}

	/**
	 * Method to break a {@link List} into multiple smaller lists.
	 * 
	 * @param list
	 *            The larger list to be broken down.
	 * @param length
	 *            The max length of a returned {@link List}.
	 * @return A {@link List} containing the smaller broken down lists.
	 */
	public static <T> List<List<T>> chopped(final List<T> list, final int length) {
		final List<List<T>> parts = new ArrayList<List<T>>();
		final int N = list.size();
		for (int i = 0; i < N; i += length) {
			final List<T> sublist = list.subList(i, Math.min(N, i + length));
			parts.add(new ArrayList<T>(sublist));
		}
		return parts;
	}

	/**
	 * This method determines if there is more than one contact entered in a String. This is determined by the separator
	 * character. If the separator character exists in the string it is assumed more than one contact exists in the string.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts.
	 * @return <code>true</code> if there is more than one contact, otherwise <code>false</code>.
	 */
	public static boolean hasMultipleContacts(final String recipients) {
		return getPositionOfLastSeparator(recipients) != -1;
	}

	/**
	 * This method is used to get only the fully entered contacts from a string. It returns everything up to the last separator
	 * character. It does no validation that the contacts are valid.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts.
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
	 *            A string containing one or more contacts.
	 * @return A list of numbers taken from the contacts in String.
	 */
	public static List<String> getContactsAsList(final String recipients) {
		final List<String> listOfRecip = new ArrayList<String>();
		final String[] tokens = trimSeparators(recipients).split(SEPARATOR_PATTERN);
		for (final String unformattedRecipient : tokens) {
			final String recipientNumber = stripRecipientName(unformattedRecipient);
			final String recipient = recipientNumber.replaceAll(NON_NUMBER_PATTERN, EMPTY_STRING);
			if (!recipient.isEmpty()) {
				listOfRecip.add(recipient);
			}
		}
		return removeIrishPrefixFromRecipients(listOfRecip);
	}

	private static String stripRecipientName(final String unformattedRecipient) {
		final int startOfNumber = unformattedRecipient.lastIndexOf('(') + 1;
		final int endOfNumber = unformattedRecipient.lastIndexOf(')');
		if (startOfNumber != 0 && endOfNumber != -1) {
			return unformattedRecipient.substring(startOfNumber, endOfNumber);
		}
		return unformattedRecipient;
	}

	/**
	 * This method finds the position of the last separator character in a string.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts.
	 * @return The position of the last separator character in this string.
	 */
	private static int getPositionOfLastSeparator(final String recipients) {
		final Pattern pattern = Pattern.compile(".*" + SEPARATOR_PATTERN);
		final Matcher matcher = pattern.matcher(recipients);

		if (matcher.find()) {
			return matcher.end() - 1;
		} else {
			return -1;
		}
	}

	/**
	 * This method is used to remove unneeded contact separator characters from a String. It will remove all contact separator
	 * characters from the string if they are 'empty'.
	 * 
	 * @param recipients
	 *            A string containing one or more contacts.
	 * @return A String containing the recipients without leading or trailing separator characters.
	 */
	public static String trimSeparators(final String recipients) {
		final StringBuilder result = new StringBuilder();
		final String[] tokens = recipients.trim().split(SEPARATOR_PATTERN);
		for (final String token : tokens) {
			final String recipient = token.trim();
			if (!recipient.equals(EMPTY_STRING)) {
				if (result.length() > 0) {
					result.append(DEFAULT_CONTACT_SEPARATOR);
				}
				result.append(recipient);
			}
		}
		return result.toString();
	}

	/**
	 * This method is used to replace the Irish +353 prefix with a 0.
	 * 
	 * @param recipient
	 *            A phone number to remove the Irish prefix from.
	 * @return The String containing the number with the new prefix of zero.
	 */
	public static String removeIrishPrefix(final String recipient) {
		if (recipient == null) {
			return null;
		}
		return recipient.replaceAll(IRISH_PREFIXS, ZERO);
	}

	/**
	 * This method is used to replace the Irish +353 prefix with a 0.
	 * 
	 * @param recipients
	 *            Recipient List to remove Irish prefix from, '+353' to '0'.
	 * @return A List containing the recipients with the new prefix of zero.
	 */
	public static List<String> removeIrishPrefixFromRecipients(final List<String> recipients) {
		final List<String> newRecipients = new ArrayList<String>();

		for (final String recipient : recipients) {
			newRecipients.add(removeIrishPrefix(recipient));
		}
		return newRecipients;
	}

	/**
	 * Method to check if a String is a valid phone number.
	 * <p>
	 * A valid phone number contains only numbers and can optionally start with a +.
	 * </p>
	 * 
	 * @param number
	 *            The String to be tested.
	 * @return <code>true</code> if the String is a valid phone number, otherwise <code>false</code>.
	 */
	public static boolean isNumber(final String number) {
		if (number == null) {
			return false;
		}
		return number.matches("\\+?\\d{7,}");
	}
}