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
package org.eobjects.datacleaner.bootstrap;

import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.metamodel.DataContext;

public class DefaultBootstrapOptions implements BootstrapOptions {

	private final String[] _args;
	private final CliArguments _arguments;

	public DefaultBootstrapOptions(String[] args) {
		_args = args;
		_arguments = CliArguments.parse(_args);
	}

	@Override
	public boolean isCommandLineMode() {
		return _arguments.isSet();
	}

	@Override
	public CliArguments getCommandLineArguments() {
		return _arguments;
	}

	@Override
	public ExitActionListener getExitActionListener() {
		return new DCExitActionListener();
	}
	
	@Override
	public boolean isSingleDatastoreMode() {
		return _arguments.getDatastoreName() != null;
	}

	@Override
	public Datastore getSingleDatastore(DatastoreCatalog datastoreCatalog) {
		String ds = _arguments.getDatastoreName();
		return datastoreCatalog.getDatastore(ds);
	}

	@Override
	public void initializeSingleDatastoreJob(AnalysisJobBuilder analysisJobBuilder, DataContext dataContext) {
		// do nothing
	}
}
