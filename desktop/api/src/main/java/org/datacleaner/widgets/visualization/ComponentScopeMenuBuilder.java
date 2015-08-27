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
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.datacleaner.api.OutputDataStream;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
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
    private final AnalysisJobBuilder _rootJobBuilder;

    public ComponentScopeMenuBuilder(ComponentBuilder componentBuilder) {
        _componentBuilder = componentBuilder;
        _rootJobBuilder = _componentBuilder.getAnalysisJobBuilder().getRootJobBuilder();
    }

    public List<ComponentBuilder> getComponentBuildersWithOutputDataStreams(AnalysisJobBuilder jobBuilder) {
        List<ComponentBuilder> descendants = new ArrayList<>();
        for (ComponentBuilder child : jobBuilder.getComponentBuilders()) {
            if (child != _componentBuilder && child.getOutputDataStreams().size() > 0) {
                descendants.add(child);
                for (OutputDataStream outputDataStream : child.getOutputDataStreams()) {
                    descendants.addAll(getComponentBuildersWithOutputDataStreams(child.getOutputDataStreamJobBuilder(outputDataStream)));
                }
            }
        }

        return descendants;
    }

    /**
     * Will find the {@link ComponentBuilder} that has a certain {@link AnalysisJobBuilder}. Since this method.
     *
     * @param analysisJobBuilder The job builder in
     * @return
     */
    public ComponentBuilder findComponentBuilder(AnalysisJobBuilder analysisJobBuilder) {
        if (analysisJobBuilder == _rootJobBuilder) {
            return null;
        }
        for (ComponentBuilder osComponenBuilder : getComponentBuildersWithOutputDataStreams(_rootJobBuilder)) {
            for (OutputDataStream outputDataStream : osComponenBuilder.getOutputDataStreams()) {
                AnalysisJobBuilder osJobBuilder = osComponenBuilder.getOutputDataStreamJobBuilder(outputDataStream);
                if (osJobBuilder == analysisJobBuilder) {
                    return osComponenBuilder;
                }
            }
        }

        throw new IllegalArgumentException("No builder holding that osJobBuilder");
    }

    public List<JMenuItem> createMenuItems() {
        final ComponentRequirement currentComponentRequirement = _componentBuilder.getComponentRequirement();
        logger.info("Current requirement: {}", currentComponentRequirement);

        final List<JMenuItem> popup = new ArrayList<>();
        final JMenuItem rootMenuItem = new JMenuItem(DEFAULT_SCOPE_TEXT);
        rootMenuItem.setToolTipText("Use the default scope for this component");
        rootMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onScopeChangeStart();
                _rootJobBuilder.moveComponent(_componentBuilder);
                _componentBuilder.setComponentRequirement(null);
                onScopeChangeComplete(_rootJobBuilder, null);
            }
        });

        if (_rootJobBuilder == _componentBuilder.getAnalysisJobBuilder()) {
            rootMenuItem.setIcon(selectedScopeIcon);
        }

        popup.add(rootMenuItem);

        final List<ComponentBuilder> osComponentBuilders = getComponentBuildersWithOutputDataStreams(_rootJobBuilder);

        for (final ComponentBuilder osComponentBuilder : osComponentBuilders) {
            final JMenu componentMenu = new JMenu(LabelUtils.getLabel(osComponentBuilder));

            for (final OutputDataStream outputDataStream : osComponentBuilder.getOutputDataStreams()) {
                final AnalysisJobBuilder osJobBuilder =
                        osComponentBuilder.getOutputDataStreamJobBuilder(outputDataStream);

                final JMenuItem scopeMenuItem = new JMenuItem(osJobBuilder.getDatastore().getName());

                if (osJobBuilder == _componentBuilder.getAnalysisJobBuilder()) {
                    componentMenu.setIcon(selectedScopeIcon);
                    scopeMenuItem.setIcon(selectedScopeIcon);
                }

                scopeMenuItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onScopeChangeStart();
                        osJobBuilder.moveComponent(_componentBuilder);
                        _componentBuilder.setComponentRequirement(null);
                        onScopeChangeComplete(osJobBuilder, osComponentBuilder);
                    }
                });
                componentMenu.add(scopeMenuItem);
            }
            popup.add(componentMenu);
        }

        return popup;
    }

    protected void onScopeChangeStart() {
    }

    protected void onScopeChangeComplete(final AnalysisJobBuilder analysisJobBuilder,
            final ComponentBuilder componentBuilder) {
    }

}
