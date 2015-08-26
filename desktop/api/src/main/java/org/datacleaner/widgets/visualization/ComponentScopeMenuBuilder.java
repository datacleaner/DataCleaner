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
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object capable of building a menu for changing a component's
 * {@link ComponentRequirement}.
 */
public class ComponentScopeMenuBuilder {

    public static final String DEFAULT_SCOPE_TEXT = "Default scope";
    private static final Logger logger = LoggerFactory.getLogger(ComponentScopeMenuBuilder.class);
    private static final ImageManager imageManager = ImageManager.get();

    private static final Icon selectedScopeIcon = imageManager.getImageIcon(IconUtils.STATUS_VALID,
            IconUtils.ICON_SIZE_SMALL);

    private final ComponentBuilder _componentBuilder;
    private final AnalysisJobBuilder _topLevelJobBuilder;

    public ComponentScopeMenuBuilder(ComponentBuilder componentBuilder) {
        _componentBuilder = componentBuilder;
        _topLevelJobBuilder = _componentBuilder.getAnalysisJobBuilder().getTopLevelJobBuilder();
    }

    public List<JMenuItem> createMenuItems() {
        final ComponentRequirement currentComponentRequirement = _componentBuilder.getComponentRequirement();
        logger.info("Current requirement: {}", currentComponentRequirement);

        final List<JMenuItem> popup = new ArrayList<>();
        final JMenuItem topLevelMenuItem = new JMenuItem(DEFAULT_SCOPE_TEXT);
        topLevelMenuItem
                .setToolTipText("Use the top level scope for this component");
        topLevelMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onScopeChangeStart();
                _topLevelJobBuilder.moveComponent(_componentBuilder);
                _componentBuilder.setComponentRequirement(null);
                onScopeChangeComplete();
            }
        });

        if(_topLevelJobBuilder == _componentBuilder.getAnalysisJobBuilder()){
            topLevelMenuItem.setIcon(selectedScopeIcon);
        }

        popup.add(topLevelMenuItem);

        final List<AnalysisJobBuilder> allJobBuilders = _topLevelJobBuilder.getDescendants();

        for (final AnalysisJobBuilder ajb : allJobBuilders) {
            final JMenuItem scopeMenuItem = new JMenuItem(ajb.getDatastore().getName());

            if(ajb == _componentBuilder.getAnalysisJobBuilder()){
                scopeMenuItem.setIcon(selectedScopeIcon);
            }

            scopeMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onScopeChangeStart();
                    ajb.moveComponent(_componentBuilder);
                    _componentBuilder.setComponentRequirement(null);
                    onScopeChangeComplete();
                }
            });
            popup.add(scopeMenuItem);
        }

        return popup;
    }

    protected void onScopeChangeStart() {
    }

    protected void onScopeChangeComplete() {
    }

}
