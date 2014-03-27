package com.swfit.utils;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.swift.utils.ContactUtils;

public class ContactUtilsTest extends TestCase {

	private final String tStrWithAllNonSeparators = "a!c\"m£c$a%m^c&a*e(m)1337c-e_m+c=a{e}m[c]a:e'm@c#a~e<m>c.a?e/m\\c|a`a¬o1b(058)";
	private final String tStrNoContacts = "";
	private final String tStrOneContact = "first contact";
	private final String tStrTwoContacts = "first contact (08984276), last contact";
	private final String tStrThreeContactsNoLast = "first (friend) contact (081 888 1111), second contact, ";
	private final String tStrFourContactsWithChars = "a_1337_contact#(0831236548), two-contacts, three&contacts, fo*r<con087147258tacts>";
	private final String tStrContactsWithPointlessSeparators = ", ,first contact, second contact,,, another contact, , ,";

	private final String tStrTwoContactsWithSemi = "first contact; last 10 contact (084798465)";
	private final String tStrThreeContactsWithSemi = "first_0847898789_contact; second co086131313ntact, last contact";
	private final String tStrThreeContactsWithSemiAtEnd = "first contact(+35385114411), 2nd contact(089 123 4567); last contact";
	private final String tStrFourContactsWithCharsAndSemi = "a#contact; two-contacts, three&contacts; fo*r<contacts>";
	private final String tStrContactsWithPointlessSeparatorsAndSemi = ", ;first contact (+35383 112 9933); second contact,;, another contact, +35381 222 5554, ;";

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
		assertEquals("first contact (08984276), ", ContactUtils.getAllButLastContacts(this.tStrTwoContacts));
		assertEquals(this.tStrThreeContactsNoLast, ContactUtils.getAllButLastContacts(this.tStrThreeContactsNoLast));
		assertEquals("a_1337_contact#(0831236548), two-contacts, three&contacts, ", ContactUtils.getAllButLastContacts(this.tStrFourContactsWithChars));
		assertEquals("", ContactUtils.getAllButLastContacts(this.tStrWithAllNonSeparators));
	}

	public void testGetAllButLastContactsWithSemiColon() {
		assertEquals("first contact; ", ContactUtils.getAllButLastContacts(this.tStrTwoContactsWithSemi));
		assertEquals("first contact(+35385114411), 2nd contact(089 123 4567); ", ContactUtils.getAllButLastContacts(this.tStrThreeContactsWithSemiAtEnd));
		assertEquals("first_0847898789_contact; second co086131313ntact, ", ContactUtils.getAllButLastContacts(this.tStrThreeContactsWithSemi));
		assertEquals("a#contact; two-contacts, three&contacts; ", ContactUtils.getAllButLastContacts(this.tStrFourContactsWithCharsAndSemi));
	}

	public void testGetLastContact() {
		assertEquals("", ContactUtils.getLastContact(this.tStrNoContacts));
		assertEquals("first contact", ContactUtils.getLastContact(this.tStrOneContact));
		assertEquals("last contact", ContactUtils.getLastContact(this.tStrTwoContacts));
		assertEquals("", ContactUtils.getLastContact(this.tStrThreeContactsNoLast));
		assertEquals("fo*r<con087147258tacts>", ContactUtils.getLastContact(this.tStrFourContactsWithChars));
		assertEquals(this.tStrWithAllNonSeparators, ContactUtils.getLastContact(this.tStrWithAllNonSeparators));
	}

	public void testGetLastContactWithSemiColon() {
		assertEquals("last 10 contact (084798465)", ContactUtils.getLastContact(this.tStrTwoContactsWithSemi));
		assertEquals("last contact", ContactUtils.getLastContact(this.tStrThreeContactsWithSemiAtEnd));
		assertEquals("last contact", ContactUtils.getLastContact(this.tStrThreeContactsWithSemi));
		assertEquals("fo*r<contacts>", ContactUtils.getLastContact(this.tStrFourContactsWithCharsAndSemi));
	}

	public void testGetContactsAsList() {
		assertEquals("[]", ContactUtils.getContactsAsList(this.tStrNoContacts).toString());
		assertEquals("[]", ContactUtils.getContactsAsList(this.tStrOneContact).toString());
		assertEquals("[08984276]", ContactUtils.getContactsAsList(this.tStrTwoContacts).toString());
		assertEquals("[0818881111]", ContactUtils.getContactsAsList(this.tStrThreeContactsNoLast).toString());
		assertEquals("[0831236548, 087147258]", ContactUtils.getContactsAsList(this.tStrFourContactsWithChars).toString());
		assertEquals("[058]", ContactUtils.getContactsAsList(this.tStrWithAllNonSeparators).toString());
	}

	public void testGetContactsAsListWithSemiColon() {
		assertEquals("[084798465]", ContactUtils.getContactsAsList(this.tStrTwoContactsWithSemi).toString());
		assertEquals("[0847898789, 086131313]", ContactUtils.getContactsAsList(this.tStrThreeContactsWithSemi).toString());
		assertEquals("[085114411, 0891234567]", ContactUtils.getContactsAsList(this.tStrThreeContactsWithSemiAtEnd).toString());
		assertEquals("[]", ContactUtils.getContactsAsList(this.tStrFourContactsWithCharsAndSemi).toString());
		assertEquals("[0831129933, 0812225554]", ContactUtils.getContactsAsList(this.tStrContactsWithPointlessSeparatorsAndSemi).toString());
	}

	public void testTrimSeparators() {
		assertEquals("", ContactUtils.trimSeparators(this.tStrNoContacts));
		assertEquals("first contact, second contact, another contact", ContactUtils.trimSeparators(this.tStrContactsWithPointlessSeparators));
		assertEquals("first contact", ContactUtils.trimSeparators(this.tStrOneContact));
		assertEquals("first contact (08984276), last contact", ContactUtils.trimSeparators(this.tStrTwoContacts));
		assertEquals("first (friend) contact (081 888 1111), second contact", ContactUtils.trimSeparators(this.tStrThreeContactsNoLast));
		assertEquals("a_1337_contact#(0831236548), two-contacts, three&contacts, fo*r<con087147258tacts>",
				ContactUtils.trimSeparators(this.tStrFourContactsWithChars));
		assertEquals("a!c\"m£c$a%m^c&a*e(m)1337c-e_m+c=a{e}m[c]a:e'm@c#a~e<m>c.a?e/m\\c|a`a¬o1b(058)", ContactUtils.trimSeparators(this.tStrWithAllNonSeparators));
	}

	public void testTrimSeparatorsWithSemiColon() {
		assertEquals("first contact (+35383 112 9933), second contact, another contact, +35381 222 5554",
				ContactUtils.trimSeparators(this.tStrContactsWithPointlessSeparatorsAndSemi));
		assertEquals("first contact, last 10 contact (084798465)", ContactUtils.trimSeparators(this.tStrTwoContactsWithSemi));
		assertEquals("first_0847898789_contact, second co086131313ntact, last contact", ContactUtils.trimSeparators(this.tStrThreeContactsWithSemi));
		assertEquals("first contact(+35385114411), 2nd contact(089 123 4567), last contact", ContactUtils.trimSeparators(this.tStrThreeContactsWithSemiAtEnd));
		assertEquals("a#contact, two-contacts, three&contacts, fo*r<contacts>", ContactUtils.trimSeparators(this.tStrFourContactsWithCharsAndSemi));
	}

	public void testRemoveIrishPrefixFromRecipients() {
		final List<String> recipients = new ArrayList<String>();
		recipients.add("+353871111111");
		recipients.add("00353872222222");
		recipients.add("353873333333");
		recipients.add("0874444444");
		recipients.add("0871135311");

		final List<String> result = ContactUtils.removeIrishPrefixFromRecipients(recipients);

		assertEquals("0871111111", result.get(0));
		assertEquals("0872222222", result.get(1));
		assertEquals("0873333333", result.get(2));
		assertEquals("0874444444", result.get(3));
		assertEquals("0871135311", result.get(4));
	}
}