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
package org.datacleaner.panels.result;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultFuture;
import org.datacleaner.api.Renderer;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.windows.ResultWindow;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that displays the rendered result in the {@link ResultWindow}.
 */
public class AnalyzerResultPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerResultPanel.class);

    private final RendererFactory _rendererFactory;
    private final ProgressInformationPanel _progressInformationPanel;
    private final ComponentJob _componentJob;
    private final AnalyzerResult _result;

    public AnalyzerResultPanel(RendererFactory rendererFactory, ProgressInformationPanel progressInformationPanel,
            ComponentJob componentJob, AnalyzerResult result) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _rendererFactory = rendererFactory;
        _progressInformationPanel = progressInformationPanel;
        _result = result;
        _componentJob = componentJob;
        setLayout(new VerticalLayout(4));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        final Icon icon = IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_TASK_PANE);

        final String headerText = getHeaderText(componentJob);
        final JLabel header1 = createHeader(icon, headerText, WidgetUtils.FONT_HEADER1, WidgetUtils.BG_COLOR_DARK);
        final JLabel header2 = createHeader(null, getSubHeaderText(componentJob, headerText), WidgetUtils.FONT_SMALL,
                WidgetUtils.BG_COLOR_BLUE_MEDIUM);

        add(header1);
        add(header2);

        final LoadingIcon loadingIcon = new LoadingIcon();
        add(loadingIcon);

        _progressInformationPanel.addUserLog("Rendering result for " + headerText);

        // use a swing worker to run the rendering in the background
        new SwingWorker<JComponent, Void>() {

            @Override
            protected JComponent doInBackground() throws Exception {
                final Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(
                        _result, SwingRenderingFormat.class);
                if (renderer == null) {
                    final String message = "No renderer found for result type " + _result.getClass().getName();
                    logger.error(message);
                    throw new IllegalStateException(message);
                }
                logger.debug("renderer.render({})", _result);
                final JComponent component = renderer.render(_result);
                if (logger.isInfoEnabled()) {
                    final String resultAsString = getResultAsString(_componentJob, _result);
                    if (resultAsString != null) {
                        String resultAsStringToLog = resultAsString.replaceAll("\n", " | ");
                        if (resultAsStringToLog.length() > 150) {
                            resultAsStringToLog = resultAsStringToLog.substring(0, 147) + "...";
                        }
                        logger.info("renderer.render({}) returned: {}", resultAsStringToLog, component);
                    }
                }
                return component;
            }

            protected void done() {
                JComponent component;
                try {
                    component = get();
                    if (_result instanceof AnalyzerResultFuture) {
                        _progressInformationPanel.addUserLog(headerText + " is still in progress - see the '"
                                + _componentJob.getDescriptor().getDisplayName() + "' tab");
                    } else {
                        _progressInformationPanel.addUserLog("Result rendered for " + headerText);
                    }
                } catch (Exception e) {
                    logger.error("Error occurred while rendering result", e);
                    _progressInformationPanel.addUserLog("Error occurred while rendering result", e, false);

                    final DCPanel panel = new DCPanel();
                    panel.setLayout(new VerticalLayout(4));

                    final ImageIcon icon = ImageManager.get().getImageIcon(IconUtils.STATUS_ERROR);
                    panel.add(new JLabel(
                            "An error occurred while rendering result, check the 'Progress information' tab.", icon,
                            SwingConstants.LEFT));

                    final String resultAsString = getResultAsString(_componentJob, _result);
                    if (resultAsString != null) {
                        final DCLabel label = DCLabel.darkMultiLine(resultAsString);
                        label.setBorder(WidgetUtils.BORDER_EMPTY);
                        panel.add(label);
                    }
                    component = panel;
                }

                // a container panel around the rendered panel helps set the
                // appropriate size
                final DCPanel containerPanel = new DCPanel();
                containerPanel.setLayout(new BorderLayout());
                containerPanel.add(component, BorderLayout.CENTER);

                remove(loadingIcon);
                add(containerPanel);
                updateUI();
            };

        }.execute();
    }

    private JLabel createHeader(Icon icon, String header, Font font, Color color) {
        final JLabel label = new JLabel(header, icon, JLabel.LEFT);
        label.setOpaque(false);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private String getHeaderText(final ComponentJob componentJob) {
        return LabelUtils.getLabel(componentJob, false, false, false);
    }

    private String getSubHeaderText(final ComponentJob componentJob, final String headerText) {
        String subHeaderString = LabelUtils.getLabel(componentJob, true, true, true);
        final int indexOfHeader = subHeaderString.indexOf(headerText);
        if (indexOfHeader != -1) {
            // remove the redundant part of both headers
            subHeaderString = subHeaderString.substring(indexOfHeader + headerText.length());
        }
        return subHeaderString;
    }

    protected String getResultAsString(ComponentJob componentJob, AnalyzerResult result) {
        try {
            return result.toString();
        } catch (Exception ex) {
            logger.error("Couldn't render result of {} as label using toString() method", componentJob, ex);
            return null;
        }
    }
}
