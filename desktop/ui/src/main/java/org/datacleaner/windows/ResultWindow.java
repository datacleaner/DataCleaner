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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

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
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.guice.JobFile;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterJob;
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

/**
 * Window in which the result (and running progress information) of job
 * execution is shown.
 */
public final class ResultWindow extends AbstractWindow implements WindowListener {

    private static final long serialVersionUID = 1L;

    public static final List<Func<ResultWindow, JComponent>> PLUGGABLE_BANNER_COMPONENTS = new ArrayList<Func<ResultWindow, JComponent>>(
            0);

    private static final ImageManager imageManager = ImageManager.get();

    private final VerticalTabbedPane _tabbedPane = new VerticalTabbedPane();
    private final AnalysisJob _job;
    private final DataCleanerConfiguration _configuration;
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
     * @param rendererInitializerProvider
     */
    @Inject
    protected ResultWindow(DataCleanerConfiguration configuration, @Nullable AnalysisJob job,
            @Nullable AnalysisResult result, @Nullable @JobFile FileObject jobFilename, WindowContext windowContext,
            UserPreferences userPreferences, RendererFactory rendererFactory) {
        super(windowContext);
        final boolean running = (result == null);
        _configuration = configuration;
        _job = job;
        _jobFilename = jobFilename;
        _userPreferences = userPreferences;
        _rendererFactory = rendererFactory;

        final Dimension size = getDefaultWindowSize();
        _windowSizePreference = new WindowSizePreferences(_userPreferences, getClass(), size.width, size.height);
        _progressInformationPanel = new ProgressInformationPanel(running);
        _tabbedPane.addTab("Progress information",
                imageManager.getImageIcon("images/model/progress_information.png", IconUtils.ICON_SIZE_TAB),
                _progressInformationPanel);

        _pluggableButtons = new ArrayList<JComponent>(1);

        _cancelButton = WidgetFactory.createDefaultButton("Cancel job", IconUtils.ACTION_STOP);

        final Ref<AnalysisResult> resultRef = new Ref<AnalysisResult>() {
            @Override
            public AnalysisResult get() {
                return getResult();
            }
        };

        _publishButton = WidgetFactory.createDefaultButton("Publish to server", IconUtils.MENU_DQ_MONITOR);
        _publishButton.addActionListener(new PublishResultToMonitorActionListener(getWindowContext(), _userPreferences,
                resultRef, _jobFilename));

        _saveButton = WidgetFactory.createDefaultButton("Save result", IconUtils.ACTION_SAVE_DARK);
        _saveButton.addActionListener(new SaveAnalysisResultActionListener(resultRef, _userPreferences));

        _exportButton = WidgetFactory.createDefaultButton("Export to HTML", IconUtils.WEBSITE);
        _exportButton.addActionListener(new ExportResultToHtmlActionListener(resultRef, _configuration,
                _userPreferences));

        for (Func<ResultWindow, JComponent> pluggableComponent : PLUGGABLE_BANNER_COMPONENTS) {
            JComponent component = pluggableComponent.eval(this);
            if (component != null) {
                if (component instanceof AbstractButton) {
                    // tweak buttons to fit our styling
                    AbstractButton b = (AbstractButton) component;
                    b.setOpaque(true);
                    WidgetUtils.setDefaultButtonStyle(b);
                }

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

    public void addResult(final ComponentJob componentJob, final AnalyzerResult result) {
        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        final String name = LabelUtils.getLabel(componentJob, false, false, false);
        final Icon icon = IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_TAB);
        WidgetUtils.invokeSwingAction(new Runnable() {
            @Override
            public void run() {
                final AnalyzerResultPanel resultPanel = new AnalyzerResultPanel(_rendererFactory,
                        _progressInformationPanel, componentJob, result);

                final JScrollPane scroll = WidgetUtils.scrolleable(resultPanel);
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                _tabbedPane.addTab(name, icon, scroll);
            }
        });
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
        _tabbedPane.bindTabTitleToBanner(banner);

        for (JComponent pluggableButton : _pluggableButtons) {
            banner.add(pluggableButton);
        }

        banner.add(_publishButton);
        banner.add(_exportButton);
        banner.add(_saveButton);
        banner.add(_cancelButton);
        banner.add(Box.createHorizontalStrut(10));

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
            public void rowProcessingProgress(AnalysisJob job, final RowProcessingMetrics metrics, final InputRow row,
                    final int currentRow) {
                _progressInformationPanel.updateProgress(metrics.getTable(), currentRow);
            }

            @Override
            public void rowProcessingSuccess(AnalysisJob job, final RowProcessingMetrics metrics) {
                _progressInformationPanel.updateProgressFinished(metrics.getTable());
                _progressInformationPanel.addUserLog("Processing of " + metrics.getTable().getName()
                        + " finished. Generating results ...");
            }

            @Override
            public void componentBegin(AnalysisJob job, final ComponentJob componentJob, ComponentMetrics metrics) {
                _progressInformationPanel.addUserLog("Starting component '" + LabelUtils.getLabel(componentJob) + "'");
            }

            @Override
            public void componentSuccess(AnalysisJob job, final ComponentJob componentJob, final AnalyzerResult result) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Component ");
                sb.append(LabelUtils.getLabel(componentJob));
                sb.append(" finished.");

                if (result == null) {
                    _progressInformationPanel.addUserLog(sb.toString());
                } else {
                    sb.append(" Adding result...");
                    _progressInformationPanel.addUserLog(sb.toString());
                    addResult(componentJob, result);
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

                for (JComponent pluggableButton : _pluggableButtons) {
                    pluggableButton.setVisible(!running);
                }
                _saveButton.setVisible(!running);
                _publishButton.setVisible(!running);
                _exportButton.setVisible(!running);
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
