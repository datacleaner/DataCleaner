package org.eobjects.datacleaner.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DCUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(DCUncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logger.error("Thread " + t.getName() + " threw uncaught exception", e);
		WidgetUtils.showErrorMessage("Unexpected error", "An unexpected error occurred: " + e.getMessage(), e);
	}

}
