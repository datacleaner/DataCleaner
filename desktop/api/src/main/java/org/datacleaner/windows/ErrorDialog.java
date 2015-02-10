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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.jdesktop.swingx.JXEditorPane;
import org.jdesktop.swingx.action.OpenBrowserAction;

import com.google.common.base.Strings;

/**
 * Error dialog for simple error messages (without stack traces) in DataCleaner.
 */
public class ErrorDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final String _shortMessage;
    private final String _detailedMessage;

    public ErrorDialog(String shortMessage, String detailedMessage) {
        super(null, ImageManager.get().getImage("images/window/banner-error.png"));
        _shortMessage = shortMessage;
        _detailedMessage = detailedMessage;
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
        return 500;
    }

    @Override
    protected JComponent getDialogContent() {
        final JXEditorPane detailedMessagePane = new JXEditorPane("text/html", _detailedMessage);
        detailedMessagePane.setEditable(false);
        detailedMessagePane.setOpaque(false);
        detailedMessagePane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
                    final String href = event.getDescription();
                    if (!Strings.isNullOrEmpty(href)) {
                        final OpenBrowserAction openBrowserAction = new OpenBrowserAction(href);
                        openBrowserAction.actionPerformed(null);
                    }
                }
            }
        });
        detailedMessagePane.setBorder(new EmptyBorder(10, 10, 40, 10));

        final JButton button = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_CLOSE_BRIGHT);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ErrorDialog.this.close();
            }
        });

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(detailedMessagePane, BorderLayout.CENTER);
        panel.add(DCPanel.flow(Alignment.CENTER, button), BorderLayout.SOUTH);
        return panel;
    }
}
