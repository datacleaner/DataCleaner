/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.ExportResultToHtmlActionListener;
import org.eobjects.datacleaner.actions.PublishResultToMonitorActionListener;
import org.eobjects.datacleaner.actions.SaveAnalysisResultActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.JobFile;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.ProgressInformationPanel;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.AnalysisRunnerSwingWorker;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.Ref;

public final class ResultWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;

    public static final List<Func<ResultWindow, JComponent>> PLUGGABLE_BANNER_COMPONENTS = new ArrayList<Func<ResultWindow, JComponent>>(
            0);

    private static final ImageManager imageManager = ImageManager.getInstance();

    private final CloseableTabbedPane _tabbedPane = new CloseableTabbedPane(true);
    private final Map<Object, ResultListPanel> _resultPanels = new HashMap<Object, ResultListPanel>();
    private final AnalysisJob _job;
    private final AnalyzerBeansConfiguration _configuration;
    private final ProgressInformationPanel _progressInformationPanel;
    private final RendererFactory _rendererFactory;
    private final FileObject _jobFilename;
    private final AnalysisRunnerSwingWorker _worker;
    private final UserPreferences _userPreferences;

    private AnalysisResult _result;

    /**
     * 
     * @param configuration
     * @param job
     *            either this or result must be available
     * @param result
     *            either this or job must be available
     * @param jobFilename
     * @param windowContext
     * @param rendererInitializerProvider
     */
    @Inject
    protected ResultWindow(AnalyzerBeansConfiguration configuration, @Nullable AnalysisJob job,
            @Nullable AnalysisResult result, @Nullable @JobFile FileObject jobFilename, WindowContext windowContext,
            UserPreferences userPreferences, RendererFactory rendererFactory) {
        super(windowContext);
        _configuration = configuration;
        _job = job;
        _jobFilename = jobFilename;
        _userPreferences = userPreferences;
        _rendererFactory = rendererFactory;
        _progressInformationPanel = new ProgressInformationPanel();
        _tabbedPane.addTab("Progress information", imageManager.getImageIcon("images/model/progress_information.png"),
                _progressInformationPanel);
        _tabbedPane.setUnclosableTab(0);

        if (result == null) {
            // run the job in a swing worker
            _result = null;
            _worker = new AnalysisRunnerSwingWorker(_configuration, _job, this, _progressInformationPanel);

            _progressInformationPanel.addStopActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _worker.cancelIfRunning();
                }
            });
        } else {
            // don't add the progress information, simply render the job asap
            _result = result;
            _worker = null;

            Map<ComponentJob, AnalyzerResult> map = result.getResultMap();
            for (Entry<ComponentJob, AnalyzerResult> entry : map.entrySet()) {
                ComponentJob componentJob = entry.getKey();
                AnalyzerResult analyzerResult = entry.getValue();

                addResult(componentJob, analyzerResult);
            }
            _progressInformationPanel.onSuccess();
        }
    }

    /**
     * Sets the result, when it is ready for eg. saving
     * 
     * @param result
     */
    public void setResult(AnalysisResult result) {
        _result = result;
    }

    public void startAnalysis() {
        _worker.execute();
    }

    private void addTableResultPanel(final Table table) {
        final String name = table.getName();
        final ResultListPanel panel = new ResultListPanel(_rendererFactory, _progressInformationPanel);
        final ImageIcon icon = imageManager.getImageIcon("images/model/table.png");
        _resultPanels.put(table, panel);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _tabbedPane.addTab(name, icon, panel);
                if (_tabbedPane.getTabCount() == 2) {
                    // switch to the first available result panel
                    _tabbedPane.setSelectedIndex(1);
                }
            }
        });
    }

    private void addDescriptorResultPanel(ComponentDescriptor<?> descriptor) {
        final ResultListPanel panel = new ResultListPanel(_rendererFactory, _progressInformationPanel);
        final String name = descriptor.getDisplayName();
        final Icon icon = IconUtils.getDescriptorIcon(descriptor);
        _resultPanels.put(descriptor, panel);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _tabbedPane.addTab(name, icon, panel);
                if (_tabbedPane.getTabCount() == 2) {
                    // switch to the first available result panel
                    _tabbedPane.setSelectedIndex(1);
                }
            }
        });
    }

    private ResultListPanel getDescriptorResultPanel(ComponentDescriptor<?> descriptor) {
        synchronized (_resultPanels) {
            if (!_resultPanels.containsKey(descriptor)) {
                addDescriptorResultPanel(descriptor);
            }
            return _resultPanels.get(descriptor);
        }
    }

    private ResultListPanel getTableResultPanel(Table table) {
        synchronized (_resultPanels) {
            if (!_resultPanels.containsKey(table)) {
                addTableResultPanel(table);
            }
            return _resultPanels.get(table);
        }
    }

    public void addResult(ComponentJob componentJob, AnalyzerResult result) {
        ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        ResultListPanel resultListPanel = getDescriptorResultPanel(descriptor);
        resultListPanel.addResult(componentJob, result);
    }

    public void addResult(Table table, ComponentJob componentJob, AnalyzerResult result) {
        ResultListPanel resultListPanel = getTableResultPanel(table);
        resultListPanel.addResult(componentJob, result);
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

        if (_jobFilename != null) {
            title = _jobFilename.getName().getBaseName() + " | " + title;
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
            if (_worker != null) {
                _worker.cancelIfRunning();
            }
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
            bannerTitle = bannerTitle + " | " + datastoreName;

            if (_jobFilename != null) {
                bannerTitle = bannerTitle + " | " + _jobFilename.getName().getBaseName();
            }
        }

        final DCBannerPanel banner = new DCBannerPanel(imageManager.getImage("images/window/banner-results.png"),
                bannerTitle);
        banner.setLayout(null);
        _tabbedPane.bindTabTitleToBanner(banner);

        final Ref<AnalysisResult> resultRef = new Ref<AnalysisResult>() {
            @Override
            public AnalysisResult get() {
                return _result;
            }
        };

        final JButton saveButton = new JButton("Save result", imageManager.getImageIcon("images/actions/save.png",
                IconUtils.ICON_SIZE_MEDIUM));
        saveButton.setOpaque(false);
        saveButton.addActionListener(new SaveAnalysisResultActionListener(resultRef, _userPreferences));

        final JButton exportButton = new JButton("Export to HTML", imageManager.getImageIcon(
                "images/actions/website.png", IconUtils.ICON_SIZE_MEDIUM));
        exportButton.setOpaque(false);
        exportButton
                .addActionListener(new ExportResultToHtmlActionListener(resultRef, _configuration, _userPreferences));

        final JButton publishButton = new JButton("Publish to monitor server", imageManager.getImageIcon(
                IconUtils.MENU_DQ_MONITOR, IconUtils.ICON_SIZE_MEDIUM));
        if (_jobFilename == null) {
            publishButton.setEnabled(false);
        } else {
            publishButton.setOpaque(false);
            publishButton.addActionListener(new PublishResultToMonitorActionListener(getWindowContext(),
                    _userPreferences, resultRef, _jobFilename));
        }

        final FlowLayout layout = new FlowLayout(Alignment.RIGHT.getFlowLayoutAlignment(), 4, 36);
        layout.setAlignOnBaseline(true);
        banner.setLayout(layout);

        for (Func<ResultWindow, JComponent> pluggableComponent : PLUGGABLE_BANNER_COMPONENTS) {
            JComponent component = pluggableComponent.eval(this);
            if (component != null) {
                banner.add(component);
            }
        }

        banner.add(publishButton);
        banner.add(exportButton);
        banner.add(saveButton);
        banner.add(Box.createHorizontalStrut(10));

        panel.add(banner, BorderLayout.NORTH);
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

    public AnalysisResult getResult() {
        return _result;
    }

    public RendererFactory getRendererFactory() {
        return _rendererFactory;
    }

    public ProgressInformationPanel getProgressInformationPanel() {
        return _progressInformationPanel;
    }

    public FileObject getJobFilename() {
        return _jobFilename;
    }

    public AnalyzerBeansConfiguration getConfiguration() {
        return _configuration;
    }

    public AnalysisJob getJob() {
        return _job;
    }

    public UserPreferences getUserPreferences() {
        return _userPreferences;
    }

    public Map<Object, ResultListPanel> getResultPanels() {
        return _resultPanels;
    }
}
