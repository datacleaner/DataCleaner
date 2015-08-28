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
package org.datacleaner.widgets.visualization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.ChangeRequirementMenuBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A button that displays the {@link AnalysisJobBuilder} of a particular
 * component that is being built.
 */
public class ComponentScopeButton extends JButton implements ActionListener {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ComponentScopeButton.class);
    private static final ImageManager imageManager = ImageManager.get();
    private static final Icon scopeIcon = imageManager.getImageIcon(IconUtils.OUTPUT_DATA_STREAM_PATH,
            IconUtils.ICON_SIZE_MEDIUM);

    private final ComponentBuilder _componentBuilder;
    private final AnalysisJobBuilder _topLevelJobBuilder;
    private final ComponentScopeMenuBuilder _menuBuilder;

    public ComponentScopeButton(final ComponentBuilder componentBuilder,
            final ComponentScopeMenuBuilder menuBuilder) {
        super(ChangeRequirementMenuBuilder.NO_REQUIREMENT_TEXT, scopeIcon);
        _componentBuilder = componentBuilder;
        _menuBuilder = menuBuilder;
        _topLevelJobBuilder = componentBuilder.getAnalysisJobBuilder().getRootJobBuilder();
        addActionListener(this);
        updateText(_componentBuilder.getAnalysisJobBuilder(), _menuBuilder.findComponentBuilder(_componentBuilder.getAnalysisJobBuilder()));
        WidgetUtils.setDefaultButtonStyle(this);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        final JPopupMenu popup = new JPopupMenu();

        final List<JMenuItem> menuItems = _menuBuilder.createMenuItems();
        for (JMenuItem menuItem : menuItems) {
            popup.add(menuItem);
        }

        popup.show(this, 0, getHeight());
    }

    public void updateText(final AnalysisJobBuilder osJobBuilder, final ComponentBuilder osComponentBuilder) {
        logger.debug("updateText()");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (osJobBuilder == _topLevelJobBuilder) {
                    setText(ComponentScopeMenuBuilder.DEFAULT_SCOPE_TEXT);
                } else {
                    setText(LabelUtils.getLabel(osComponentBuilder) + ": " + osJobBuilder.getDatastore().getName());
                }
            }
        };
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeAndWait(runnable);
            }
        } catch (Exception e) {
            logger.error("Failed to update ComponentScopeButton", e);
        }
    }

    @Override
    public String toString() {
        return "ComponentScopeMenuBuilder[componentBuilder=" + LabelUtils.getLabel(_componentBuilder) + "]";
    }

    /**
     * Determines if changing scope is relevant or not. If only the top-level scope exist, it is
     * not relevant to even show the ability to set scope.
     *
     */
    public boolean isRelevant() {
        return _menuBuilder.getComponentBuildersWithOutputDataStreams(_topLevelJobBuilder).size() > 0;
    }
}
