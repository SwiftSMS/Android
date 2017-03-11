package org.swiftsms.io.net;

import org.swiftsms.models.Account;

public class OperatorFactory {

	public static Operator getOperator(final Account account) {
		switch (account.getOperator()) {
		case METEOR:
			if (account.getMobileNumber().contains("@")) {
				return new NewMeteor(account);
			}
			return new Meteor(account);
		case O2:
			return new O2(account);
		case THREE:
			return new Three(account);
		case VODAFONE:
			return new Vodafone(account);
		default:
			return null;
		}
	}
}