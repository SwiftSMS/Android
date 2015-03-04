package com.swift.tasks.results;

import com.swift.tasks.Status;

/**
 * Notifications for ICC Activities
 * 
 * @author Rob Powell
 */
public abstract class OperationResult {

	private final int stringResource;
	private final Status status;
	private final int colourResource;

	public OperationResult(final int stringResource, final int colourResource, final Status status) {
		this.stringResource = stringResource;
		this.colourResource = colourResource;
		this.status = status;
	}

	public int getStringResource() {
		return this.stringResource;
	}

	public Status getStatus() {
		return this.status;
	}

	public int getColourResource() {
		return this.colourResource;
	}
}