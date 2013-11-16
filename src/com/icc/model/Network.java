package com.icc.model;

import com.icc.R;

/**
 * Simple enum to hold the current Network operators ICC will support.
 * 
 * @author Rob Powell
 * @version 1.1
 */
public enum Network {
	O2("O2", R.drawable.operator_logo_o2), METEOR("Meteor", R.drawable.operator_logo_o2), VODAFONE("Vodafone",
			R.drawable.operator_logo_o2), TESCO("Tesco", R.drawable.operator_logo_o2), EMOBILE("EMobile",
			R.drawable.operator_logo_o2), THREE("Three", R.drawable.operator_logo_o2);

	private final String network;
	private int logo;

	private Network(final String network, final int logo) {
		this.network = network;
		this.logo = logo;
	}

	public int getLogo() {
		return this.logo;
	}

	@Override
	public String toString() {
		return this.network;
	}
}