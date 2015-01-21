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
package org.datacleaner.lifecycle;

import java.util.Set;

import org.datacleaner.descriptors.CloseMethodDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Life cycle callback for closing components after execution.
 */
final class CloseCallback implements LifeCycleCallback<Object, ComponentDescriptor<?>> {

    private static final Logger logger = LoggerFactory.getLogger(CloseCallback.class);
    private final boolean _includeNonDistributed;
    private final boolean _success;

    public CloseCallback(boolean includeNonDistributed, boolean success) {
        _includeNonDistributed = includeNonDistributed;
        _success = success;
    }

    @Override
    public void onEvent(Object analyzerBean, ComponentDescriptor<?> descriptor) {
        Set<CloseMethodDescriptor> closeMethods = descriptor.getCloseMethods();
        for (CloseMethodDescriptor closeDescriptor : closeMethods) {
            if (_includeNonDistributed || closeDescriptor.isDistributed()) {
                if (_success && closeDescriptor.isEnabledOnSuccess()) {
                    closeDescriptor.close(analyzerBean);
                } else if (!_success && closeDescriptor.isEnabledOnFailure()) {
                    closeDescriptor.close(analyzerBean);
                } else {
                    logger.debug("Omitting close method {} since success={}", closeDescriptor, _success);
                }
            }
        }
    }

}
