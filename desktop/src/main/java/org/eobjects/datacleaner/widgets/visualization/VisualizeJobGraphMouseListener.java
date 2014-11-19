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
package org.eobjects.datacleaner.widgets.visualization;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.metamodel.schema.Table;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.metadata.HasMetadataProperties;
import org.eobjects.analyzer.result.renderer.Renderable;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.datacleaner.actions.AnalyzeButtonActionListener;
import org.eobjects.datacleaner.actions.RemoveComponentMenuItem;
import org.eobjects.datacleaner.actions.RemoveSourceTableMenuItem;
import org.eobjects.datacleaner.actions.RenameComponentMenuItem;
import org.eobjects.datacleaner.actions.TransformButtonActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.ComponentJobBuilderPresenter;
import org.eobjects.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.ChangeRequirementMenu;
import org.eobjects.datacleaner.widgets.DescriptorMenuBuilder;
import org.eobjects.datacleaner.windows.ComponentConfigurationDialog;
import org.eobjects.datacleaner.windows.SourceTableConfigurationDialog;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Listener for mouse events on the {@link VisualizeJobGraph}.
 * 
 * Note that this class implements two interfaces and is thus added as two
 * distinct listener types: {@link MouseListener} and {@link GraphMouseListener}
 */
public class VisualizeJobGraphMouseListener extends MouseAdapter implements GraphMouseListener<Object> {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final VisualizationViewer<Object, VisualizeJobLink> _visualizationViewer;
    private final RendererFactory _presenterRendererFactory;
    private final WindowContext _windowContext;
    private final UsageLogger _usageLogger;

    // this is ugly, but a hack to make the graph mouse listener and the
    // regular mouse listener aware of each other's actions.
    private final AtomicBoolean clickCaught = new AtomicBoolean(false);

    public VisualizeJobGraphMouseListener(AnalysisJobBuilder analysisJobBuilder,
            VisualizationViewer<Object, VisualizeJobLink> visualizationViewer,
            RendererFactory presenterRendererFactory, WindowContext windowContext, UsageLogger usageLogger) {
        _analysisJobBuilder = analysisJobBuilder;
        _visualizationViewer = visualizationViewer;
        _presenterRendererFactory = presenterRendererFactory;
        _windowContext = windowContext;
        _usageLogger = usageLogger;
    }

