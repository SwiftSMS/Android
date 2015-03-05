package com.swift.model;

import android.text.InputType;

import com.swift.R;

/**
 * Simple enum to hold the current Network operators ICC will support.
 * 
 * @author Rob Powell
 * @version 1.1
 */
public enum Network {

	EMOBILE("eMobile", R.drawable.operator_logo_emobile, InputType.TYPE_CLASS_PHONE),
	METEOR("Meteor", R.drawable.operator_logo_meteor, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
	O2("O2 on Three", R.drawable.operator_logo_o3, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
	TESCO("Tesco", R.drawable.operator_logo_tesco, InputType.TYPE_CLASS_PHONE),
	THREE("Three", R.drawable.operator_logo_three, InputType.TYPE_CLASS_PHONE),
	VODAFONE("Vodafone", R.drawable.operator_logo_vodafone, InputType.TYPE_CLASS_PHONE);

	private final String network;
	private int logo;
	private int inputType;

	private Network(final String network, final int logo, final int inputType) {
		this.network = network;
		this.logo = logo;
		this.inputType = inputType;
	}

	/**
	 * This method returns the default logo for the network Operator.
	 * 
	 * @return The drawable id of the Operators logo.
	 */
	public int getLogo() {
		return this.logo;
	}

	/**
	 * This method returns the {@link InputType} that most closely matches the login username for the Operator.
	 * 
	 * @return The InputType to be used for the Operators username.
	 */
	public int getInputType() {
		return this.inputType;
	}

	@Override
	public String toString() {
		return this.network;
	}
}
