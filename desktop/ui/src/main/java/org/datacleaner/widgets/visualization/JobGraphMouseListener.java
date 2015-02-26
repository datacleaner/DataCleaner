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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.metamodel.schema.Table;
import org.datacleaner.actions.PreviewSourceDataActionListener;
import org.datacleaner.actions.PreviewTransformedDataActionListener;
import org.datacleaner.actions.RemoveComponentMenuItem;
import org.datacleaner.actions.RemoveSourceTableMenuItem;
import org.datacleaner.actions.RenameComponentActionListener;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.metadata.HasMetadataProperties;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.ChangeRequirementMenu;
import org.datacleaner.widgets.DescriptorMenuBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Listener for mouse events on the {@link JobGraph}.
 * 
 * Note that this class implements two interfaces and is thus added as two
 * distinct listener types: {@link MouseListener} and {@link GraphMouseListener}
 */
public class JobGraphMouseListener extends MouseAdapter implements GraphMouseListener<Object> {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphMouseListener.class);

    private final JobGraphContext _graphContext;
    private final JobGraphLinkPainter _linkPainter;
    private final JobGraphActions _actions;
    private final WindowContext _windowContext;
    private final UsageLogger _usageLogger;

    // this is ugly, but a hack to make the graph mouse listener and the
    // regular mouse listener aware of each other's actions.
    private boolean _clickCaught = false;

    private Point _pressedPoint;

    public JobGraphMouseListener(JobGraphContext graphContext, JobGraphLinkPainter linkPainter,
            JobGraphActions actions, WindowContext windowContext, UsageLogger usageLogger) {
        _graphContext = graphContext;
        _linkPainter = linkPainter;
        _actions = actions;
        _windowContext = windowContext;
        _usageLogger = usageLogger;
    }

    /**
     * Invoked when a component is double-clicked
     * 
     * @param componentBuilder
     * @param me
     */
    public void onComponentDoubleClicked(ComponentBuilder componentBuilder, MouseEvent me) {
        _actions.showConfigurationDialog(componentBuilder);
    }

    /**
     * Invoked when a {@link Table} is double clicked
     * 
     * @param table
     * @param me
     */
    public void onTableDoubleClicked(final Table table, MouseEvent me) {
        _actions.showTableConfigurationDialog(table);
    }

    /**
     * Invoked when a {@link Table} is right-clicked
     * 
     * @param table
     * @param me
     */
    public void onTableRightClicked(Table table, MouseEvent me) {
        final JPopupMenu popup = new JPopupMenu();

        popup.add(createLinkMenuItem(table));

        final JMenuItem previewMenuItem = new JMenuItem("Preview data", ImageManager.get().getImageIcon(
                IconUtils.ACTION_PREVIEW, IconUtils.ICON_SIZE_SMALL));
        final AnalysisJobBuilder analysisJobBuilder = _graphContext.getAnalysisJobBuilder();
        final Datastore datastore = analysisJobBuilder.getDatastore();
        final List<MetaModelInputColumn> inputColumns = analysisJobBuilder.getSourceColumnsOfTable(table);
        previewMenuItem.addActionListener(new PreviewSourceDataActionListener(_windowContext, datastore, inputColumns));
        popup.add(previewMenuItem);
        popup.addSeparator();
        popup.add(new RemoveSourceTableMenuItem(analysisJobBuilder, table));
        popup.show(_graphContext.getVisualizationViewer(), me.getX(), me.getY());
    }

    /**
     * Invoked when a component is right-clicked
     * 
     * @param componentBuilder
     * @param me
     */
    public void onComponentRightClicked(final ComponentBuilder componentBuilder, final MouseEvent me) {
        final JPopupMenu popup = new JPopupMenu();

        final JMenuItem configureComponentMenuItem = new JMenuItem("Configure ...", ImageManager.get().getImageIcon(
                IconUtils.MENU_OPTIONS, IconUtils.ICON_SIZE_SMALL));
        configureComponentMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _actions.showConfigurationDialog(componentBuilder);
            }
        });
        popup.add(configureComponentMenuItem);

        if (componentBuilder instanceof InputColumnSourceJob || componentBuilder instanceof HasFilterOutcomes) {
            popup.add(createLinkMenuItem(componentBuilder));
        }

        final Icon renameIcon = ImageManager.get().getImageIcon(IconUtils.ACTION_RENAME, IconUtils.ICON_SIZE_SMALL);
        final JMenuItem renameMenuItem = WidgetFactory.createMenuItem("Rename component", renameIcon);
        renameMenuItem.addActionListener(new RenameComponentActionListener(componentBuilder) {

            @Override
            protected void onNameChanged() {
                _graphContext.getJobGraph().refresh();
            }
        });
        popup.add(renameMenuItem);

        if (componentBuilder instanceof TransformerComponentBuilder) {
            final TransformerComponentBuilder<?> tjb = (TransformerComponentBuilder<?>) componentBuilder;
            final JMenuItem previewMenuItem = new JMenuItem("Preview data", ImageManager.get().getImageIcon(
                    IconUtils.ACTION_PREVIEW, IconUtils.ICON_SIZE_SMALL));
            previewMenuItem.addActionListener(new PreviewTransformedDataActionListener(_windowContext, tjb));
            previewMenuItem.setEnabled(componentBuilder.isConfigured());
            popup.add(previewMenuItem);
        }

        popup.add(new ChangeRequirementMenu(componentBuilder));
        popup.addSeparator();
        popup.add(new RemoveComponentMenuItem(_graphContext.getAnalysisJobBuilder(), componentBuilder));
        popup.show(_graphContext.getVisualizationViewer(), me.getX(), me.getY());
    }

    private JMenuItem createLinkMenuItem(final Object from) {
        final ImageManager imageManager = ImageManager.get();
        final JMenuItem menuItem = new JMenuItem("Link to ...", imageManager.getImageIcon(IconUtils.ACTION_ADD,
                IconUtils.ICON_SIZE_SMALL));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _linkPainter.startLink(from);
            }
        });
        return menuItem;
    }

    /**
     * Invoked when the canvas is right-clicked
     * 
     * @param me
     */
    public void onCanvasRightClicked(final MouseEvent me) {
        _linkPainter.cancelLink();

        final JPopupMenu popup = new JPopupMenu();

        final Point point = me.getPoint();
        final AnalysisJobBuilder analysisJobBuilder = _graphContext.getAnalysisJobBuilder();
        final AnalyzerBeansConfiguration configuration = analysisJobBuilder.getConfiguration();
        final Set<ComponentSuperCategory> superCategories = configuration.getDescriptorProvider()
                .getComponentSuperCategories();
        for (ComponentSuperCategory superCategory : superCategories) {
            final DescriptorMenuBuilder menuBuilder = new DescriptorMenuBuilder(analysisJobBuilder, _usageLogger,
                    superCategory, point);

            final JMenu menu = new JMenu(superCategory.getName());
            menu.setIcon(IconUtils.getComponentSuperCategoryIcon(superCategory, IconUtils.ICON_SIZE_MENU_ITEM));
            menuBuilder.addItemsToMenu(menu);
            popup.add(menu);
        }

        popup.show(_graphContext.getVisualizationViewer(), me.getX(), me.getY());
    }

    @Override
    public void graphReleased(Object v, MouseEvent me) {
        logger.debug("Graph released");

        if (isLeftClick(me)) {
            if (_pressedPoint != null && _pressedPoint.equals(me.getPoint())) {
                // avoid updating any coordinates when nothing has been moved
                return;
            }
            final PickedState<Object> pickedVertexState = _graphContext.getVisualizationViewer().getPickedVertexState();

            final Object[] selectedObjects = pickedVertexState.getSelectedObjects();

            final AbstractLayout<Object, JobGraphLink> graphLayout = _graphContext.getGraphLayout();

            // update the coordinates metadata of the moved objects.

            for (final Object vertex : selectedObjects) {
                final Double x = graphLayout.getX(vertex);
                final Double y = graphLayout.getY(vertex);
                if (vertex instanceof HasMetadataProperties) {
                    final Map<String, String> metadataProperties = ((HasMetadataProperties) vertex)
                            .getMetadataProperties();
                    metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_X, "" + x.intValue());
                    metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_Y, "" + y.intValue());
                } else if (vertex instanceof Table) {
                    JobGraphMetadata.setPointForTable(_graphContext.getAnalysisJobBuilder(), (Table) vertex, x, y);
                }
            }
            if (selectedObjects.length > 0) {
                _graphContext.getJobGraph().refresh();
            }
        } else if (isRightClick(me)) {
            if (v instanceof ComponentBuilder) {
                final ComponentBuilder componentBuilder = (ComponentBuilder) v;
                onComponentRightClicked(componentBuilder, me);
            } else if (v instanceof Table) {
                final Table table = (Table) v;
                onTableRightClicked(table, me);
            }
        }
    }

    @Override
    public void graphPressed(Object v, MouseEvent me) {
        logger.debug("graphPressed({}, {})", v, me);
        _pressedPoint = me.getPoint();
        _clickCaught = false;
        if (isLeftClick(me)) {
            if (v instanceof ComponentBuilder) {
                final ComponentBuilder componentBuilder = (ComponentBuilder) v;
                if (me.getClickCount() == 2) {
                    _clickCaught = true;
                    onComponentDoubleClicked(componentBuilder, me);
                } else {
                    final boolean ended = _linkPainter.endLink(componentBuilder, me);
                    if (ended) {
                        me.consume();
                    }
                }
            } else if (v instanceof Table) {
                final Table table = (Table) v;
                if (me.getClickCount() == 2) {
                    _clickCaught = true;
                    onTableDoubleClicked(table, me);
                }
            }
        } else if (isRightClick(me)) {
            if (v instanceof ComponentBuilder) {
                _clickCaught = true;
            } else if (v instanceof Table) {
                _clickCaught = true;
            }
        }
    }

    @Override
    public void graphClicked(Object v, MouseEvent me) {
        logger.debug("graphClicked({}, {})", v, me);
        //We do nothing. We show the menu only when the mouse is released
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        logger.debug("mouseReleased({}) (clickCaught={})", me, _clickCaught);
        if (!_clickCaught) {
            if (isRightClick(me)) {
                onCanvasRightClicked(me);
            }
        }
        // reset the variable for next time
        _clickCaught = false;
    }
    
    private boolean isLeftClick(MouseEvent me) {
        int button = me.getButton();
        return button == MouseEvent.BUTTON1 && (!me.isMetaDown());
    }

    private boolean isRightClick(MouseEvent me) {
        int button = me.getButton();
        if (button == MouseEvent.BUTTON2) {
            return true;
        }
        if (button == MouseEvent.BUTTON3) {
            return true;
        }
        // Mac OS X Cmd + click to achieve right-click
        if (button == MouseEvent.BUTTON1 && (me.isMetaDown())) {
            return true;
        }
        return false;
    }
}
