package com.icc.model;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Contact {

	private final String number;
	private final String name;
	private final String numberType;
	private final Bitmap photo;

	public Contact(final String name, final InputStream photo, final String number, final String numberType) {
		this.name = name;
		this.photo = BitmapFactory.decodeStream(photo);
		this.number = number;
		this.numberType = numberType;
	}

	public Bitmap getPhoto() {
		return this.photo;
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

	@Override
	public String toString() {
		return this.name + "(" + this.number + ")";
	}
}