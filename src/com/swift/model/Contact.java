package com.swift.model;

import android.net.Uri;
import android.telephony.PhoneNumberUtils;

import com.swift.R;

public class Contact {

	private final String number;
	private final String name;
	private final String numberType;
	private final Uri photo;

	public Contact(final String name, final String photo, final String number, final String numberType) {
		this.name = name;
		this.photo = Uri.parse(this.getImageUri(photo));
		this.number = number;
		this.numberType = numberType;
	}

	public Uri getPhoto() {
		return this.photo;
	}

	public String getNumberType() {
		return this.numberType;
	}

	public String getNumber() {
		return this.number;
	}

	/**
	 * Get the contacts phone number with all formatting removed. The returned number is in a format that is acceptable by all
	 * network operators for sending an SMS.
	 * 
	 * @return A non formatted phone number.
	 */
	public String getStrippedNumber() {
		return PhoneNumberUtils.stripSeparators(this.number);
	}

	public String getName() {
		return this.name;
	}

	private String getImageUri(final String imageUri) {
		if (imageUri == null) {
			final int num = (int) (Math.random() * 2);
			if (num == 0) {
				return "android.resource://com.swift/" + R.drawable.ic_contact_picture_2;
			} else if (num == 1) {
				return "android.resource://com.swift/" + R.drawable.ic_contact_picture_3;
			}
		}
		return imageUri;
	}

	@Override
	public String toString() {
		if (this.name == null) {
			return this.getStrippedNumber();
		}
		return this.name + " (" + this.getStrippedNumber() + ")";
	}
}