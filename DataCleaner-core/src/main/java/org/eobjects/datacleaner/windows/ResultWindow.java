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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.ProgressInformationPanel;
import org.eobjects.datacleaner.util.AnalysisRunnerSwingWorker;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.result.DCRendererInitializer;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;

import org.eobjects.metamodel.schema.Table;

public final class ResultWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final CloseableTabbedPane _tabbedPane = new CloseableTabbedPane();
	private final Map<Table, ResultListPanel> _resultPanels = new HashMap<Table, ResultListPanel>();
	private final AnalysisJob _job;
	private final AnalyzerBeansConfiguration _configuration;
	private final ProgressInformationPanel _progressInformationPanel;
	private final RendererFactory _rendererFactory;
	private final String _jobFilename;
	private final AnalysisRunnerSwingWorker _worker;

	public ResultWindow(AnalyzerBeansConfiguration configuration, AnalysisJob job, String jobFilename, WindowContext windowContext) {
		super(windowContext);
		_configuration = configuration;
		_job = job;
		_jobFilename = jobFilename;
		_rendererFactory = new RendererFactory(configuration.getDescriptorProvider(), new DCRendererInitializer(getwindowContext()));

		_progressInformationPanel = new ProgressInformationPanel();
		_tabbedPane.addTab("Progress information", imageManager.getImageIcon("images/model/progress_information.png"),
				_progressInformationPanel);
		_tabbedPane.setUnclosableTab(0);

		_worker = new AnalysisRunnerSwingWorker(_configuration, _job, this, _progressInformationPanel);

		_progressInformationPanel.addStopActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_worker.cancelIfRunning();
			}
		});
	}

	public void startAnalysis() {
		_worker.execute();
	}

	private void addTableResultPanel(final Table table) {
		final String tableName = table.getName();
		final ResultListPanel panel = new ResultListPanel(_rendererFactory, _progressInformationPanel);
		final ImageIcon tableIcon = imageManager.getImageIcon("images/model/table.png");
		_resultPanels.put(table, panel);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_tabbedPane.addTab(tableName, tableIcon, panel);
				if (_tabbedPane.getTabCount() == 2) {
					// switch to the first available result panel
					_tabbedPane.setSelectedIndex(1);
				}
			}
		});
	}

	private ResultListPanel getTableResultPanel(Table table) {
		synchronized (_resultPanels) {
			if (!_resultPanels.containsKey(table)) {
				addTableResultPanel(table);
			}
			return _resultPanels.get(table);
		}
	}

	public void addResult(Table table, AnalyzerJob analyzerJob, AnalyzerResult result) {
		ResultListPanel resultListPanel = getTableResultPanel(table);
		resultListPanel.addResult(analyzerJob, result);
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	public String getWindowTitle() {
		String title = "Analysis results";

		String datastoreName = getDatastoreName();
		if (!StringUtils.isNullOrEmpty(datastoreName)) {
			title = datastoreName + " | " + title;
		}

		if (!StringUtils.isNullOrEmpty(_jobFilename)) {
			title = _jobFilename + " | " + title;
		}
		return title;
	}

	private String getDatastoreName() {
		if (_job != null) {
			Datastore datastore = _job.getDatastore();
			if (datastore != null) {
				String datastoreName = datastore.getName();
				if (!StringUtils.isNullOrEmpty(datastoreName)) {
					return datastoreName;
				}
			}
		}
		return null;
	}

	@Override
	public Image getWindowIcon() {
		return imageManager.getImage("images/model/result.png");
	}

	@Override
	protected boolean onWindowClosing() {
		boolean closing = super.onWindowClosing();
		if (closing) {
			_worker.cancelIfRunning();
		}
		return closing;
	}

	@Override
	protected JComponent getWindowContent() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());

		String bannerTitle = "Analysis results";
		String datastoreName = getDatastoreName();
		if (!StringUtils.isNullOrEmpty(datastoreName)) {
			bannerTitle = bannerTitle + "\n" + datastoreName;

			if (!StringUtils.isNullOrEmpty(_jobFilename)) {
				bannerTitle = bannerTitle + " | " + _jobFilename;
			}
		}

		panel.add(new DCBannerPanel(imageManager.getImage("images/window/banner-results.png"), bannerTitle),
				BorderLayout.NORTH);
		panel.add(_tabbedPane, BorderLayout.CENTER);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;

		int height = 550;
		if (screenHeight > 1000) {
			height = 900;
		} else if (screenHeight > 750) {
			height = 700;
		}

		int width = 750;
		if (screenWidth > 1200) {
			width = 1100;
		} else if (screenWidth > 1000) {
			width = 900;
		}

		panel.setPreferredSize(width, height);
		return panel;
	}
}
