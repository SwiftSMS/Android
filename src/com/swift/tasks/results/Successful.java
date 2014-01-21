package com.swift.tasks.results;

import com.swift.R;
import com.swift.tasks.Status;

public class Successful extends OperationResult {

	public Successful() {
		super(R.string.message_sent, R.color.green, Status.SUCCESS);
	}

	public Successful(final int messageId) {
		super(messageId, R.color.green, Status.SUCCESS);
	}

}