package org.swiftsms.io.net;

public class OperatorChangedException extends RuntimeException {

	public OperatorChangedException() {
		super("Operator website changed");
	}

}