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

import javax.swing.JComponent;

import org.jdesktop.swingx.JXCollapsiblePane;

/**
 * Default {@link DisplayChartCallback} implementation.
 * 
 * @author Kasper Sørensen
 */
public class DisplayChartCallbackImpl implements DisplayChartCallback {

	private final JXCollapsiblePane _chartContainer;
	private final boolean _allowAnimations;

	public DisplayChartCallbackImpl(JXCollapsiblePane chartContainer, boolean allowAnimations) {
		_chartContainer = chartContainer;
		_allowAnimations = allowAnimations;
	}

	@Override
	public void displayChart(JComponent chart) {
		boolean showing = _chartContainer.isShowing();
		if (_allowAnimations && !showing) {
			_chartContainer.setAnimated(false);
		}

		_chartContainer.getContentPane().removeAll();
		_chartContainer.getContentPane().add(chart);
		_chartContainer.updateUI();
		_chartContainer.setCollapsed(false);

		if (_allowAnimations && !showing) {
			_chartContainer.setAnimated(true);
		}
	}

}
