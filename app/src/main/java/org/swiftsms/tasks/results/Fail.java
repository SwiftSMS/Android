package org.swiftsms.tasks.results;

import org.swiftsms.R;

public class Fail extends OperationResult {

	public static final OperationResult MESSAGE_FAILED = new Fail(R.string.message_failed_to_send);
	public static final OperationResult LOGIN_FAILED = new Fail(R.string.error_login_failed);
	public static final OperationResult OPERATOR_CHANGED = new Fail(R.string.operator_changed);
	public static final OperationResult NO_INTERNET_CONNECTION = new Fail(R.string.no_internet_connection);

	private Fail(final int resId) {
		super(Status.FAILED, resId);
	}

}