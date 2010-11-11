package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.windows.DetailsResultWindow;

public final class InvokeResultProducerActionListener implements ActionListener {

	private final String _title;
	private final ResultProducer _resultProducer;

	public InvokeResultProducerActionListener(String title, ResultProducer resultProducer) {
		_title = title;
		_resultProducer = resultProducer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AnalyzerResult result = _resultProducer.getResult();
		new DetailsResultWindow(_title, CollectionUtils.list(result)).setVisible(true);
	}

}
