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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.apache.commons.vfs2.FileObject;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.Ref;
import org.datacleaner.actions.ExportResultToHtmlActionListener;
import org.datacleaner.actions.PublishResultToMonitorActionListener;
import org.datacleaner.actions.SaveAnalysisResultActionListener;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.api.ExecutionLogMessage;
import org.datacleaner.api.InputRow;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.guice.JobFile;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.ImmutableAnalyzerJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.concurrent.PreviousErrorsExistException;
import org.datacleaner.job.runner.AnalysisJobCancellation;
import org.datacleaner.job.runner.AnalysisJobMetrics;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.AnalysisListenerAdaptor;
import org.datacleaner.job.runner.ComponentMetrics;
import org.datacleaner.job.runner.RowProcessingMetrics;
import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.result.AnalyzerResultPanel;
import org.datacleaner.panels.result.ProgressInformationPanel;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.AnalysisRunnerSwingWorker;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.WindowSizePreferences;
import org.datacleaner.widgets.DCPersistentSizedPanel;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.PopupButton.MenuPosition;
import org.datacleaner.widgets.tabs.Tab;
import org.datacleaner.widgets.tabs.VerticalTabbedPane;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Window in which the result (and running progress information) of job
 * execution is shown.
 */
public final class ResultWindow extends AbstractWindow implements WindowListener {
    private static final Logger logger = LoggerFactory.getLogger(ResultWindow.class);

    public static final List<Func<ResultWindow, JComponent>> PLUGGABLE_BANNER_COMPONENTS = new ArrayList<>(
            0);
    private static final long serialVersionUID = 1L;
    private static final ImageManager imageManager = ImageManager.get();

    private final VerticalTabbedPane _tabbedPane;
    private final Map<ComponentJob, Tab<AnalyzerResultPanel>> _resultPanels;
    private final AnalysisJob _job;
    private final DataCleanerConfiguration _configuration;
    private final ProgressInformationPanel _progressInformationPanel;
    private final RendererFactory _rendererFactory;
    private final FileObject _jobFilename;
    private final AnalysisRunnerSwingWorker _worker;
    private final UserPreferences _userPreferences;
    private final JButton _cancelButton;
    private final PopupButton _saveResultsPopupButton;
    private final WindowSizePreferences _windowSizePreference;

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
     * @param userPreferences
     * @param rendererFactory
     */
    @Inject
    protected ResultWindow(DataCleanerConfiguration configuration, @Nullable AnalysisJob job,
            @Nullable AnalysisResult result, @Nullable @JobFile FileObject jobFilename, WindowContext windowContext,
            UserPreferences userPreferences, RendererFactory rendererFactory) {
        super(windowContext);
        final boolean running = (result == null);

        _resultPanels = new IdentityHashMap<>();
        _configuration = configuration;
        _job = job;
        _jobFilename = jobFilename;
        _userPreferences = userPreferences;
        _rendererFactory = rendererFactory;

        final Ref<AnalysisResult> resultRef = new Ref<AnalysisResult>() {
            @Override
            public AnalysisResult get() {
                return getResult();
            }
        };

        Border buttonBorder = new CompoundBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE, new EmptyBorder(10, 4, 10, 4));
        _cancelButton = WidgetFactory.createDefaultButton("Cancel job", IconUtils.ACTION_STOP);
        _cancelButton.setHorizontalAlignment(SwingConstants.LEFT);
        _cancelButton.setBorder(buttonBorder);

        _saveResultsPopupButton = WidgetFactory.createDefaultPopupButton("Save results", IconUtils.ACTION_SAVE_DARK);
        _saveResultsPopupButton.setHorizontalAlignment(SwingConstants.LEFT);
        _saveResultsPopupButton.setBorder(buttonBorder);
        _saveResultsPopupButton.setMenuPosition(MenuPosition.TOP);
        _saveResultsPopupButton.getMenu().setBorder(new MatteBorder(1, 0, 0, 1, WidgetUtils.BG_COLOR_MEDIUM));

        JMenuItem saveAsFileItem = WidgetFactory.createMenuItem("Save as result file", IconUtils.ACTION_SAVE_DARK);
        saveAsFileItem.addActionListener(new SaveAnalysisResultActionListener(resultRef, _userPreferences));
        saveAsFileItem.setBorder(buttonBorder);
        _saveResultsPopupButton.getMenu().add(saveAsFileItem);

        JMenuItem exportToHtmlItem = WidgetFactory.createMenuItem("Export to HTML", IconUtils.WEBSITE);
        exportToHtmlItem.addActionListener(new ExportResultToHtmlActionListener(resultRef, _configuration,
                _userPreferences));
        exportToHtmlItem.setBorder(buttonBorder);
        _saveResultsPopupButton.getMenu().add(exportToHtmlItem);

