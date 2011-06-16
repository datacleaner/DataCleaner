package org.eobjects.datacleaner.user;

import java.io.File;

import org.eobjects.analyzer.connection.FileDatastore;
import org.eobjects.analyzer.connection.PerformanceCharacteristics;
import org.eobjects.analyzer.connection.SingleDataContextProvider;
import org.eobjects.analyzer.connection.UsageAwareDataContextProvider;
import org.eobjects.analyzer.connection.UsageAwareDatastore;
import org.eobjects.metamodel.DataContext;
import org.eobjects.sassy.metamodel.SasDataContext;

public class SasDatastore extends UsageAwareDatastore implements FileDatastore, PerformanceCharacteristics {

	private static final long serialVersionUID = 1L;
	private final File _directory;

	public SasDatastore(String name, File directory) {
		super(name);
		_directory = directory;
	}

	@Override
	public PerformanceCharacteristics getPerformanceCharacteristics() {
		return this;
	}
	
	@Override
	protected UsageAwareDataContextProvider createDataContextProvider() {
		DataContext dataContext = new SasDataContext(_directory);
		return new SingleDataContextProvider(dataContext, this);
	}

	@Override
	public boolean isQueryOptimizationPreferred() {
		return false;
	}

	@Override
	public String getFilename() {
		return _directory.getAbsolutePath();
	}

}
