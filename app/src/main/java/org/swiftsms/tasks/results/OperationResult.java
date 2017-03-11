package org.swiftsms.tasks.results;

/**
 * Notifications for ICC Activities
 *
 * @author Rob Powell
 */
public class OperationResult {

    private final int message;
    private final Status status;

    public OperationResult(final Status status, final int message) {
        this.message = message;
        this.status = status;
    }

    public int getMessage() {
        return this.message;
    }

    public Status getStatus() {
        return this.status;
    }

}