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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Scrollable;
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
public class AnalyzerResultPanel extends DCPanel implements Scrollable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerResultPanel.class);

    private final RendererFactory _rendererFactory;
    private final ProgressInformationPanel _progressInformationPanel;
    private final ComponentJob _componentJob;
    private final LoadingIcon _loadingIcon;

    public AnalyzerResultPanel(RendererFactory rendererFactory, ProgressInformationPanel progressInformationPanel,
            ComponentJob componentJob) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _rendererFactory = rendererFactory;
        _progressInformationPanel = progressInformationPanel;
        _componentJob = componentJob;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();
        final Icon icon = IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_TASK_PANE);

        final String headerText = getHeaderText();
        final JLabel header1 = createHeader(icon, headerText, WidgetUtils.FONT_HEADER1, WidgetUtils.BG_COLOR_DARK);
        final JLabel header2 = createHeader(null, getSubHeaderText(componentJob, headerText), WidgetUtils.FONT_SMALL,
                WidgetUtils.BG_COLOR_BLUE_MEDIUM);

        final DCPanel headerPanel = new DCPanel();
        headerPanel.setLayout(new VerticalLayout(4));
        headerPanel.add(header1);
        headerPanel.add(header2);
        headerPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
        add(headerPanel, BorderLayout.NORTH);

        _loadingIcon = new LoadingIcon();
        add(_loadingIcon, BorderLayout.CENTER);
    }

    public void setResult(final AnalyzerResult result) {
        final String headerText = getHeaderText();
        _progressInformationPanel.addUserLog("Rendering result for " + headerText);

        // use a swing worker to run the rendering in the background
        new SwingWorker<JComponent, Void>() {

            @Override
            protected JComponent doInBackground() throws Exception {
                final Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(
                        result, SwingRenderingFormat.class);
                if (renderer == null) {
                    final String message = "No renderer found for result type " + result.getClass().getName();
                    logger.error(message);
                    throw new IllegalStateException(message);
                }
                logger.debug("renderer.render({})", result);
                final JComponent component = renderer.render(result);
                if (logger.isInfoEnabled()) {
                    final String resultAsString = getResultAsString(_componentJob, result);
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
                    if (result instanceof AnalyzerResultFuture) {
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

                    final String resultAsString = getResultAsString(_componentJob, result);
                    if (resultAsString != null) {
                        final DCLabel label = DCLabel.darkMultiLine(resultAsString);
                        label.setBorder(WidgetUtils.BORDER_EMPTY);
                        panel.add(label);
                    }
                    component = panel;
                }

                remove(_loadingIcon);
                add(component, BorderLayout.CENTER);

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

    private String getHeaderText() {
        return LabelUtils.getLabel(_componentJob, false, false, false);
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

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return WidgetUtils.SCROLL_UNIT_INCREMENT;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        // page down scrolls almost a full screen size
        return visibleRect.height - 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        // ensure that the width within the scroll area never expands the
        // viewport
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
