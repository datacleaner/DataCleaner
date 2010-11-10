package org.eobjects.datacleaner.output.beans;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.datastore.DatastoreOutputWriterFactory;

@AnalyzerBean("Write to Datastore")
@HiddenFromMenu
public class DatastoreOutputAnalyzer extends AbstractOutputAnalyzer {

	private OutputWriter _outputWriter;

	@Configured
	String datastoreName;

	@Override
	public OutputWriter getOutputWriter() {
		if (_outputWriter == null) {
			synchronized (this) {
				String[] headers = new String[columns.length];
				for (int i = 0; i < headers.length; i++) {
					headers[i] = columns[i].getName();
				}

				if (_outputWriter == null) {
					_outputWriter = DatastoreOutputWriterFactory.getWriter(datastoreName, columns);
				}
			}
		}
		return _outputWriter;
	}

	public void setOutputWriter(OutputWriter outputWriter) {
		_outputWriter = outputWriter;
	}

	public String getDatastoreName() {
		return datastoreName;
	}

	public void setDatastoreName(String datastoreName) {
		this.datastoreName = datastoreName;
	}
}