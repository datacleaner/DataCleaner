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
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.datacleaner.actions.ComponentBuilderTabTextActionListener;
import org.datacleaner.actions.HideTabTextActionListener;
import org.datacleaner.actions.RenameComponentActionListener;
import org.datacleaner.api.Renderer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.AbstractComponentBuilderPanel;
import org.datacleaner.panels.AnalyzerComponentBuilderPresenter;
import org.datacleaner.panels.ComponentBuilderPresenter;
import org.datacleaner.panels.ComponentBuilderPresenterRenderingFormat;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.FilterComponentBuilderPresenter;
import org.datacleaner.panels.MetadataPanel;
import org.datacleaner.panels.SourceColumnsPanel;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.panels.maxrows.MaxRowsFilterShortcutPanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.user.UsageLogger;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the "Classic view" of datacleaner where each job component is
 * represented with a tab in a {@link CloseableTabbedPane}.
 */
public class JobClassicView extends DCPanel implements TabCloseListener {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();
    private static final Logger logger = LoggerFactory.getLogger(JobClassicView.class);

    private static final int TAB_ICON_SIZE = IconUtils.ICON_SIZE_LARGE;
    private static final int SOURCE_TAB = 0;
    private static final int METADATA_TAB = 1;

    private final Map<AnalyzerComponentBuilder<?>, AnalyzerComponentBuilderPresenter> _analyzerPresenters = new LinkedHashMap<>();
    private final Map<TransformerComponentBuilder<?>, TransformerComponentBuilderPresenter> _transformerPresenters = new LinkedHashMap<>();
    private final Map<FilterComponentBuilder<?, ?>, FilterComponentBuilderPresenter> _filterPresenters = new LinkedHashMap<>();
    private final Map<ComponentBuilderPresenter, JComponent> _jobBuilderTabs = new HashMap<>();
    private final MetadataPanel _metadataPanel;
    private final SourceColumnsPanel _sourceColumnsPanel;
    private final CloseableTabbedPane _tabbedPane;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private final RendererFactory _presenterRendererFactory;

    private volatile AbstractComponentBuilderPanel _latestPanel = null;

