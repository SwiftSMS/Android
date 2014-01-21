package com.swift.tasks.results;

import com.swift.R;
import com.swift.tasks.Status;

public class Failure extends OperationResult {

	public Failure() {
		super(R.string.message_failed_to_send, R.color.red, Status.FAILED);
	}

	public Failure(final int operatorChanged) {
		super(operatorChanged, R.color.red, Status.FAILED);
	}

}