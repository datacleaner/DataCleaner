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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.metamodel.schema.Table;
import org.datacleaner.actions.AnalyzeButtonActionListener;
import org.datacleaner.actions.DisplayOutputWritersAction;
import org.datacleaner.actions.PreviewSourceDataActionListener;
import org.datacleaner.actions.PreviewTransformedDataActionListener;
import org.datacleaner.actions.RemoveComponentMenuItem;
import org.datacleaner.actions.RemoveSourceTableMenuItem;
import org.datacleaner.actions.RenameComponentMenuItem;
import org.datacleaner.actions.TransformButtonActionListener;
import org.datacleaner.api.Renderer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.metadata.HasMetadataProperties;
import org.datacleaner.panels.ComponentBuilderPresenter;
import org.datacleaner.panels.ComponentBuilderPresenterRenderingFormat;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.widgets.ChangeRequirementMenu;
import org.datacleaner.widgets.DescriptorMenuBuilder;
import org.datacleaner.windows.ComponentConfigurationDialog;
import org.datacleaner.windows.SourceTableConfigurationDialog;
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

    private final Map<ComponentBuilder, ComponentConfigurationDialog> _configurationDialogs;
    private final JobGraphContext _graphContext;
    private final JobGraphLinkPainter _linkPainter;
    private final RendererFactory _presenterRendererFactory;
    private final WindowContext _windowContext;
    private final UsageLogger _usageLogger;

    // this is ugly, but a hack to make the graph mouse listener and the
    // regular mouse listener aware of each other's actions.
    private boolean _clickCaught = false;

    public JobGraphMouseListener(JobGraphContext graphContext, JobGraphLinkPainter linkPainter,
            RendererFactory presenterRendererFactory, WindowContext windowContext, UsageLogger usageLogger,
            Map<ComponentBuilder, ComponentConfigurationDialog> configurationDialogs) {
        _graphContext = graphContext;
        _linkPainter = linkPainter;
        _presenterRendererFactory = presenterRendererFactory;
        _windowContext = windowContext;
        _usageLogger = usageLogger;
        _configurationDialogs = configurationDialogs;
    }

    /**
     * Invoked when a component is double-clicked
     * 
     * @param componentBuilder
     * @param me
     */
    public void onComponentDoubleClicked(ComponentBuilder componentBuilder, MouseEvent me) {
        showConfigurationDialog(componentBuilder);
    }

    private void showConfigurationDialog(final ComponentBuilder componentBuilder) {
        final ComponentConfigurationDialog existingDialog = _configurationDialogs.get(componentBuilder);
        if (existingDialog != null) {
            existingDialog.toFront();
            return;
        }

        @SuppressWarnings("unchecked")
        final Renderer<ComponentBuilder, ? extends ComponentBuilderPresenter> renderer = (Renderer<ComponentBuilder, ? extends ComponentBuilderPresenter>) _presenterRendererFactory
                .getRenderer(componentBuilder, ComponentBuilderPresenterRenderingFormat.class);

        if (renderer != null) {
            final ComponentBuilderPresenter presenter = renderer.render(componentBuilder);

            final ComponentConfigurationDialog dialog = new ComponentConfigurationDialog(componentBuilder,
                    _graphContext.getAnalysisJobBuilder(), presenter);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    _configurationDialogs.remove(componentBuilder);
                }
            });
            _configurationDialogs.put(componentBuilder, dialog);
            dialog.open();
        }
    }

    /**
     * Invoked when a {@link Table} is double clicked
     * 
     * @param table
     * @param me
     */
    public void onTableDoubleClicked(Table table, MouseEvent me) {
        SourceTableConfigurationDialog dialog = new SourceTableConfigurationDialog(_windowContext,
                _graphContext.getAnalysisJobBuilder(), table);
        dialog.open();
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
                showConfigurationDialog(componentBuilder);
            }
        });
        popup.add(configureComponentMenuItem);

        if (componentBuilder instanceof InputColumnSourceJob || componentBuilder instanceof HasFilterOutcomes) {
            popup.add(createLinkMenuItem(componentBuilder));
        }

        popup.add(new RenameComponentMenuItem(componentBuilder));

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

        final ImageManager imageManager = ImageManager.get();
        final AnalysisJobBuilder analysisJobBuilder = _graphContext.getAnalysisJobBuilder();
        final AnalyzerBeansConfiguration configuration = analysisJobBuilder.getConfiguration();
        final Point point = me.getPoint();

        final JMenu transformMenuItem = new JMenu("Transform");
        transformMenuItem
                .setIcon(imageManager.getImageIcon(IconUtils.TRANSFORMER_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
        {
            final TransformButtonActionListener transformButtonHelper = new TransformButtonActionListener(
                    configuration, analysisJobBuilder, _usageLogger);
            final Collection<? extends ComponentDescriptor<?>> descriptors = configuration.getDescriptorProvider()
                    .getTransformerDescriptors();
            final DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors) {
                @Override
                protected JMenuItem createMenuItem(ComponentDescriptor<?> descriptor) {
                    final JMenuItem menuItem = transformButtonHelper.createMenuItem(descriptor, point);
                    return menuItem;
                }
            };
            descriptorMenuBuilder.addItemsToMenu(transformMenuItem);
        }

        final JMenu filterMenuItem = new JMenu("Filter");
        filterMenuItem.setIcon(imageManager.getImageIcon(IconUtils.FILTER_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
        {
            final TransformButtonActionListener transformButtonHelper = new TransformButtonActionListener(
                    configuration, analysisJobBuilder, _usageLogger);
            final Collection<? extends ComponentDescriptor<?>> descriptors = configuration.getDescriptorProvider()
                    .getFilterDescriptors();
            final DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors, false) {
                @Override
                protected JMenuItem createMenuItem(ComponentDescriptor<?> descriptor) {
                    final JMenuItem menuItem = transformButtonHelper.createMenuItem(descriptor, point);
                    return menuItem;
                }
            };
            descriptorMenuBuilder.addItemsToMenu(filterMenuItem);
        }

        final JMenu analyzeMenuItem = new JMenu("Analyze");
        analyzeMenuItem.setIcon(imageManager.getImageIcon(IconUtils.ANALYZER_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
        {
            final AnalyzeButtonActionListener analyzeButtonHelper = new AnalyzeButtonActionListener(configuration,
                    analysisJobBuilder, _usageLogger);
            final Collection<? extends ComponentDescriptor<?>> descriptors = analyzeButtonHelper.getDescriptors();
            final DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors) {
                @Override
                protected JMenuItem createMenuItem(ComponentDescriptor<?> descriptor) {
                    final JMenuItem menuItem = analyzeButtonHelper.createMenuItem(descriptor, point);
                    return menuItem;
                }
            };
            descriptorMenuBuilder.addItemsToMenu(analyzeMenuItem);
        }

        final JMenu writeMenuItem = new JMenu("Write");
        writeMenuItem.setIcon(imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH,
                IconUtils.ICON_SIZE_SMALL));
        {
            final DisplayOutputWritersAction writeButtonHelper = new DisplayOutputWritersAction(analysisJobBuilder);
            final List<JMenuItem> menuItems = writeButtonHelper.createMenuItems();
            for (JMenuItem menuItem : menuItems) {
                writeMenuItem.add(menuItem);
            }
        }

        final JPopupMenu popup = new JPopupMenu();
        popup.add(transformMenuItem);
        popup.add(filterMenuItem);
        popup.add(analyzeMenuItem);
        popup.add(writeMenuItem);
        popup.show(_graphContext.getVisualizationViewer(), me.getX(), me.getY());
    }

    @Override
    public void graphReleased(Object v, MouseEvent me) {
        final PickedState<Object> pickedVertexState = _graphContext.getVisualizationViewer().getPickedVertexState();

        final Object[] selectedObjects = pickedVertexState.getSelectedObjects();

        final AbstractLayout<Object, JobGraphLink> graphLayout = _graphContext.getGraphLayout();

        // update the coordinates metadata of the moved objects.

        for (final Object vertex : selectedObjects) {
            final Double x = graphLayout.getX(vertex);
            final Double y = graphLayout.getY(vertex);
            if (vertex instanceof HasMetadataProperties) {
                final Map<String, String> metadataProperties = ((HasMetadataProperties) vertex).getMetadataProperties();
                metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_X, "" + x.intValue());
                metadataProperties.put(JobGraphMetadata.METADATA_PROPERTY_COORDINATES_Y, "" + y.intValue());
            } else if (vertex instanceof Table) {
                JobGraphMetadata.setPointForTable(_graphContext.getAnalysisJobBuilder(), (Table) vertex, x, y);
            }
        }
    }

    @Override
    public void graphPressed(Object v, MouseEvent me) {
    }

    @Override
    public void graphClicked(Object v, MouseEvent me) {
        logger.debug("graphClicked({}, {})", v, me);
        _clickCaught = false;
        final int button = me.getButton();
        if (v instanceof ComponentBuilder) {
            final ComponentBuilder componentBuilder = (ComponentBuilder) v;
            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                _clickCaught = true;
                onComponentRightClicked(componentBuilder, me);
            } else if (me.getClickCount() == 2) {
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
            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                _clickCaught = true;
                onTableRightClicked(table, me);
            } else if (me.getClickCount() == 2) {
                _clickCaught = true;
                onTableDoubleClicked(table, me);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        logger.debug("mouseClicked({}) (clickCaught={})", me, _clickCaught);
        if (!_clickCaught) {
            int button = me.getButton();
            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                onCanvasRightClicked(me);
            }
        }
        // reset the variable for next time
        _clickCaught = false;
    }
}
