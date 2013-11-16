package com.icc.model;

import android.text.InputType;

import com.icc.R;

/**
 * Simple enum to hold the current Network operators ICC will support.
 * 
 * @author Rob Powell
 * @version 1.1
 */
public enum Network {
	
	O2("O2", R.drawable.operator_logo_o2, InputType.TYPE_CLASS_TEXT), 
	METEOR("Meteor", R.drawable.operator_logo_meteor, InputType.TYPE_CLASS_PHONE), 
	VODAFONE("Vodafone", R.drawable.operator_logo_vodafone, InputType.TYPE_CLASS_PHONE), 
	TESCO("Tesco", R.drawable.operator_logo_tesco, InputType.TYPE_CLASS_PHONE), 
	EMOBILE("EMobile", R.drawable.operator_logo_emobile, InputType.TYPE_CLASS_PHONE), 
	THREE("Three", R.drawable.operator_logo_three, InputType.TYPE_CLASS_PHONE);

	private final String network;
	private int logo;
	private int inputType;

	private Network(final String network, final int logo, final int inputType) {
		this.network = network;
		this.logo = logo;
		this.inputType = inputType;
	}

	public int getLogo() {
		return this.logo;
	}
	
	public int getInputType() {
		return inputType;
	}

	@Override
	public String toString() {
		return this.network;
	}
}