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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

import org.datacleaner.api.ShortNews;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.panels.DCGlassPane;
import org.datacleaner.panels.NewsChannelPanel;
import org.datacleaner.restclient.NewsChannelRESTClient;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Status Label News Channel for DataCloud
 */
public class NewsChannelStatusLabel extends JLabel {
    private static final Logger logger = LoggerFactory.getLogger(NewsChannelStatusLabel.class);
    private static final long serialVersionUID = 1L;

    private final NewsChannelPanel _newNewsChannelPanel;
    private final UserPreferences _userPreferences;

    private static final String LAST_NEWS_READING = "lastNewsReading";

    public NewsChannelStatusLabel(DCGlassPane glassPane, UserPreferences userPreferences) {
        super("News Channel");
        _userPreferences = userPreferences;
        setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        java.util.List<ShortNews.Item> newsitems = getNews();

        String lastNewsCheckValue = userPreferences.getAdditionalProperties().get(LAST_NEWS_READING);
        long lastNewsCheck = 0;
        if(lastNewsCheckValue != null) {
            lastNewsCheck = Long.valueOf(lastNewsCheckValue);
        }
        _newNewsChannelPanel = new NewsChannelPanel(glassPane, newsitems, lastNewsCheck);

        if(newsitems.size() > 0) {
            if (lastNewsCheckValue == null || newsitems.get(0).getDateCreated().getTime() > lastNewsCheck) {
                setIcon(ImageManager.get().getImageIcon(IconUtils.NEWS_BLUE, IconUtils.ICON_SIZE_SMALL));
            } else {
                setIcon(ImageManager.get().getImageIcon(IconUtils.NEWS_GREY, IconUtils.ICON_SIZE_SMALL));
            }
        } else {
            setIcon(ImageManager.get().getImageIcon(IconUtils.NEWS_GREY, IconUtils.ICON_SIZE_SMALL));
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClick(newsitems.size());
            }
        });
    }

    protected void onMouseClick(int size) {
        if (_newNewsChannelPanel.isVisible()) {
            if(size > 0) {
                _newNewsChannelPanel.refresh(Long.valueOf(_userPreferences.getAdditionalProperties().get(LAST_NEWS_READING)));
            }
            _newNewsChannelPanel.moveOut(0);
        } else {
            if(size > 0) {
                setIcon(ImageManager.get().getImageIcon(IconUtils.NEWS_GREY, IconUtils.ICON_SIZE_SMALL));
                _userPreferences.getAdditionalProperties().put(LAST_NEWS_READING, String.valueOf(new Date().getTime()));
                _userPreferences.save();
            }
            _newNewsChannelPanel.scrollToTop();
            _newNewsChannelPanel.moveIn(0);
        }
    }

    private java.util.List<ShortNews.Item> getNews() {
        try{
            NewsChannelRESTClient client = new NewsChannelRESTClient(RemoteDescriptorProvider.DATACLOUD_NEWS_CHANNEL_URL);
            return client.getNews(3);
        } catch(Exception e) {
            logger.error("Connection problem to the website service.", e.getMessage());
            return new ArrayList<>();
        }
    }
}
