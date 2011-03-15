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
package org.eobjects.datacleaner.widgets.result;

import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.windows.DetailsResultWindow;

public class DrillToDetailsCallbackImpl implements DrillToDetailsCallback {

	@Override
	public void drillToDetails(String title, ResultProducer resultProducer) {
		final AnalyzerResult result = resultProducer.getResult();
		final DetailsResultWindow window = new DetailsResultWindow(title, CollectionUtils.list(result));
		window.setVisible(true);
	}
}
