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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

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

    private final Color _background = WidgetUtils.BG_COLOR_BRIGHTEST;
    private final Color _foreground = WidgetUtils.BG_COLOR_DARKEST;

    private final JScrollPane scroll;
    private final List<ShortNews.Item> _newsitems;
    private final long _lastCheck;
    private List<NewsDCHtmlBox> _newsBoxes = new ArrayList<>();

    public NewsChannelPanel(List<ShortNews.Item> newsitems, long lastCheck) {
        super();
        _newsitems = newsitems;
        _lastCheck = lastCheck;

        JComponent content = createContentPanel();
        scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        this.setLayout(new BorderLayout());
        this.add(scroll);
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
        header.setIcon(ImageManager.get().getImageIcon(IconUtils.NEWS_CHANNEL_TITLE_ICON));
        p.add(header);

        if(_newsitems == null) {
            p.add(createWarningLabel("DataCleaner has a problem to connect to the server."));
        } else {
            if (_newsitems.size() == 0) {
                p.add(createWarningLabel("You have not received any News."));
            } else {
                for (int i = 0; i < _newsitems.size(); i++) {
                    NewsDCHtmlBox newsDCHtmlBox = new NewsDCHtmlBox(_lastCheck, _newsitems.get(i));
                    _newsBoxes.add(newsDCHtmlBox);
                    p.add(newsDCHtmlBox);
                }
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scroll.getViewport().setViewPosition(new Point(0,0));
            }
        });
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
                    " <a href='" + RemoteDescriptorProvider.DATACLEANER_BASE_URL + "/newsitem/" + _item.getName() +
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

    private DCLabel createWarningLabel(String warningText) {
        DCLabel label = DCLabel.darkMultiLine(warningText);
        label.setFont(WidgetUtils.FONT_HEADER2);
        return  label;
    }
}
