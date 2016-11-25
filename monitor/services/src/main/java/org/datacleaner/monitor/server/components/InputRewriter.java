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
package org.datacleaner.monitor.server.components;

import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.restclient.ProcessStatelessInput;

/**
 * Implementors of this interface are used to rewrite input of the REST API.
 * Can be used to support more input styles and rewriters transform the input
 * to one canonical representation.
 */
public interface InputRewriter {

    /**
     * (Possibly) rewrites the input of
     * {@link org.datacleaner.monitor.server.controllers.ComponentControllerV1#processStateless(String,
     * String, String, boolean, ProcessStatelessInput)}
     * method.
     *
     * @return true if the input rewriting should stop here (NO other rewriters will be processed).
     */
    boolean rewriteInput(TransformerDescriptor<?> transformer, ProcessStatelessInput input);
}
