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
package org.eobjects.datacleaner.panels;

import java.util.List;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.SourceColumnChangeListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.metamodel.schema.Column;
import org.jdesktop.swingx.VerticalLayout;

public class MetadataPanel extends DCPanel implements SourceColumnChangeListener {

	private static final long serialVersionUID = 1L;
	private final AnalysisJobBuilder _analysisJobBuilder;

	public MetadataPanel(AnalysisJobBuilder analysisJobBuilder) {
		super(ImageManager.getInstance().getImage("images/window/metadata-tab-background.png"), 95, 95,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new VerticalLayout(4));
		_analysisJobBuilder = analysisJobBuilder;
		_analysisJobBuilder.getSourceColumnListeners().add(this);
		updateComponents();
	}

	private void updateComponents() {
		removeAll();
		List<MetaModelInputColumn> sourceColumns = _analysisJobBuilder.getSourceColumns();
		for (MetaModelInputColumn inputColumn : sourceColumns) {
			Column column = inputColumn.getPhysicalColumn();
			add(DCLabel.dark(column.getQualifiedLabel() + ": " + column.getNativeType()));
		}
	}

	@Override
	public void onAdd(InputColumn<?> sourceColumn) {
		updateComponents();
	}

	@Override
	public void onRemove(InputColumn<?> sourceColumn) {
		updateComponents();
	}

}
