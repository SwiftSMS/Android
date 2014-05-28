package com.swift.tasks.results;

import com.swift.R;
import com.swift.tasks.Status;

public class Success extends OperationResult {

	public static final OperationResult MESSAGE_SENT = new Success(R.string.message_sent);
	public static final OperationResult LOGGED_IN = new Success(R.string.logged_in);

	private Success(final int messageId) {
		super(messageId, R.color.green, Status.SUCCESS);
	}

}