    public JobClassicView(WindowContext windowContext, AnalysisJobBuilder analysisJobBuilder,
            RendererFactory presenterRendererFactory, UsageLogger usageLogger) {
        super();

        _analysisJobBuilder = analysisJobBuilder;
        _presenterRendererFactory = presenterRendererFactory;
        _sourceColumnsPanel = new SourceColumnsPanel(analysisJobBuilder, windowContext);
        _metadataPanel = new MetadataPanel(analysisJobBuilder);

        _tabbedPane = new CloseableTabbedPane(false);
        _tabbedPane.addTabCloseListener(this);
        _tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public synchronized void stateChanged(ChangeEvent e) {
                if (_latestPanel != null) {
                    _latestPanel.applyPropertyValues(false);
                }
                Component selectedComponent = _tabbedPane.getSelectedComponent();
                if (selectedComponent instanceof AbstractComponentBuilderPanel) {
                    _latestPanel = (AbstractComponentBuilderPanel) selectedComponent;
                } else {
                    _latestPanel = null;
                }
            }
        });

        // add source tab
        _tabbedPane.addTab("Source", imageManager.getImageIcon(IconUtils.MODEL_SOURCE, TAB_ICON_SIZE),
                WidgetUtils.scrolleable(_sourceColumnsPanel));
        _tabbedPane.setRightClickActionListener(SOURCE_TAB, new HideTabTextActionListener(_tabbedPane, SOURCE_TAB));
        _tabbedPane.setUnclosableTab(SOURCE_TAB);

        // add metadata tab
        _tabbedPane.addTab("Metadata", imageManager.getImageIcon(IconUtils.MODEL_METADATA, TAB_ICON_SIZE),
                _metadataPanel);
        _tabbedPane.setRightClickActionListener(METADATA_TAB, new HideTabTextActionListener(_tabbedPane, METADATA_TAB));
        _tabbedPane.setUnclosableTab(METADATA_TAB);

        // add separator for fixed vs dynamic tabs
        _tabbedPane.addSeparator();

        initializeExistingComponents();

        setLayout(new BorderLayout());
        add(_tabbedPane, BorderLayout.CENTER);
    }

    private void initializeExistingComponents() {
        final List<FilterComponentBuilder<?, ?>> filterJobBuilders = _analysisJobBuilder.getFilterComponentBuilders();
        for (FilterComponentBuilder<?, ?> fjb : filterJobBuilders) {
            initializeFilter(fjb);
        }

        final List<TransformerComponentBuilder<?>> transformerJobBuilders = _analysisJobBuilder
                .getTransformerComponentBuilders();
        for (TransformerComponentBuilder<?> tjb : transformerJobBuilders) {
            initializeTransformer(tjb);
        }

        final List<AnalyzerComponentBuilder<?>> analyzerJobBuilders = _analysisJobBuilder
                .getAnalyzerComponentBuilders();
        for (AnalyzerComponentBuilder<?> ajb : analyzerJobBuilders) {
            initializeAnalyzer((AnalyzerComponentBuilder<?>) ajb);
        }
    }

    public void initializeAnalyzer(final AnalyzerComponentBuilder<?> analyzerJobBuilder) {
        @SuppressWarnings("unchecked")
        final Renderer<AnalyzerComponentBuilder<?>, ? extends ComponentBuilderPresenter> renderer = (Renderer<AnalyzerComponentBuilder<?>, ? extends ComponentBuilderPresenter>) _presenterRendererFactory
                .getRenderer(analyzerJobBuilder, ComponentBuilderPresenterRenderingFormat.class);
        AnalyzerComponentBuilderPresenter presenter = (AnalyzerComponentBuilderPresenter) renderer
                .render(analyzerJobBuilder);

        _analyzerPresenters.put(analyzerJobBuilder, presenter);
        JComponent comp = presenter.createJComponent();
        _tabbedPane.addTab(LabelUtils.getLabel(analyzerJobBuilder),
                IconUtils.getDescriptorIcon(analyzerJobBuilder.getDescriptor(), TAB_ICON_SIZE), comp);
        _jobBuilderTabs.put(presenter, comp);
        final int tabIndex = _tabbedPane.getTabCount() - 1;
        _tabbedPane.setRightClickActionListener(tabIndex, new ComponentBuilderTabTextActionListener(
                _analysisJobBuilder, analyzerJobBuilder, tabIndex, _tabbedPane));
        _tabbedPane.setDoubleClickActionListener(tabIndex, new RenameComponentActionListener(analyzerJobBuilder) {
            @Override
            protected void onNameChanged() {
                _tabbedPane.setTitleAt(tabIndex, LabelUtils.getLabel(analyzerJobBuilder));
            }
        });

        _tabbedPane.setSelectedIndex(tabIndex);
    }

    public void initializeTransformer(final TransformerComponentBuilder<?> transformerJobBuilder) {
        @SuppressWarnings("unchecked")
        final Renderer<TransformerComponentBuilder<?>, ? extends ComponentBuilderPresenter> renderer = (Renderer<TransformerComponentBuilder<?>, ? extends ComponentBuilderPresenter>) _presenterRendererFactory
                .getRenderer(transformerJobBuilder, ComponentBuilderPresenterRenderingFormat.class);
        final TransformerComponentBuilderPresenter presenter = (TransformerComponentBuilderPresenter) renderer
                .render(transformerJobBuilder);

        _transformerPresenters.put(transformerJobBuilder, presenter);
        final JComponent comp = presenter.createJComponent();
        _tabbedPane.addTab(LabelUtils.getLabel(transformerJobBuilder),
                IconUtils.getDescriptorIcon(transformerJobBuilder.getDescriptor(), TAB_ICON_SIZE), comp);
        _jobBuilderTabs.put(presenter, comp);
        final int tabIndex = _tabbedPane.getTabCount() - 1;
        _tabbedPane.setSelectedIndex(tabIndex);
        _tabbedPane.setRightClickActionListener(tabIndex, new ComponentBuilderTabTextActionListener(
                _analysisJobBuilder, transformerJobBuilder, tabIndex, _tabbedPane));
        _tabbedPane.setDoubleClickActionListener(tabIndex, new RenameComponentActionListener(transformerJobBuilder) {
            @Override
            protected void onNameChanged() {
                _tabbedPane.setTitleAt(tabIndex, LabelUtils.getLabel(transformerJobBuilder));
            }
        });
    }

    public void initializeFilter(final FilterComponentBuilder<?, ?> filterJobBuilder) {
        @SuppressWarnings("unchecked")
        final Renderer<FilterComponentBuilder<?, ?>, ? extends ComponentBuilderPresenter> renderer = (Renderer<FilterComponentBuilder<?, ?>, ? extends ComponentBuilderPresenter>) _presenterRendererFactory
                .getRenderer(filterJobBuilder, ComponentBuilderPresenterRenderingFormat.class);
        final FilterComponentBuilderPresenter presenter = (FilterComponentBuilderPresenter) renderer
                .render(filterJobBuilder);

        _filterPresenters.put(filterJobBuilder, presenter);
        JComponent comp = presenter.createJComponent();
        _tabbedPane.addTab(LabelUtils.getLabel(filterJobBuilder),
                IconUtils.getDescriptorIcon(filterJobBuilder.getDescriptor(), TAB_ICON_SIZE), comp);
        _jobBuilderTabs.put(presenter, comp);
        final int tabIndex = _tabbedPane.getTabCount() - 1;
        if (MaxRowsFilterShortcutPanel.isFilter(filterJobBuilder)) {
            // the max rows shortcut must be disabled using checkbox on
            // source
            // tab
            _tabbedPane.setUnclosableTab(tabIndex);
        } else {
            _tabbedPane.setSelectedIndex(tabIndex);
            _tabbedPane.setRightClickActionListener(tabIndex, new ComponentBuilderTabTextActionListener(
                    _analysisJobBuilder, filterJobBuilder, tabIndex, _tabbedPane));
            _tabbedPane.setDoubleClickActionListener(tabIndex, new RenameComponentActionListener(filterJobBuilder) {
                @Override
                protected void onNameChanged() {
                    _tabbedPane.setTitleAt(tabIndex, LabelUtils.getLabel(filterJobBuilder));
                }
            });
        }
    }

    @Override
    public void tabClosed(TabCloseEvent ev) {
        Component panel = ev.getTabContents();

        if (panel != null) {
            // if panel was a row processing analyzer panel
            for (Iterator<AnalyzerComponentBuilderPresenter> it = _analyzerPresenters.values().iterator(); it.hasNext();) {
                AnalyzerComponentBuilderPresenter analyzerPresenter = it.next();
                if (_jobBuilderTabs.get(analyzerPresenter) == panel) {
                    _analysisJobBuilder.removeAnalyzer(analyzerPresenter.getComponentBuilder());
                    return;
                }
            }

            // if panel was a transformer panel
            for (Iterator<TransformerComponentBuilderPresenter> it = _transformerPresenters.values().iterator(); it
                    .hasNext();) {
                TransformerComponentBuilderPresenter transformerPresenter = it.next();
                if (_jobBuilderTabs.get(transformerPresenter) == panel) {
                    _analysisJobBuilder.removeTransformer(transformerPresenter.getComponentBuilder());
                    return;
                }
            }

            // if panel was a filter panel
            for (Iterator<FilterComponentBuilderPresenter> it = _filterPresenters.values().iterator(); it.hasNext();) {
                FilterComponentBuilderPresenter filterPresenter = it.next();
                if (_jobBuilderTabs.get(filterPresenter) == panel) {
                    _analysisJobBuilder.removeFilter(filterPresenter.getComponentBuilder());
                    return;
                }
            }
        }
        logger.info("Could not handle removal of tab {}, containing {}", ev.getTabIndex(), panel);
    }

    public void applyPropertyValues() {
        for (FilterComponentBuilderPresenter presenter : _filterPresenters.values()) {
            presenter.applyPropertyValues();
        }

        for (TransformerComponentBuilderPresenter presenter : _transformerPresenters.values()) {
            presenter.applyPropertyValues();
        }

        for (AnalyzerComponentBuilderPresenter presenter : _analyzerPresenters.values()) {
            presenter.applyPropertyValues();
        }
    }

    public void removeTransformer(TransformerComponentBuilder<?> transformerJobBuilder) {
        TransformerComponentBuilderPresenter presenter = _transformerPresenters.remove(transformerJobBuilder);
        JComponent comp = _jobBuilderTabs.remove(presenter);
        _tabbedPane.remove(comp);
    }

    public void removeFilter(FilterComponentBuilder<?, ?> filterJobBuilder) {
        FilterComponentBuilderPresenter presenter = _filterPresenters.remove(filterJobBuilder);
        JComponent comp = _jobBuilderTabs.remove(presenter);
        _tabbedPane.remove(comp);

        if (MaxRowsFilterShortcutPanel.isFilter(filterJobBuilder)) {
            _sourceColumnsPanel.getMaxRowsFilterShortcutPanel().resetToDefault();
        }
    }

    public void removeAnalyzer(AnalyzerComponentBuilder<?> analyzerJobBuilder) {
        AnalyzerComponentBuilderPresenter presenter = _analyzerPresenters.remove(analyzerJobBuilder);
        JComponent comp = _jobBuilderTabs.remove(presenter);
        _tabbedPane.remove(comp);
    }

    public void onSourceColumnsChanged(boolean everythingEnabled) {
        if (!everythingEnabled) {
            _tabbedPane.setSelectedIndex(SOURCE_TAB);
        }

        int tabCount = _tabbedPane.getTabCount();
        for (int i = 1; i < tabCount; i++) {
            _tabbedPane.setEnabledAt(i, everythingEnabled);
        }
    }
}
