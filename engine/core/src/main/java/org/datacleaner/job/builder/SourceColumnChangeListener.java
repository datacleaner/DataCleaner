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
package org.datacleaner.job.builder;

import org.datacleaner.data.InputColumn;

/**
 * Listener interface for receiving notifications when source columns are being
 * added or removed from the job.
 * 
 * Note that other columns than source columns may be of interest since
 * transformers generate virtual columns as well. Use a
 * TransformerChangeListener to receive notifications about such columns.
 * 
 * @see TransformerChangeListener
 * 
 * 
 */
public interface SourceColumnChangeListener {

	public void onAdd(InputColumn<?> sourceColumn);

	public void onRemove(InputColumn<?> sourceColumn);
}
