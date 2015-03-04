package com.swift.io.net;

public class NoInternetAccessException extends RuntimeException {

	private static final long serialVersionUID = 2013412976393613645L;

	public NoInternetAccessException() {
		super("No network connection");
	}

}