/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.output.beans;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.result.AnalyzerResult;

/**
 * Represents the output of an output analyzer, which is an analyzer that writes
 * data to a target destination.
 * 
 * @author Kasper Sørensen
 */
public interface OutputAnalyzerResult extends AnalyzerResult {

	/**
	 * @return the amount of rows that was written
	 */
	public int getWrittenRowCount();

	/**
	 * @return a datastore that can be used to access the target destination, or
	 *         null of it is not available (eg. destination not reachable or no
	 *         rows written).
	 */
	public Datastore getDatastore(DatastoreCatalog datastoreCatalog);
}
