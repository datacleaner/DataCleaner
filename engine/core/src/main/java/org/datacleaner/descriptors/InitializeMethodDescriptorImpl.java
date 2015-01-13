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
package org.datacleaner.descriptors;

import java.lang.reflect.Method;

import org.datacleaner.api.Close;

final class InitializeMethodDescriptorImpl extends AbstractMethodDescriptor implements InitializeMethodDescriptor {

    private static final long serialVersionUID = 1L;

    protected InitializeMethodDescriptorImpl(Method method, ComponentDescriptor<?> componentDescriptor) {
        super(method, componentDescriptor);
    }

    @Override
    public boolean isDistributed() {
        Close annotation = getAnnotation(Close.class);
        if (annotation == null) {
            return true;
        }
        return annotation.distributed();
    }

    public void initialize(Object component) throws IllegalStateException {
        invoke(component);
    }
}
