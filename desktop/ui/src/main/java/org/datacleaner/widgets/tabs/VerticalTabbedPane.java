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
package org.datacleaner.widgets.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.ResultWindow;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

/**
 * The vertical tabbed pane used in the {@link ResultWindow} to present the
 * available analyzer results
 */
public class VerticalTabbedPane extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();

    private static final Color COLOR_SELECTED_FOREGROUND = WidgetUtils.BG_COLOR_BLUE_DARK;
    private static final Color COLOR_SELECTED_BACKGROUND = WidgetUtils.BG_COLOR_LESS_BRIGHT;
    private static final Border BORDER_TABS = new CompoundBorder(WidgetUtils.BORDER_LIST_ITEM_SUBTLE, new EmptyBorder(10, 4, 10, 4));

    private final List<VerticalTab<?>> _tabs;
    private final DCPanel _leftPanel;
    private JComponent _currentContent;
    private DCBannerPanel _banner;

    public VerticalTabbedPane() {
        super();

        _tabs = new ArrayList<>();
        _leftPanel = new DCPanel();
        _leftPanel.setLayout(new VerticalLayout(0));

        setLayout(new BorderLayout());
        add(wrapLeftPanel(_leftPanel), BorderLayout.WEST);
    }

    private JComponent wrapLeftPanel(final DCPanel panel) {
        final JScrollPane scroll = WidgetUtils.scrolleable(panel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        final JXCollapsiblePane collapsiblePane = new JXCollapsiblePane(JXCollapsiblePane.Direction.LEFT);
        collapsiblePane.getContentPane().setBackground(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        collapsiblePane.add(scroll);
        collapsiblePane.setAnimated(false);

        final JButton toggleTabViewButton = new JButton(imageManager.getImageIcon("images/widgets/vertical-tabs-collapse.png"));
        toggleTabViewButton.setBorder(null);
        toggleTabViewButton.setOpaque(false);
        toggleTabViewButton.setContentAreaFilled(false);
        toggleTabViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean collapsed = collapsiblePane.isCollapsed();
                if (collapsed) {
                    toggleTabViewButton.setIcon(imageManager.getImageIcon("images/widgets/vertical-tabs-collapse.png"));
                    toggleTabViewButton.setBorder(null);
                } else {
                    toggleTabViewButton.setIcon(imageManager.getImageIcon("images/widgets/vertical-tabs-expand.png"));
                    toggleTabViewButton.setBorder(new EmptyBorder(0, 2, 0, 0));
                }
                collapsiblePane.setCollapsed(!collapsed);
            }
        });

        final DCPanel collapseButtonPanel = new DCPanel();
        collapseButtonPanel.setOpaque(true);
        collapseButtonPanel.setBackground(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        collapseButtonPanel.setLayout(new VerticalLayout(4));
        collapseButtonPanel.setBorder(null);
        collapseButtonPanel.add(toggleTabViewButton);

        final DCPanel wrapperPanel = new DCPanel();
        wrapperPanel.setLayout(new BorderLayout());
        wrapperPanel.add(collapsiblePane, BorderLayout.CENTER);
        wrapperPanel.add(collapseButtonPanel, BorderLayout.EAST);
        return wrapperPanel;
    }

    public int getTabCount() {
        return _tabs.size();
    }

    public int getSelectedIndex() {
        int i = 0;
        for (VerticalTab<?> tab : _tabs) {
            final JButton button = tab.getButton();
            if (button.getForeground() == COLOR_SELECTED_FOREGROUND && button.getBackground() == COLOR_SELECTED_BACKGROUND) {
                return i;
            }
            i++;
        }

        return -1;
    }

    public void setSelectedIndex(int index) {
        // reset other components
        for (VerticalTab<?> tab : _tabs) {
            final JButton button = tab.getButton();
            button.setForeground(null);
            button.setBackground(null);
        }

        if (_currentContent != null) {
            remove(_currentContent);
        }

        final VerticalTab<?> tab = _tabs.get(index);

        // styling of button
        final JButton button = tab.getButton();
        button.setBackground(COLOR_SELECTED_BACKGROUND);
        button.setForeground(COLOR_SELECTED_FOREGROUND);

        // set component as content
        final JComponent panel = tab.getContents();

        final JScrollPane scroll = WidgetUtils.scrolleable(panel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
        _currentContent = scroll;

        // update banner if necesary
        if (_banner != null) {
            _banner.setTitle2(button.getText());
            _banner.updateUI();
        }

        updateUI();
    }

    public <C extends JComponent> Tab<C> addTab(String title, Icon icon, C component) {
        final int index = _tabs.size();

        final JButton button = WidgetFactory.createDefaultButton(title, icon);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BORDER_TABS);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSelectedIndex(index);
            }
        });
        _leftPanel.add(button);
        final VerticalTab<C> tab = new VerticalTab<C>(button, component);
        _tabs.add(tab);

        if (index == 0) {
            // the first tab automatically gets selected
            button.doClick();
        }

        return tab;
    }

    public <C extends JComponent> void updateTabTitle(String title, Tab<C> tab) {
        int index = _tabs.indexOf(tab);
        ((JButton) _leftPanel.getComponent(index)).setText(title);
    }

    public void bindTabTitleToBanner(DCBannerPanel banner) {
        _banner = banner;
        final int selectedIndex = getSelectedIndex();
        if (selectedIndex != -1) {
            // refresh the selection and thus the banner
            setSelectedIndex(selectedIndex);
        }
    }

}
