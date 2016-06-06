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
package org.datacleaner.panels;

import java.awt.*;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.datacleaner.actions.MoveComponentTimerActionListener;
import org.datacleaner.api.ShortNews.Items;
import org.datacleaner.restclient.NewsChannelRESTClient;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCHtmlBox;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel that shows News form DataCloud
 */
public class NewsChannelPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int WIDTH = 360;
    private static final int POSITION_Y = 130;

    private final DCGlassPane _glassPane;
    private final Color _background = WidgetUtils.BG_COLOR_BRIGHTEST;
    private final Color _foreground = WidgetUtils.BG_COLOR_DARKEST;
    private final Color _borderColor = WidgetUtils.BG_COLOR_MEDIUM;

    private final JScrollPane scroll;

    public NewsChannelPanel(DCGlassPane glassPane, UserPreferences userPreferences) {
        super();
        _glassPane = glassPane;
        setVisible(false);
        setLocation(getXWhenOut(), POSITION_Y);
        this.setSize(WIDTH, 400);

        JComponent content = createContentPanel();
        scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        this.setLayout(new BorderLayout());
        this.add(scroll);
        this.setBorder(new CompoundBorder(new LineBorder(_borderColor, 1), new EmptyBorder(20, 20, 20, 30)));
    }

    private JComponent createContentPanel() {
        JPanel p = new JPanel() {
            // this trick effectively makes the panel with fixed width, but flexible height.
            public Dimension getPreferredSize() {
                Dimension s = super.getPreferredSize();
                s.width = WIDTH - scroll.getVerticalScrollBar().getWidth() - 50;
                return s;
            }
        };
        p.setLayout(new VerticalLayout(5));
        p.setBackground(WidgetUtils.BG_COLOR_BRIGHTEST);

        DCLabel header = DCLabel.darkMultiLine("News Channel");
        header.setFont(WidgetUtils.FONT_HEADER1);
        header.setIcon(ImageManager.get().getImageIcon("images/editions/community.png"));
        p.add(header);

        java.util.List<Items> newsitems = getNews();
        String divStyles = "background-color: #F5F5F5; padding: 5px;";
        String titleStyles = "font-weight: bold;";
        String dateStyles = "font-size: 80%; color: grey;";
        String msgStyles = "font-size: 90%;";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for(int i = 0; i < newsitems.size(); i++) {
            Items newsitem = newsitems.get(i);
            DCHtmlBox text = new DCHtmlBox("<div style='" + divStyles + "'>" +
                    "<span style='" + titleStyles + "'>" + newsitem.getTitle() + "</span>" +
                    " <span style='" + dateStyles + "'>(" + formatter.format(newsitem.getDateCreated()) + ")</span>" +
                    "<br /><span style='" + msgStyles + "'>" + newsitem.getMessage() + "</span>" +
                    " <a href='http://127.0.0.1:8888/newsitem/" + newsitem.getName() + "' style='text-decoration: none;'>More.</a>" +
                    "</div>");
            p.add(text);
        }
        return p;
    }

    private java.util.List<Items> getNews() {
        NewsChannelRESTClient client = new NewsChannelRESTClient("http://127.0.0.1:8888/ws/lastnews");
        return client.getNews(3);
    }

    private int getXWhenOut() {
        return _glassPane.getSize().width + WIDTH + 10;
    }

    private int getXWhenIn() {
        return _glassPane.getSize().width - WIDTH + 10;
    }

    public void moveIn(int delay) {
        setLocation(getXWhenOut(), POSITION_Y);
        setVisible(true);
        _glassPane.add(this);
        final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenIn(), POSITION_Y, 40) {
            @Override
            protected void done() {
            }
        });
        timer.setInitialDelay(delay);
        timer.start();
    }

    public void moveOut(int delay) {
        final Timer timer = new Timer(10, new MoveComponentTimerActionListener(this, getXWhenOut(), POSITION_Y, 40) {
            @Override
            protected void done() {
                NewsChannelPanel me = NewsChannelPanel.this;
                me.setVisible(false);
                _glassPane.remove(me);
            }
        });
        timer.setInitialDelay(delay);
        timer.start();
    }

    @Override
    public Color getBackground() {
        return _background;
    }

    @Override
    public Color getForeground() {
        return _foreground;
    }
}
