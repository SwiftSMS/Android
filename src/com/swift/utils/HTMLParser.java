package com.swift.utils;

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
}