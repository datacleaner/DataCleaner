/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.writers;

import org.apache.metamodel.util.Action;

/**
 * Provides a buffering mechanism that enables writing rows periodically instead
 * of instantly.
 * 
 * @deprecated use {@link org.eobjects.analyzer.util.WriteBuffer} instead.
 */
@Deprecated
public final class WriteBuffer extends org.eobjects.analyzer.util.WriteBuffer {

    public WriteBuffer(int bufferSize, Action<Iterable<Object[]>> flushAction) {
        super(bufferSize, flushAction);
    }
}
