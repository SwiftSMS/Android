package com.swift.io.net;

import com.swift.model.Account;

public class OperatorFactory {

	public static Operator getOperator(final Account account) {
		switch (account.getOperator()) {
		case EMOBILE:
			return new EMobile(account);
		case METEOR:
			return new Meteor(account);
		// case O2:
			// return new O2(account);
		case TESCO:
			return new Tesco(account);
		case THREE:
			return new Three(account);
		case VODAFONE:
			return new Vodafone(account);
		default:
			return null;
		}
	}
}