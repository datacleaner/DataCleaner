package org.datacleaner.monitor.shared.model;

public class CronExpressionException extends DCUserInputException {

    private static final long serialVersionUID = 1L;

    public CronExpressionException() {
        super();
    }
 
    public CronExpressionException(final String message) {
        super(message);
    }
}
