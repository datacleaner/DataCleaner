/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.util;

import java.lang.Thread.UncaughtExceptionHandler;

import org.datacleaner.api.RestrictedFunctionalityCallToAction;
import org.datacleaner.api.RestrictedFunctionalityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DCUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DCUncaughtExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, final Throwable e) {
        if (isIgnoreIssue(e)) {
            logger.debug("Ignoring uncaught exception", e);
            return;
        }

        if (e instanceof RestrictedFunctionalityException) {
            logger.debug("Handling restricted functionality exception", e);

            final StringBuilder sb = new StringBuilder(e.getMessage());
            final RestrictedFunctionalityCallToAction[] callToActions = ((RestrictedFunctionalityException) e)
                    .getCallToActions();
            for (RestrictedFunctionalityCallToAction callToAction : callToActions) {
                sb.append('\n');
                sb.append(" - " + callToAction.getName() + " - " + callToAction.getHref());
            }

            final String detailedMessage = sb.toString();

            WidgetUtils.invokeSwingAction(new Runnable() {
                @Override
                public void run() {
                    WidgetUtils.showErrorMessage("Restricted functionality", detailedMessage);
                }
            });
            return;
        }

        logger.error("Thread " + t.getName() + " threw uncaught exception", e);

        WidgetUtils.invokeSwingAction(new Runnable() {
            @Override
            public void run() {
                WidgetUtils.showErrorMessage("Unexpected error!", e);
            }
        });
    }

    /**
     * Intentionally dirty (because it's hard to make this sorta stuff pretty)
     * method used to identify known issues that we cannot do anything about
     * 
     * @param e
     * @return
     */
    private boolean isIgnoreIssue(Throwable e) {
        final StackTraceElement[] stackTrace = e.getStackTrace();
        /**
         * <pre>
         * java.lang.NullPointerException
         *   at java.awt.geom.RectangularShape.setFrameFromDiagonal(RectangularShape.java:271)
         *   at edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin.mouseDragged(PickingGraphMousePlugin.java:295)
         * </pre>
         */
        if (e instanceof NullPointerException && stackTrace != null && stackTrace.length > 1) {
            final StackTraceElement stack1 = stackTrace[0];
            if ("java.awt.geom.RectangularShape".equals(stack1.getClassName())
                    && "setFrameFromDiagonal".equals(stack1.getMethodName())) {
                final StackTraceElement stack2 = stackTrace[1];
                if ("edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin".equals(stack2.getClassName())
                        && "mouseDragged".equals(stack2.getMethodName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
