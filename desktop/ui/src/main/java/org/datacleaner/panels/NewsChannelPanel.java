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
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.datacleaner.actions.MoveComponentTimerActionListener;
import org.datacleaner.api.ShortNews;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.util.IconUtils;
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
    private final java.util.List<ShortNews.Item> _newsitems;
    private final long _lastCheck;
    private java.util.List<NewsDCHtmlBox> _newsBoxes = new ArrayList<>();

    public NewsChannelPanel(DCGlassPane glassPane, java.util.List<ShortNews.Item> newsitems, long lastCheck) {
        super();
        _glassPane = glassPane;
        _newsitems = newsitems;
        _lastCheck = lastCheck;
        setVisible(false);
        setLocation(getXWhenOut(), POSITION_Y);
        this.setSize(WIDTH, 500);

        JComponent content = createContentPanel();
        scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        this.setLayout(new BorderLayout());
        this.add(scroll);
        this.setBorder(new CompoundBorder(new LineBorder(_borderColor, 1), new EmptyBorder(20, 20, 20, 10)));
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
        header.setIcon(ImageManager.get().getImageIcon(IconUtils.NEWS_CHANNEL_ORANGE));
        p.add(header);

        if(_newsitems .size() == 0) {
            DCLabel label = DCLabel.darkMultiLine("You have not received any News.");
            label.setFont(WidgetUtils.FONT_HEADER2);
            p.add(label);
        } else {
            for (int i = 0; i < _newsitems.size(); i++) {
                NewsDCHtmlBox newsDCHtmlBox = new NewsDCHtmlBox(_lastCheck, _newsitems.get(i));
                _newsBoxes.add(newsDCHtmlBox);
                p.add(newsDCHtmlBox);
            }
        }
        return p;
    }

    public void refresh(long lastCheck){
        for(NewsDCHtmlBox box : _newsBoxes) {
            box.setLastCheck(lastCheck);
        }
        this.repaint();
    }

    public void scrollToTop() {
        scroll.getViewport().setViewPosition(new Point(0,0));
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

    private class NewsDCHtmlBox extends DCHtmlBox{
        private final ShortNews.Item _item;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        private long _lastCheckTime;

        private final String divStyles = "padding: 5px; border-bottom: 1px #DCDCDC solid; margin-right: 15px; background-color: ";
        private final String titleStyles = "font-weight: bold;";
        private final String dateStyles = "font-size: 80%; color: grey;";
        private final String msgStyles = "font-size: 90%;";

        public NewsDCHtmlBox(long lastCheck, ShortNews.Item item) {
            super(null);
            _item = item;
            _lastCheckTime = lastCheck;
            setContentText();
        }

        private void setLastCheck(Long lastCheck){
            _lastCheckTime = lastCheck;
            setContentText();
        }

        private void setContentText() {
            setText("<div style='" + divStyles + getBgColor() + "'>" +
                    "<span style='" + titleStyles + "'>" + _item.getTitle() + "</span>" +
                    " <span style='" + dateStyles + "'>(" + formatter.format(_item.getDateCreated()) + ")</span>" +
                    "<br /><span style='" + msgStyles + "'>" + _item.getMessage() + "</span>" +
                    " <a href='" + RemoteDescriptorProvider.DATACLOUD_URL + "/newsitem/" + _item.getName() +
                    "' style='text-decoration: none;'>More.</a>" +
                    "</div>");
        }

        private String getBgColor() {
            String backgroundItemColor = "#FFFFFF;";
            java.util.Date itemDate = _item.getDateCreated();
            if(itemDate.getTime() > _lastCheckTime) {
                backgroundItemColor = "#F5F5F5;";
            }
            return  backgroundItemColor;
        }
    }
}
