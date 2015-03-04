package com.swift.tasks.results;

import com.swift.R;
import com.swift.tasks.Status;

public class Fail extends OperationResult {

	public static final OperationResult MESSAGE_FAILED = new Fail(R.string.message_failed_to_send);
	public static final OperationResult LOGIN_FAILED = new Fail(R.string.login_failed);
	public static final OperationResult OPERATOR_CHANGED = new Fail(R.string.operator_changed);
	public static final OperationResult NO_INTERNET_CONNECTION = new Fail(R.string.no_internet_connection);

	private Fail(final int resId) {
		super(resId, R.color.red, Status.FAILED);
	}

}