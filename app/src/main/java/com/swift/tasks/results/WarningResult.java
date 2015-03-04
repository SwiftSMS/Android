package com.swift.tasks.results;

import com.swift.R;
import com.swift.tasks.Status;

public class WarningResult extends OperationResult {

	public WarningResult(final int stringResource) {
		super(stringResource, R.color.holo_light_blue, Status.CANCELLED);
	}

}