package com.icc.utils;

import static com.icc.InternalString.CONTACT_SEPARATOR;
import static com.icc.InternalString.EMPTY_STRING;
import static com.icc.InternalString.SPACE;

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

	private static int getPositionOfLastSeparator(final String recipients) {
		return recipients.lastIndexOf(CONTACT_SEPARATOR);
	}

}