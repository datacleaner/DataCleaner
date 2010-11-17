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
	private final AnalyzerResult _result;

	public InvokeResultProducerActionListener(String title, ResultProducer resultProducer) {
		_title = title;
		_resultProducer = resultProducer;
		_result = null;
	}

	public InvokeResultProducerActionListener(String title, AnalyzerResult result) {
		_title = title;
		_result = result;
		_resultProducer = null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AnalyzerResult result;
		if (_result == null) {
			result = _resultProducer.getResult();
		} else {
			result = _result;
		}
		new DetailsResultWindow(_title, CollectionUtils.list(result)).setVisible(true);
	}

}
