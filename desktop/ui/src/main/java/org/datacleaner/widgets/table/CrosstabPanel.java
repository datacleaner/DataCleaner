/**
 * DataCleaner (community edition)
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
package org.datacleaner.widgets.table;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.datacleaner.result.Crosstab;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.result.DisplayChartCallback;
import org.datacleaner.widgets.result.DisplayChartCallbackImpl;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;

/**
 * A panel that contains the view for a {@link Crosstab}. This includes a
 * DCTable and a chart area that expands if charts are assigned to it.
 * 
 * These components can be accessed using {@link #getDisplayChartCallback()} and
 * {@link #getTable()}.
 * 
 * @author Kasper Sørensen
 */
public class CrosstabPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final DisplayChartCallback _displayChartCallback;
	private final DCTable _table;

	/**
	 * 
	 * @param table
	 * @param allowAnimations
	 * 
	 * @deprecated use {@link #CrosstabPanel(DCTable)} instead
	 */
	@Deprecated
	public CrosstabPanel(DCTable table, boolean allowAnimations) {
		this(table);
	}

	public CrosstabPanel(DCTable table) {
		super();
		_table = table;

		setLayout(new BorderLayout());

		final JComponent tableComponent;
		if ("".equals(table.getColumnName(1))) {
			tableComponent = table;
		} else {
			tableComponent = table.toPanel();
		}

		JXCollapsiblePane chartContainer = WidgetFactory.createCollapsiblePane(Direction.UP);
		chartContainer.setCollapsed(true);

		_displayChartCallback = new DisplayChartCallbackImpl(chartContainer);

		add(chartContainer, BorderLayout.NORTH);
		add(tableComponent, BorderLayout.CENTER);
	}

	public DisplayChartCallback getDisplayChartCallback() {
		return _displayChartCallback;
	}

	public DCTable getTable() {
		return _table;
	}
}
