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
package org.datacleaner.job.concurrent;

import org.datacleaner.job.tasks.Task;

/**
 * {@link TaskListener} that wraps several other {@link TaskListener}s
 */
public class CompositeTaskListener implements TaskListener {

    private final TaskListener[] _listeners;

    public CompositeTaskListener(final TaskListener... listeners) {
        _listeners = listeners;
    }

    @Override
    public void onBegin(final Task task) {
        for (final TaskListener listener : _listeners) {
            listener.onBegin(task);
        }
    }

    @Override
    public void onComplete(final Task task) {
        for (final TaskListener listener : _listeners) {
            listener.onComplete(task);
        }
    }

    @Override
    public void onError(final Task task, final Throwable throwable) {
        for (final TaskListener listener : _listeners) {
            listener.onError(task, throwable);
        }
    }

}
