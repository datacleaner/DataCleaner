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
package org.datacleaner.widgets;

import java.awt.*;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HTML viewer.
 */
public class DCHtmlBox extends JEditorPane {
    private static final Logger logger = LoggerFactory.getLogger(DCHtmlBox.class);
    private static final long serialVersionUID = 1L;
    private static final String HTML_START_TAG = "<html>";
    private static final String HTML_END_TAG = "</html>";
    private static final String CONTENT_TYPE_HTML = "text/html";

    public DCHtmlBox(String text) {
        super();

        setEditorKit(JEditorPane.createEditorKitForContentType(this.CONTENT_TYPE_HTML));
        setEditable(false);
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        setFont(WidgetUtils.FONT_NORMAL);
        addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    (new LinkRunner(e.getURL())).execute();
                }
            }
        });

        if (text != null) {
            setText(text);
        }
    }

    public void setText(String text) {
        if (text.startsWith(this.HTML_START_TAG) && text.endsWith(this.HTML_END_TAG)) {
            text = text.substring(this.HTML_START_TAG.length(), text.length() - this.HTML_END_TAG.length());
        }

        super.setText(this.HTML_START_TAG + getTableHtml(text) + this.HTML_END_TAG);
    }

    private String getTableHtml(String content) {
        return "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>" + content + "</td></tr></table>";
    }

    private static class LinkRunner extends SwingWorker<Void, Void> {
        private URL url = null;

        private LinkRunner(URL url) {
            if (url != null) {
                this.url = url;
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            Desktop.getDesktop().browse(url.toURI());

            return null;
        }

        @Override
        protected void done() {
            try {
                get();
            } catch (ExecutionException | InterruptedException e) {
                logger.warn(e.getMessage());
            }
        }
    }
}
