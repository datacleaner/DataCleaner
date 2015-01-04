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
package org.eobjects.analyzer.job.builder;

import java.util.ArrayList;
import java.util.Collection;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.InputColumnSourceJob;

public final class SourceColumns implements InputColumnSourceJob {

	private final Collection<InputColumn<?>> _sourceColumns = new ArrayList<InputColumn<?>>();

	public SourceColumns(Collection<? extends InputColumn<?>> sourceColumns) {
		_sourceColumns.addAll(sourceColumns);
	}

	@Override
	public InputColumn<?>[] getOutput() {
		return _sourceColumns.toArray(new InputColumn<?>[_sourceColumns.size()]);
	}

}
