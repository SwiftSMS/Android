package com.icc.model;

public class Contact {

	private final String number;
	private final String name;
	private final String numberType;

	public Contact(final String name, final String number, final String cNumberType) {
		this.name = name;
		this.number = number;
		this.numberType = cNumberType;
	}

	public String getNumberType() {
		return this.numberType;
	}

	public String getNumber() {
		return this.number;
	}

	public String getName() {
		return this.name;
	}

}
