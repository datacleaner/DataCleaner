package org.datacleaner.monitor.shared.model;

public class CronExpressionException extends Exception {

    private static final long serialVersionUID = 1L;

    public CronExpressionException() {
        super();
    }

    public CronExpressionException(String message) {
        super(message);
    }
    
    public CronExpressionException(String message, Throwable e) {
        super(message, e);
    }
}
