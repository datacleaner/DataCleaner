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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.PreviousErrorsExistException;
import org.eobjects.analyzer.job.runner.AnalysisJobCancellation;
import org.eobjects.analyzer.job.runner.AnalysisJobMetrics;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisListenerAdaptor;
import org.eobjects.analyzer.job.runner.AnalyzerMetrics;
import org.eobjects.analyzer.job.runner.RowProcessingMetrics;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.analyzer.util.SourceColumnFinder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.ExportResultToHtmlActionListener;
import org.eobjects.datacleaner.actions.PublishResultToMonitorActionListener;
import org.eobjects.datacleaner.actions.SaveAnalysisResultActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.JobFile;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.result.ProgressInformationPanel;
import org.eobjects.datacleaner.panels.result.ResultListPanel;
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

/**
 * Window in which the result (and running progress information) of job
 * execution is shown.
 */
public final class ResultWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;

    public static final List<Func<ResultWindow, JComponent>> PLUGGABLE_BANNER_COMPONENTS = new ArrayList<Func<ResultWindow, JComponent>>(
            0);

    private static final ImageManager imageManager = ImageManager.get();

    private final CloseableTabbedPane _tabbedPane = new CloseableTabbedPane(true);
    private final ConcurrentMap<Object, ResultListPanel> _resultPanels = new ConcurrentHashMap<Object, ResultListPanel>();
    private final AnalysisJob _job;
    private final AnalyzerBeansConfiguration _configuration;
    private final ProgressInformationPanel _progressInformationPanel;
    private final RendererFactory _rendererFactory;
    private final FileObject _jobFilename;
    private final AnalysisRunnerSwingWorker _worker;
    private final UserPreferences _userPreferences;
    private final JButton _cancelButton;
    private final JButton _saveButton;
    private final JButton _exportButton;
    private final JButton _publishButton;
    private final List<JComponent> _pluggableButtons;

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
        final boolean running = (result == null);
        _configuration = configuration;
        _job = job;
        _jobFilename = jobFilename;
        _userPreferences = userPreferences;
        _rendererFactory = rendererFactory;
        _progressInformationPanel = new ProgressInformationPanel(running);
        _tabbedPane.addTab("Progress information", imageManager.getImageIcon("images/model/progress_information.png"),
                _progressInformationPanel);
        _tabbedPane.setUnclosableTab(0);

        _pluggableButtons = new ArrayList<JComponent>(1);

        _cancelButton = new JButton("Cancel job", imageManager.getImageIcon("images/actions/stop.png",
                IconUtils.ICON_SIZE_MEDIUM));
        _cancelButton.setOpaque(false);

        final Ref<AnalysisResult> resultRef = new Ref<AnalysisResult>() {
            @Override
            public AnalysisResult get() {
                return getResult();
            }
        };

        _publishButton = new JButton("Publish to server", imageManager.getImageIcon(IconUtils.MENU_DQ_MONITOR,
                IconUtils.ICON_SIZE_MEDIUM));
        _publishButton.setOpaque(false);
        _publishButton.addActionListener(new PublishResultToMonitorActionListener(getWindowContext(), _userPreferences,
                resultRef, _jobFilename));

        _saveButton = new JButton("Save result", imageManager.getImageIcon("images/actions/save.png",
                IconUtils.ICON_SIZE_MEDIUM));
        _saveButton.setOpaque(false);
        _saveButton.addActionListener(new SaveAnalysisResultActionListener(resultRef, _userPreferences));

        _exportButton = new JButton("Export to HTML", imageManager.getImageIcon("images/actions/website.png",
                IconUtils.ICON_SIZE_MEDIUM));
        _exportButton.setOpaque(false);
        _exportButton.addActionListener(new ExportResultToHtmlActionListener(resultRef, _configuration,
                _userPreferences));
        
        for (Func<ResultWindow, JComponent> pluggableComponent : PLUGGABLE_BANNER_COMPONENTS) {
            JComponent component = pluggableComponent.eval(this);
            if (component != null) {
                _pluggableButtons.add(component);
            }
        }

        if (running) {
            // run the job in a swing worker
            _result = null;
            _worker = new AnalysisRunnerSwingWorker(_configuration, _job, this);

            _cancelButton.addActionListener(new ActionListener() {
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

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (_tabbedPane.getTabCount() > 1) {
                        // switch to the first available result panel
                        _tabbedPane.setSelectedIndex(1);
                    }
                }
            });
        }
        
        updateButtonVisibility(running);
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

    private ResultListPanel getDescriptorResultPanel(final ComponentDescriptor<?> descriptor) {
        ResultListPanel resultPanel = _resultPanels.get(descriptor);
        if (resultPanel == null) {
            resultPanel = new ResultListPanel(_rendererFactory, _progressInformationPanel);
            ResultListPanel previous = _resultPanels.putIfAbsent(descriptor, resultPanel);
            if (previous != null) {
                resultPanel = previous;
            } else {
                final ResultListPanel finalResultListPanel = resultPanel;
                final String name = descriptor.getDisplayName();
                final Icon icon = IconUtils.getDescriptorIcon(descriptor);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        _tabbedPane.addTab(name, icon, finalResultListPanel);
                    }
                });
            }
        }
        return resultPanel;
    }

    public void addResult(ComponentJob componentJob, AnalyzerResult result) {
        ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        ResultListPanel resultListPanel = getDescriptorResultPanel(descriptor);
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

        final FlowLayout layout = new FlowLayout(Alignment.RIGHT.getFlowLayoutAlignment(), 4, 36);
        layout.setAlignOnBaseline(true);
        banner.setLayout(layout);

        for (JComponent pluggableButton : _pluggableButtons) {
            banner.add(pluggableButton);
        }

        banner.add(_publishButton);
        banner.add(_exportButton);
        banner.add(_saveButton);
        banner.add(_cancelButton);
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
        if (_result == null && _worker != null) {
            try {
                _result = _worker.get();
            } catch (Exception e) {
                WidgetUtils.showErrorMessage("Unable to fetch result", e);
            }
        }
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

    public void onUnexpectedError(AnalysisJob job, Throwable throwable) {
        if (throwable instanceof AnalysisJobCancellation) {
            _progressInformationPanel.onCancelled();
            _cancelButton.setEnabled(false);
            return;
        } else if (throwable instanceof PreviousErrorsExistException) {
            // do nothing
            return;
        }
        _progressInformationPanel.addUserLog("An error occurred in the analysis job!", throwable, true);
    }

    public AnalysisListener createAnalysisListener() {
        return new AnalysisListenerAdaptor() {
            @Override
            public void jobBegin(AnalysisJob job, AnalysisJobMetrics metrics) {
                updateButtonVisibility(true);
                _progressInformationPanel.onBegin();
            }

            @Override
            public void jobSuccess(AnalysisJob job, AnalysisJobMetrics metrics) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateButtonVisibility(false);
                        _progressInformationPanel.onSuccess();
                        if (_tabbedPane.getTabCount() > 1) {
                            // switch to the first available result panel
                            _tabbedPane.setSelectedIndex(1);
                        }
                    }
                });
            }

            @Override
            public void rowProcessingBegin(final AnalysisJob job, final RowProcessingMetrics metrics) {
                final int expectedRows = metrics.getExpectedRows();
                final Table table = metrics.getTable();
                if (expectedRows == -1) {
                    _progressInformationPanel.addUserLog("Starting processing of " + table.getName());
                } else {
                    _progressInformationPanel.addUserLog("Starting processing of " + table.getName() + " (approx. "
                            + expectedRows + " rows)");
                    _progressInformationPanel.setExpectedRows(table, expectedRows);
                }
            }

            @Override
            public void rowProcessingProgress(AnalysisJob job, final RowProcessingMetrics metrics, final InputRow row, final int currentRow) {
                _progressInformationPanel.updateProgress(metrics.getTable(), currentRow);
            }

            @Override
            public void rowProcessingSuccess(AnalysisJob job, final RowProcessingMetrics metrics) {
                _progressInformationPanel.updateProgressFinished(metrics.getTable());
                _progressInformationPanel.addUserLog("Processing of " + metrics.getTable().getName()
                        + " finished. Generating results ...");
            }

            @Override
            public void analyzerBegin(AnalysisJob job, final AnalyzerJob analyzerJob, AnalyzerMetrics metrics) {
                _progressInformationPanel.addUserLog("Starting analyzer '" + LabelUtils.getLabel(analyzerJob) + "'");
            }

            @Override
            public void analyzerSuccess(AnalysisJob job, final AnalyzerJob analyzerJob, final AnalyzerResult result) {
                final List<InputColumn<?>> inputColumns = Arrays.asList(analyzerJob.getInput());
                final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
                sourceColumnFinder.addSources(job);

                Table table = null;
                String tableName = null;
                for (InputColumn<?> inputColumn : inputColumns) {
                    table = sourceColumnFinder.findOriginatingTable(inputColumn);
                    if (table != null) {
                        tableName = table.getName();
                        break;
                    }
                }

                assert table != null;

                _progressInformationPanel.addUserLog("Analyzer '" + LabelUtils.getLabel(analyzerJob)
                        + "' finished, adding result to tab of " + tableName);
                addResult(analyzerJob, result);
            }

            @Override
            public void errorInFilter(AnalysisJob job, final FilterJob filterJob, InputRow row,
                    final Throwable throwable) {
                _progressInformationPanel.addUserLog(
                        "An error occurred in the filter: " + LabelUtils.getLabel(filterJob), throwable, true);
            }

            @Override
            public void errorInTransformer(AnalysisJob job, final TransformerJob transformerJob, InputRow row,
                    final Throwable throwable) {
                _progressInformationPanel
                        .addUserLog("An error occurred in the transformer: " + LabelUtils.getLabel(transformerJob),
                                throwable, true);
            }

            @Override
            public void errorInAnalyzer(AnalysisJob job, final AnalyzerJob analyzerJob, InputRow row,
                    final Throwable throwable) {
                _progressInformationPanel.addUserLog(
                        "An error occurred in the analyzer: " + LabelUtils.getLabel(analyzerJob), throwable, true);
            }

            @Override
            public void errorUknown(AnalysisJob job, final Throwable throwable) {
                onUnexpectedError(job, throwable);
            }
        };
    }

    protected void updateButtonVisibility(boolean running) {
        _cancelButton.setVisible(running);

        for (JComponent pluggableButton : _pluggableButtons) {
            pluggableButton.setVisible(!running);
        }
        _saveButton.setVisible(!running);
        _publishButton.setVisible(!running);
        _exportButton.setVisible(!running);
    }
}