        JMenuItem publishToServerItem = WidgetFactory.createMenuItem("Publish to server", IconUtils.MENU_DQ_MONITOR);
        publishToServerItem.addActionListener(new PublishResultToMonitorActionListener(getWindowContext(),
                _userPreferences, resultRef, _jobFilename));
        publishToServerItem.setBorder(buttonBorder);
        _saveResultsPopupButton.getMenu().add(publishToServerItem);

        _tabbedPane = new VerticalTabbedPane() {
            private static final long serialVersionUID = 1L;

            @Override
            protected JComponent wrapInCollapsiblePane(final JComponent originalPanel) {
                DCPanel buttonPanel = new DCPanel();
                buttonPanel.setLayout(new VerticalLayout());
                buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, WidgetUtils.BG_COLOR_MEDIUM));

                buttonPanel.add(_saveResultsPopupButton);
                buttonPanel.add(_cancelButton);

                DCPanel wrappedPanel = new DCPanel();
                wrappedPanel.setLayout(new BorderLayout());
                wrappedPanel.add(originalPanel, BorderLayout.CENTER);
                wrappedPanel.add(buttonPanel, BorderLayout.SOUTH);
                return super.wrapInCollapsiblePane(wrappedPanel);
            }
        };

        final Dimension size = getDefaultWindowSize();
        _windowSizePreference = new WindowSizePreferences(_userPreferences, getClass(), size.width, size.height);
        _progressInformationPanel = new ProgressInformationPanel(running);
        _tabbedPane.addTab("Progress information",
                imageManager.getImageIcon("images/model/progress_information.png", IconUtils.ICON_SIZE_TAB),
                _progressInformationPanel);

        for (Func<ResultWindow, JComponent> pluggableComponent : PLUGGABLE_BANNER_COMPONENTS) {
            JComponent component = pluggableComponent.eval(this);
            if (component != null) {
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    JMenuItem menuItem = WidgetFactory.createMenuItem(button.getText(), button.getIcon());
                    for (ActionListener listener : button.getActionListeners()) {
                        menuItem.addActionListener(listener);
                    }
                    menuItem.setBorder(buttonBorder);
                    _saveResultsPopupButton.getMenu().add(menuItem);
                } else if (component instanceof JMenuItem) { // TODO: Not possible. JMenuItem is a subclass of AbstractButton. Reorder or remove?
                    JMenuItem menuItem = (JMenuItem) component;
                    menuItem.setBorder(buttonBorder);
                    _saveResultsPopupButton.getMenu().add(menuItem);
                }
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

            final Map<ComponentJob, AnalyzerResult> map = result.getResultMap();
            for (Entry<ComponentJob, AnalyzerResult> entry : map.entrySet()) {
                final ComponentJob componentJob = entry.getKey();
                final AnalyzerResult analyzerResult = entry.getValue();

                addResult(componentJob, analyzerResult);
            }
            _progressInformationPanel.onSuccess();

            WidgetUtils.invokeSwingAction(new Runnable() {
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

    public void startAnalysis() {
        _worker.execute();
    }

    private Dimension getDefaultWindowSize() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int screenWidth = screenSize.width;
        final int screenHeight = screenSize.height;

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

        return new Dimension(width, height);
    }

    public Tab<AnalyzerResultPanel> getOrCreateResultPanel(final ComponentJob componentJob, boolean finished) {
        synchronized (_resultPanels) {
            final Tab<AnalyzerResultPanel> existingTab = _resultPanels.get(componentJob);
            if (existingTab != null) {
                return existingTab;
            }

            String title = LabelUtils.getLabel(componentJob, false, false, false);
            if (title.length() > 40) {
                title = title.substring(0, 39) + "...";
            }

            final Icon icon = IconUtils.getDescriptorIcon(componentJob.getDescriptor(), IconUtils.ICON_SIZE_TAB);
            final AnalyzerResultPanel resultPanel = new AnalyzerResultPanel(_rendererFactory,
                    _progressInformationPanel, componentJob);
            final Tab<AnalyzerResultPanel> tab = _tabbedPane.addTab(title, icon, resultPanel);
            tab.setTooltip(LabelUtils.getLabel(componentJob, false, true, true));

            _resultPanels.put(componentJob, tab);

            return tab;
        }
    }

    public void addResult(final ComponentJob componentJob, final AnalyzerResult result) {
        final Tab<AnalyzerResultPanel> tab = getOrCreateResultPanel(componentJob, true);
        tab.getContents().setResult(result);
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
        return imageManager.getImage(IconUtils.MODEL_RESULT);
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
        final String datastoreName = getDatastoreName();
        String bannerTitle = "Analysis results";
        if (!StringUtils.isNullOrEmpty(datastoreName)) {
            bannerTitle = bannerTitle + " | " + datastoreName;

            if (_jobFilename != null) {
                bannerTitle = bannerTitle + " | " + _jobFilename.getName().getBaseName();
            }
        }

        final DCBannerPanel banner = new DCBannerPanel(imageManager.getImage("images/window/banner-results.png"),
                bannerTitle);
        _tabbedPane.addListener(new VerticalTabbedPane.Listener() {
            @Override
            public void stateChanged(int newIndex, Tab<?> newTab) {
                banner.setTitle2(newTab.getTitle());
                banner.updateUI();
            }
        });

        final DCPanel panel = new DCPersistentSizedPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND, _windowSizePreference);
        panel.setLayout(new BorderLayout());
        panel.add(banner, BorderLayout.NORTH);
        panel.add(_tabbedPane, BorderLayout.CENTER);
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

    /**
     * Sets the result, when it is ready for eg. saving
     *
     * @param result
     */
    public void setResult(AnalysisResult result) {
        _result = result;
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

    public DataCleanerConfiguration getConfiguration() {
        return _configuration;
    }

    public AnalysisJob getJob() {
        return _job;
    }

    public UserPreferences getUserPreferences() {
        return _userPreferences;
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
                WidgetUtils.invokeSwingAction(new Runnable() {
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
            public void onComponentMessage(AnalysisJob job, ComponentJob componentJob, ComponentMessage message) {

                if (message instanceof ExecutionLogMessage) {
                    final String messageString = ((ExecutionLogMessage) message).getMessage();
                    final String componentLabel = LabelUtils.getLabel(componentJob);

                    _progressInformationPanel.addUserLog(messageString + " (" + componentLabel + ")");
                }

            }

            @Override
            public void rowProcessingBegin(final AnalysisJob job, final RowProcessingMetrics metrics) {
                logger.info("rowProcessingBegin: {}", job.getDatastore().getName());
                final int expectedRows = metrics.getExpectedRows();
                final Table table = metrics.getTable();
                WidgetUtils.invokeSwingAction(new Runnable() {
                    @Override
                    public void run() {
                        final ComponentJob[] componentJobs = metrics.getResultProducers();
                        // Put analyzers at the top, then the rest (untouched)
                        Arrays.sort(componentJobs, new Comparator<ComponentJob>() {

                            @Override
                            public int compare(ComponentJob o1, ComponentJob o2) {
                                if ((o1 instanceof ImmutableAnalyzerJob) && !(o2 instanceof ImmutableAnalyzerJob)) {
                                    return -1;
                                }
                                if ((o2 instanceof ImmutableAnalyzerJob) && !(o1 instanceof ImmutableAnalyzerJob)) {
                                    return 1;
                                }
                                return 0;
                            }
                        });

                        for (ComponentJob componentJob : componentJobs) {
                            // instantiate result panels
                            getOrCreateResultPanel(componentJob, false);
                        }
                        _tabbedPane.updateUI();

                        if (expectedRows == -1) {
                            _progressInformationPanel.addUserLog("Starting processing of " + table.getName());
                        } else {
                            _progressInformationPanel.addUserLog("Starting processing of " + table.getName()
                                    + " (approx. " + expectedRows + " rows)");
                            _progressInformationPanel.setExpectedRows(table, expectedRows);
                        }
                    }
                });
            }

            @Override
            public void rowProcessingProgress(AnalysisJob job, final RowProcessingMetrics metrics, final InputRow row,
                    final int currentRow) {
                logger.info("rowProcessingProgress: {}", job.getDatastore().getName());

                _progressInformationPanel.updateProgress(metrics.getTable(), currentRow);
            }

            @Override
            public void rowProcessingSuccess(AnalysisJob job, final RowProcessingMetrics metrics) {
                logger.info("rowProcessingSuccess: {}", job.getDatastore().getName());
                _progressInformationPanel.updateProgressFinished(metrics.getTable());
                _progressInformationPanel.addUserLog("Processing of " + metrics.getTable().getName()
                        + " finished. Generating results...");
            }

            @Override
            public void componentBegin(AnalysisJob job, final ComponentJob componentJob, ComponentMetrics metrics) {
                _progressInformationPanel.addUserLog("Starting component '" + LabelUtils.getLabel(componentJob) + "'");
            }

            @Override
            public void componentSuccess(AnalysisJob job, final ComponentJob componentJob,
                    final AnalyzerResult result) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Component ");
                sb.append(LabelUtils.getLabel(componentJob));
                sb.append(" finished.");

                if (result == null) {
                    _progressInformationPanel.addUserLog(sb.toString());
                } else {
                    sb.append(" Adding result...");
                    _progressInformationPanel.addUserLog(sb.toString());
                    WidgetUtils.invokeSwingAction(new Runnable() {
                        @Override
                        public void run() {
                            addResult(componentJob, result);
                        }
                    });
                }
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

    protected void updateButtonVisibility(final boolean running) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                _cancelButton.setVisible(running);
                _saveResultsPopupButton.setVisible(!running);
            }
        });
    }

    public void windowClosed(WindowEvent e) {
        if (this.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            _windowSizePreference.setUserPreferredSize(null, true);
        } else {
            _windowSizePreference.setUserPreferredSize(getSize(), false);
        }
    }

    @Override
    protected boolean maximizeWindow() {
        return _windowSizePreference.isWindowMaximized();
    }
}
