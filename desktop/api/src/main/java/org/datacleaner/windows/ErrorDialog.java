/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;


/**
 * Error dialog for error messages in DataCleaner, optionally with a
 * scrollable stack trace of an exception.
 */
public class ErrorDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final String _shortMessage;
    private final String _detailedMessage;
    private final Throwable _exception;

    public ErrorDialog(final String shortMessage, final String detailedMessage) {
        this(shortMessage, detailedMessage, null);
    }

    public ErrorDialog(final String shortMessage, final String detailedMessage, final Throwable exception) {
        super(null, ImageManager.get().getImage("images/window/banner-error.png"));
        _shortMessage = shortMessage;
        _detailedMessage = detailedMessage;
        _exception = exception;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    public String getWindowTitle() {
        return _shortMessage;
    }

    @Override
    protected String getBannerTitle() {
        return _shortMessage;
    }

    @Override
    protected int getDialogWidth() {
        return WidgetUtils.DIALOG_WIDTH_NARROW;
    }

    @Override
    protected JComponent getDialogContent() {
        final String detailedMessage;
        if (_detailedMessage.contains("<br>") || _detailedMessage.contains("<br/>")) {
            detailedMessage = _detailedMessage;
        } else {
            detailedMessage = _detailedMessage.replace("\n", "<br/>");
        }

        final JEditorPane detailedMessagePane = new JEditorPane("text/html", detailedMessage);
        detailedMessagePane.setEditable(false);
        detailedMessagePane.setOpaque(false);
        detailedMessagePane.addHyperlinkListener(event -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
                WidgetUtils.openUrl(event.getDescription());
            }
        });
        detailedMessagePane.setBorder(WidgetUtils.BORDER_EMPTY);

        final JButton button = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_CLOSE_BRIGHT);
        button.addActionListener(e -> ErrorDialog.this.close());

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new BorderLayout(WidgetUtils.SPACING_SMALL, WidgetUtils.SPACING_SMALL));
        panel.setBorder(WidgetUtils.BORDER_EMPTY);
        panel.add(detailedMessagePane, BorderLayout.NORTH);
        panel.add(createStackTraceComponent(), BorderLayout.CENTER);
        panel.add(DCPanel.flow(Alignment.CENTER, button), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createStackTraceComponent() {
        if (_exception == null) {
            // empty, invisible filler component
            final DCPanel filler = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
            return filler;
        }

        final JTextArea stackTraceArea = new JTextArea(ExceptionUtils.getStackTrace(_exception));
        stackTraceArea.setFont(WidgetUtils.FONT_MONOSPACE);
        stackTraceArea.setEditable(false);
        stackTraceArea.setCaretPosition(0);

        final JScrollPane scrollPane = WidgetUtils.scrolleable(stackTraceArea);
        scrollPane.setPreferredSize(
                new Dimension(getDialogWidth() - WidgetUtils.SPACING_XLARGE, WidgetUtils.scalePx(150)));
        return scrollPane;
    }
}
