package dk.eobjects.datacleaner.execution;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.metamodel.schema.Table;

public class ProgressObserverDelegate implements IProgressObserver {

	protected Log _log = LogFactory.getLog(getClass());
	private List<IProgressObserver> _progressObservers = new LinkedList<IProgressObserver>();

	public void init(Table[] tables) {
		_log.debug("initObservers()");
		for (IProgressObserver observer : _progressObservers) {
			observer.init(tables);
		}
	}

	public void notifySuccess(Table table, long numRowsProcessed) {
		_log.debug("notifyExecutionSuccess()");
		for (IProgressObserver observer : _progressObservers) {
			observer.notifySuccess(table, numRowsProcessed);
		}
	}

	public void notifyFailure(Table table, Throwable t, Long lastRow) {
		_log.debug("notifyExecutionFailed()");
		for (IProgressObserver observer : _progressObservers) {
			observer.notifyFailure(table, t, lastRow);
		}
	}

	public void notifyBeginning(Table table, long numRows) {
		_log.debug("notifyExecutionBegin()");
		for (IProgressObserver observer : _progressObservers) {
			observer.notifyBeginning(table, numRows);
		}
	}

	public void notifyProgress(Table table, long numRowsProcessed) {
		_log.debug("notifyProgress()");
		for (IProgressObserver observer : _progressObservers) {
			observer.notifyProgress(table, numRowsProcessed);
		}
	}

	public void addProgressObserver(IProgressObserver observer) {
		_progressObservers.add(observer);
	}

	public void removeProgressObserver(IProgressObserver observer) {
		_progressObservers.remove(observer);
	}
}
