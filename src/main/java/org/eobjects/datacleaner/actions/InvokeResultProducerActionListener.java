package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.windows.ResultWindow;

public final class InvokeResultProducerActionListener implements ActionListener {

	private final ResultProducer _resultProducer;

	public InvokeResultProducerActionListener(ResultProducer resultProducer) {
		_resultProducer = resultProducer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AnalyzerResult result = _resultProducer.getResult();
		new ResultWindow(CollectionUtils.list(result)).setVisible(true);
	}

}
