package com.icc.utils;

import static com.icc.InternalString.CONTACT_SEPARATOR;
import static com.icc.InternalString.EMPTY_STRING;
import static com.icc.InternalString.SPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipleContactUtilities {

	public static boolean hasMultipleContacts(final String recipients) {
		return getPositionOfLastSeparator(recipients) != -1;
	}

	public static String getEnteredContacts(final String recipients) {
		final int lastComma = getPositionOfLastSeparator(recipients);
		String oldText = EMPTY_STRING;
		if (hasMultipleContacts(recipients)) {
			oldText = recipients.substring(0, lastComma + 1) + SPACE;
		}
		return oldText;
	}

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

	private static int getPositionOfLastSeparator(final String recipients) {
		return recipients.lastIndexOf(CONTACT_SEPARATOR);
	}

}