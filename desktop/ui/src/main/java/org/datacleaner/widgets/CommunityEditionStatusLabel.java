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

import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.Version;
import org.datacleaner.VersionComparator;
import org.datacleaner.actions.OpenDataCleanerWebsiteActionListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.ws.NaiveHostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Label that shows the details about community edition of DataCleaner
 */
public class CommunityEditionStatusLabel extends JLabel {

    private static final Logger logger = LoggerFactory.getLogger(CommunityEditionStatusLabel.class);
    
    private static final long serialVersionUID = 1L;

    private boolean _updateAvailable;

    public CommunityEditionStatusLabel() {
        super(Version.getEdition());
        _updateAvailable = false;
        setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        checkForUpdates();

        setIcon(ImageManager.get().getImageIcon("images/editions/community.png", IconUtils.ICON_SIZE_SMALL));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                onMouseClick();
            }
        });
    }

    private void checkForUpdates() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                final HttpUriRequest req =
                        RequestBuilder.get(OpenDataCleanerWebsiteActionListener.createUrl("/meta/versions.json")).build();

                final CloseableHttpClient client = HttpClients.custom()
                        // because the HTTPS cert for datacleaner.github.io is registered for github.com, the hostname
                        // verifier has to be more loose than default.
                        .setSSLHostnameVerifier(new NaiveHostnameVerifier()).build();
                try {
                    try (CloseableHttpResponse resp = client.execute(req)) {
                        final String body = EntityUtils.toString(resp.getEntity());
                        final Map<?, ?> bodyAsMap = new ObjectMapper().readValue(body, Map.class);
                        final String latestVersion = (String) CollectionUtils.find(bodyAsMap, "latest.version");
                        return latestVersion;
                    }
                } finally {
                    client.close();
                }
            }

            protected void done() {
                try {
                    final String latestVersion = get();
                    final VersionComparator comparator = new VersionComparator();
                    final String currentVersion = Version.getVersion();
                    if (comparator.isComparable(currentVersion)) {
                        final int diff = comparator.compare(currentVersion, latestVersion);
                        if (diff < 0) {
                            setText(Version.getEdition() + " - Update available: " + latestVersion);
                            _updateAvailable = true;
                        } else {
                            setText(Version.getEdition() + " - Up to date");
                        }
                    } else {
                        setText(Version.getEdition() + " - Update available: " + latestVersion);
                        _updateAvailable = true;
                    }
                } catch (Exception e) {
                    logger.warn("Failed to retrieve and compare latest version: {}", e.getMessage());
                }
            }
        }.execute();
    }

    protected void onMouseClick() {
        final ActionListener action;
        if (_updateAvailable) {
            action = new OpenDataCleanerWebsiteActionListener("/downloads");
        } else {
            action = new OpenDataCleanerWebsiteActionListener();
        }
        action.actionPerformed(null);
    }
}
