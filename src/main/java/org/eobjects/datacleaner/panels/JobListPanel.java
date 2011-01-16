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

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.actions.NewAnalysisJobActionListener;
import org.eobjects.datacleaner.actions.OpenAnalysisJobActionListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;

public class JobListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;
	private final AnalyzerBeansConfiguration _configuration;

	public JobListPanel(AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_configuration = configuration;

		setLayout(new BorderLayout());

		JToolBar toolBar = WidgetFactory.createToolBar();
		final JButton addJobButton = new JButton("New", ImageManager.getInstance().getImageIcon("images/actions/new.png"));
		addJobButton.setToolTipText("New analysis job");
		addJobButton.addActionListener(new NewAnalysisJobActionListener(_configuration));

		JButton openJobButton = new JButton("Open", ImageManager.getInstance().getImageIcon("images/actions/open.png"));
		openJobButton.setToolTipText("Open analysis job");
		openJobButton.addActionListener(new OpenAnalysisJobActionListener(_configuration));

		toolBar.add(addJobButton);
		toolBar.add(openJobButton);

		add(toolBar, BorderLayout.NORTH);
	}
}