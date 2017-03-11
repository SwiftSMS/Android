package org.swiftsms.utils;

public class HTMLParser {

	/**
	 * This method is used to extract a piece of text from a String.
	 * 
	 * @param html
	 *            The larger String containing the text to be extracted.
	 * @param prefix
	 *            A unique String that occurs directly before the sought text.
	 * @param postfix
	 *            The String that occurs directly after the sought text.
	 * @return The String between the pre & post fix.
	 */
	public static String parseHtml(final String html, final String prefix, final String postfix) {
		final int startPos = html.indexOf(prefix) + prefix.length();
		final int endPos = html.indexOf(postfix, startPos);

		String targetStr = null;
		if (startPos > prefix.length()) {
			targetStr = html.substring(startPos, endPos);
		}
		return targetStr;
	}

	/**
	 * This method is used to extract an integer from a String.
	 *
	 * @param html
	 *            The larger String containing the integer to be extracted.
	 * @param prefix
	 *            A unique String that occurs directly before the sought integer.
	 * @param postfix
	 *            The String that occurs directly after the sought integer.
	 * @return The integer between the pre & post fix.
	 * @throws NumberFormatException If the text between the pre & post fix isn't a number.
	 */
	public static int parseIntFromHtml(final String html, final String prefix, final String postfix) {
		final String targetStr = parseHtml(html, prefix, postfix);

		if (targetStr != null) {
			return Integer.parseInt(targetStr);
		}
		return -1;
	}
}