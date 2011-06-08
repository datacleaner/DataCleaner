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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.table.DCTable;

@RendererBean(SwingRenderingFormat.class)
public class PatternFinderResultSwingRenderer extends AbstractCrosstabResultSwingRenderer<PatternFinderResult> {

	private final MutableReferenceDataCatalog _catalog = (MutableReferenceDataCatalog) DCConfiguration.get()
			.getReferenceDataCatalog();

	@Override
	protected void decorate(PatternFinderResult result, DCTable table, DisplayChartCallback displayChartCallback) {
		super.decorate(result, table, displayChartCallback);
		
		table.setAlignment(1, Alignment.RIGHT);

		int rowCount = table.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			final String expression;
			final Object value = table.getValueAt(i, 0);
			if (value == null) {
				expression = null;
			} else if (value instanceof String) {
				expression = (String) value;
			} else if (value instanceof JLabel) {
				expression = ((JLabel) value).getText();
			} else {
				expression = value.toString();
			}

			final String synonymCatalogName = "PF: " + expression;

			if (!_catalog.containsSynonymCatalog(synonymCatalogName)) {
				DCPanel panel = new DCPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

				panel.add(Box.createHorizontalStrut(4));
				panel.add(new JLabel(expression));

				final JButton button = WidgetFactory.createSmallButton("images/actions/save.png");
				button.setToolTipText("Save as string pattern");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						_catalog.addStringPattern(new SimpleStringPattern(synonymCatalogName, expression));
						button.setEnabled(false);
					}
				});
				panel.add(Box.createHorizontalStrut(4));
				panel.add(button);

				table.setValueAt(panel, i, 0);
			}
		}
	}
}
