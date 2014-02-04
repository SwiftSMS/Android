package com.swift.tasks.results;

import com.swift.R;
import com.swift.tasks.Status;

public class Failure extends OperationResult {

	public Failure() {
		this(R.string.message_failed_to_send);
	}

	public Failure(final int resId) {
		super(resId, R.color.red, Status.FAILED);
	}

}