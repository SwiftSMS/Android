package org.swiftsms.tasks.results;

import org.swiftsms.R;

public class Success extends OperationResult {

	public static final OperationResult MESSAGE_SENT = new Success(R.string.message_sent);
	public static final OperationResult LOGGED_IN = new Success(R.string.logged_in);

	private Success(final int messageId) {
		super(Status.SUCCESS, messageId);
	}

}