    /**
     * Invoked when a component is double-clicked
     * 
     * @param componentBuilder
     * @param me
     */
    public void onComponentDoubleClicked(AbstractBeanJobBuilder<?, ?, ?> componentBuilder, MouseEvent me) {
        @SuppressWarnings("unchecked")
        final Renderer<Renderable, ? extends ComponentJobBuilderPresenter> renderer = (Renderer<Renderable, ? extends ComponentJobBuilderPresenter>) _presenterRendererFactory
                .getRenderer(componentBuilder, ComponentJobBuilderRenderingFormat.class);
        if (renderer != null) {
            final ComponentJobBuilderPresenter presenter = renderer.render(componentBuilder);

            final ComponentConfigurationDialog dialog = new ComponentConfigurationDialog(componentBuilder,
                    _analysisJobBuilder, presenter);
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
        SourceTableConfigurationDialog dialog = new SourceTableConfigurationDialog(_windowContext, _analysisJobBuilder,
                table);
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
        popup.add(new RemoveSourceTableMenuItem(_analysisJobBuilder, table));
        popup.show(_visualizationViewer, me.getX(), me.getY());
    }

    /**
     * Invoked when a component is right-clicked
     * 
     * @param componentBuilder
     * @param me
     */
    public void onComponentRightClicked(AbstractBeanJobBuilder<?, ?, ?> componentBuilder, MouseEvent me) {
        final JPopupMenu popup = new JPopupMenu();
        popup.add(new RenameComponentMenuItem(componentBuilder));
        popup.add(new ChangeRequirementMenu(componentBuilder));
        popup.add(new RemoveComponentMenuItem(_analysisJobBuilder, componentBuilder));
        popup.show(_visualizationViewer, me.getX(), me.getY());
    }

    /**
     * Invoked when the canvas is right-clicked
     * 
     * @param me
     */
    public void onCanvasRightClicked(MouseEvent me) {

        final ImageManager imageManager = ImageManager.get();

        final JMenu transformMenuItem = new JMenu("Transform");
        transformMenuItem
                .setIcon(imageManager.getImageIcon(IconUtils.TRANSFORMER_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
        {
            final TransformButtonActionListener transformButtonHelper = new TransformButtonActionListener(
                    _analysisJobBuilder.getConfiguration(), _analysisJobBuilder, _usageLogger);
            final List<BeanDescriptor<?>> descriptors = transformButtonHelper.getDescriptors();
            final DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors) {
                @Override
                protected JMenuItem createMenuItem(BeanDescriptor<?> descriptor) {
                    final JMenuItem menuItem = transformButtonHelper.createMenuItem(descriptor);
                    return menuItem;
                }
            };
            descriptorMenuBuilder.addItemsToMenu(transformMenuItem);
        }

        final JMenu analyzeMenuItem = new JMenu("Analyze");
        analyzeMenuItem.setIcon(imageManager.getImageIcon(IconUtils.ANALYZER_IMAGEPATH, IconUtils.ICON_SIZE_SMALL));
        {
            final AnalyzeButtonActionListener analyzeButtonHelper = new AnalyzeButtonActionListener(
                    _analysisJobBuilder.getConfiguration(), _analysisJobBuilder, _usageLogger);
            final Collection<? extends BeanDescriptor<?>> descriptors = analyzeButtonHelper.getDescriptors();
            final DescriptorMenuBuilder descriptorMenuBuilder = new DescriptorMenuBuilder(descriptors) {
                @Override
                protected JMenuItem createMenuItem(BeanDescriptor<?> descriptor) {
                    final JMenuItem menuItem = analyzeButtonHelper.createMenuItem(descriptor);
                    return menuItem;
                }
            };
            descriptorMenuBuilder.addItemsToMenu(analyzeMenuItem);
        }

        final JMenu writeMenuItem = new JMenu("Write");
        writeMenuItem.setIcon(imageManager.getImageIcon(IconUtils.GENERIC_DATASTORE_IMAGEPATH,
                IconUtils.ICON_SIZE_SMALL));
        // TODO
        writeMenuItem.add(new JMenuItem("TODO"));

        final JPopupMenu popup = new JPopupMenu();
        popup.add(transformMenuItem);
        popup.add(analyzeMenuItem);
        popup.add(writeMenuItem);
        popup.show(_visualizationViewer, me.getX(), me.getY());
    }

    @Override
    public void graphReleased(Object v, MouseEvent me) {
        final PickedState<Object> pickedVertexState = _visualizationViewer.getPickedVertexState();

        final Object[] selectedObjects = pickedVertexState.getSelectedObjects();

        final AbstractLayout<Object, VisualizeJobLink> graphLayout = getGraphLayout();

        // update the coordinates metadata of the moved objects.

        for (final Object vertex : selectedObjects) {
            final Double x = graphLayout.getX(vertex);
            final Double y = graphLayout.getY(vertex);
            if (vertex instanceof HasMetadataProperties) {
                final Map<String, String> metadataProperties = ((HasMetadataProperties) vertex).getMetadataProperties();
                metadataProperties.put(VisualizationMetadata.METADATA_PROPERTY_COORDINATES_X, "" + x.intValue());
                metadataProperties.put(VisualizationMetadata.METADATA_PROPERTY_COORDINATES_Y, "" + y.intValue());
            } else if (vertex instanceof Table) {
                VisualizationMetadata.setPointForTable(_analysisJobBuilder, (Table) vertex, x, y);
            }
        }
    }

    private AbstractLayout<Object, VisualizeJobLink> getGraphLayout() {
        Layout<Object, VisualizeJobLink> layout = _visualizationViewer.getGraphLayout();
        while (layout instanceof LayoutDecorator) {
            layout = ((LayoutDecorator<Object, VisualizeJobLink>) layout).getDelegate();
        }
        return (AbstractLayout<Object, VisualizeJobLink>) layout;
    }

    @Override
    public void graphPressed(Object v, MouseEvent me) {
    }

    @Override
    public void graphClicked(Object v, MouseEvent me) {
        clickCaught.set(false);
        final int button = me.getButton();
        if (v instanceof AbstractBeanJobBuilder) {
            final AbstractBeanJobBuilder<?, ?, ?> componentBuilder = (AbstractBeanJobBuilder<?, ?, ?>) v;
            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                clickCaught.set(true);
                onComponentRightClicked(componentBuilder, me);
            } else if (me.getClickCount() == 2) {
                clickCaught.set(true);
                onComponentDoubleClicked(componentBuilder, me);
            }
        } else if (v instanceof Table) {
            final Table table = (Table) v;
            if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
                clickCaught.set(true);
                onTableRightClicked(table, me);
            } else if (me.getClickCount() == 2) {
                clickCaught.set(true);
                onTableDoubleClicked(table, me);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        int button = me.getButton();
        if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
            if (!clickCaught.get()) {
                onCanvasRightClicked(me);
            }
        }
    }
}
