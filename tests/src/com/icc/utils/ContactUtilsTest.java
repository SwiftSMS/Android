package com.icc.utils;

import junit.framework.TestCase;

public class ContactUtilsTest extends TestCase {

	private final String tStrWithAllNonSeparators = "a!c\"m£c$a%m^c&a*e(m)c-e_m+c=a{e}m[c]a:e'm@c#a~e<m>c.a?e/m\\c|a`a¬o1b";
	private final String tStrNoContacts = "";
	private final String tStrOneContact = "first contact";
	private final String tStrTwoContacts = "first contact, last contact";
	private final String tStrThreeContactsNoLast = "first contact, second contact, ";
	private final String tStrFourContactsWithChars = "a#contact, two-contacts, three&contacts, fo*r<contacts>";
	private final String tStrContactsWithPointlessSeparators = ", ,first contact, second contact,,, another contact, , ,";

	private final String tStrTwoContactsWithSemi = "first contact; last contact";
	private final String tStrThreeContactsWithSemi = "first contact; second contact, last contact";
	private final String tStrThreeContactsWithSemiAtEnd = "first contact, second contact; last contact";
	private final String tStrFourContactsWithCharsAndSemi = "a#contact; two-contacts, three&contacts; fo*r<contacts>";
	private final String tStrContactsWithPointlessSeparatorsAndSemi = ", ;first contact; second contact,;, another contact, , ;";

	public void testHasMultipleContacts() {
		assertTrue(ContactUtils.hasMultipleContacts(this.tStrTwoContacts));
		assertTrue(ContactUtils.hasMultipleContacts(this.tStrThreeContactsNoLast));
		assertTrue(ContactUtils.hasMultipleContacts(this.tStrFourContactsWithChars));
	}

	public void testHasMultipleContactsFalse() {
		assertFalse(ContactUtils.hasMultipleContacts(this.tStrNoContacts));
		assertFalse(ContactUtils.hasMultipleContacts(this.tStrOneContact));
		assertFalse(ContactUtils.hasMultipleContacts(this.tStrWithAllNonSeparators));
	}

	public void testHasMultipleContactsWithSemiColon() {
		assertTrue(ContactUtils.hasMultipleContacts(this.tStrTwoContactsWithSemi));
		assertTrue(ContactUtils.hasMultipleContacts(this.tStrThreeContactsWithSemi));
		assertTrue(ContactUtils.hasMultipleContacts(this.tStrThreeContactsWithSemiAtEnd));
		assertTrue(ContactUtils.hasMultipleContacts(this.tStrFourContactsWithCharsAndSemi));
	}

	public void testGetAllButLastContacts() {
		assertEquals("", ContactUtils.getAllButLastContacts(this.tStrNoContacts));
		assertEquals("", ContactUtils.getAllButLastContacts(this.tStrOneContact));
		assertEquals("first contact, ", ContactUtils.getAllButLastContacts(this.tStrTwoContacts));
		assertEquals(this.tStrThreeContactsNoLast, ContactUtils.getAllButLastContacts(this.tStrThreeContactsNoLast));
		assertEquals("a#contact, two-contacts, three&contacts, ", ContactUtils.getAllButLastContacts(this.tStrFourContactsWithChars));
		assertEquals("", ContactUtils.getAllButLastContacts(this.tStrWithAllNonSeparators));
	}

	public void testGetAllButLastContactsWithSemiColon() {
		assertEquals("first contact, ", ContactUtils.getAllButLastContacts(this.tStrTwoContactsWithSemi));
		assertEquals("first contact, second contact, ", ContactUtils.getAllButLastContacts(this.tStrThreeContactsWithSemiAtEnd));
		assertEquals("first contact; second contact, ", ContactUtils.getAllButLastContacts(this.tStrThreeContactsWithSemi));
		assertEquals("a#contact; two-contacts, three&contacts, ", ContactUtils.getAllButLastContacts(this.tStrFourContactsWithCharsAndSemi));
	}

	public void testGetLastContact() {
		assertEquals("", ContactUtils.getLastContact(this.tStrNoContacts));
		assertEquals("first contact", ContactUtils.getLastContact(this.tStrOneContact));
		assertEquals("last contact", ContactUtils.getLastContact(this.tStrTwoContacts));
		assertEquals("", ContactUtils.getLastContact(this.tStrThreeContactsNoLast));
		assertEquals("fo*r<contacts>", ContactUtils.getLastContact(this.tStrFourContactsWithChars));
		assertEquals(this.tStrWithAllNonSeparators, ContactUtils.getLastContact(this.tStrWithAllNonSeparators));
	}

	public void testGetLastContactWithSemiColon() {
		assertEquals("last contact", ContactUtils.getLastContact(this.tStrTwoContactsWithSemi));
		assertEquals("last contact", ContactUtils.getLastContact(this.tStrThreeContactsWithSemiAtEnd));
		assertEquals("last contact", ContactUtils.getLastContact(this.tStrThreeContactsWithSemi));
		assertEquals("fo*r<contacts>", ContactUtils.getLastContact(this.tStrFourContactsWithCharsAndSemi));
	}

	public void testGetContactsAsList() {
		fail("Not yet implemented");
	}

	public void testTrimSeparators() {
		assertEquals("", ContactUtils.trimSeparators(this.tStrNoContacts));
		assertEquals("first contact, second contact, another contact", ContactUtils.trimSeparators(this.tStrContactsWithPointlessSeparators));
		assertEquals("first contact", ContactUtils.trimSeparators(this.tStrOneContact));
		assertEquals("first contact, last contact", ContactUtils.trimSeparators(this.tStrTwoContacts));
		assertEquals("first contact, second contact", ContactUtils.trimSeparators(this.tStrThreeContactsNoLast));
		assertEquals("a#contact, two-contacts, three&contacts, fo*r<contacts>", ContactUtils.trimSeparators(this.tStrFourContactsWithChars));
		assertEquals("a!c\"m£c$a%m^c&a*e(m)c-e_m+c=a{e}m[c]a:e'm@c#a~e<m>c.a?e/m\\c|a`a¬o1b", ContactUtils.trimSeparators(this.tStrWithAllNonSeparators));
	}

	public void testTrimSeparatorsWithSemiColon() {
		assertEquals("first contact, second contact, another contact", ContactUtils.trimSeparators(this.tStrContactsWithPointlessSeparatorsAndSemi));
		assertEquals("first contact, last contact", ContactUtils.trimSeparators(this.tStrTwoContactsWithSemi));
		assertEquals("first contact, second contact, last contact", ContactUtils.trimSeparators(this.tStrThreeContactsWithSemi));
		assertEquals("first contact, second contact, last contact", ContactUtils.trimSeparators(this.tStrThreeContactsWithSemiAtEnd));
		assertEquals("a#contact, two-contacts, three&contacts, fo*r<contacts>", ContactUtils.trimSeparators(this.tStrFourContactsWithCharsAndSemi));
	}
}