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
package org.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;
import org.datacleaner.api.RendererBean;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.beans.stringpattern.PatternFinderResult;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCCollapsiblePanel;
import org.datacleaner.windows.ResultWindow;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Renderer for {@link PatternFinderAnalyzer} results. Displays crosstabs with
 * optional charts displaying the distribution of the patterns.
 * 
 * @author Kasper Sørensen
 * 
 */
@RendererBean(SwingRenderingFormat.class)
public class PatternFinderResultSwingRenderer extends AbstractRenderer<PatternFinderResult, JComponent> {

	private static final Logger logger = LoggerFactory.getLogger(PatternFinderResultSwingRenderer.class);

	private static final int MAX_EXPANDED_GROUPS = 30;

	@Inject
	WindowContext windowContext;

	@Inject
	RendererFactory rendererFactory;

	@Inject
	ReferenceDataCatalog referenceDataCatalog;

	private PatternFinderResultSwingRendererCrosstabDelegate delegateRenderer;

	@Override
	public JComponent render(PatternFinderResult result) {
		delegateRenderer = new PatternFinderResultSwingRendererCrosstabDelegate(windowContext, rendererFactory,
				(MutableReferenceDataCatalog) referenceDataCatalog);
		if (result.isGroupingEnabled()) {
			return renderGroupedResult(result);
		} else {
			Crosstab<?> singleCrosstab = result.getSingleCrosstab();
			return renderCrosstab(singleCrosstab);
		}
	}

	public JComponent renderGroupedResult(PatternFinderResult result) {
		final DCPanel panel = new DCPanel();
		panel.setLayout(new VerticalLayout(0));
		final Map<String, Crosstab<?>> crosstabs = result.getGroupedCrosstabs();
		boolean collapsed = false;
		if (crosstabs.size() > MAX_EXPANDED_GROUPS) {
			collapsed = true;
		}

		final Set<Entry<String, Crosstab<?>>> entries = crosstabs.entrySet();
		for (Entry<String, Crosstab<?>> entry : entries) {
			final String groupName = entry.getKey();
			if (panel.getComponentCount() != 0) {
				panel.add(Box.createVerticalStrut(10));
			}
			final Crosstab<?> crosstab = entry.getValue();

			final Ref<JComponent> componentRef = new LazyRef<JComponent>() {
				@Override
				protected JComponent fetch() {
					logger.info("Rendering group result '{}'", groupName);
					final JComponent renderedResult = delegateRenderer.render(new CrosstabResult(crosstab));
					final DCPanel decoratedPanel = createDecoration(renderedResult);
					return decoratedPanel;
				}
			};

			final int patternCount = crosstab.getDimension(
					crosstab.getDimensionIndex(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN)).getCategoryCount();
			final String expandedLabel = (patternCount == 1 ? "1 pattern" : patternCount + " patterns") + " in group '"
					+ LabelUtils.getLabel(entry.getKey() + "'");
			final String collapsedLabel = expandedLabel;
			final DCCollapsiblePanel collapsiblePanel = new DCCollapsiblePanel(collapsedLabel, expandedLabel,
					collapsed, componentRef);
			panel.add(collapsiblePanel.toPanel());
		}
		return panel;
	}

	private DCPanel createDecoration(JComponent renderedResult) {
		renderedResult.setBorder(WidgetUtils.BORDER_SHADOW);
		final DCPanel wrappingPanel = new DCPanel();
		wrappingPanel.setLayout(new BorderLayout());
		wrappingPanel.add(renderedResult, BorderLayout.CENTER);
		wrappingPanel.setBorder(new EmptyBorder(4, 20, 4, 4));
		return wrappingPanel;
	}

	public JComponent renderCrosstab(Crosstab<?> crosstab) {
		CrosstabResult crosstabResult = new CrosstabResult(crosstab);
		return delegateRenderer.render(crosstabResult);
	}

	/**
	 * A main method that will display the results of a few example pattern
	 * finder analyzers. Useful for tweaking the charts and UI.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LookAndFeelManager.get().init();

		Injector injector = Guice.createInjector(new DCModuleImpl());

		// run a small job
		final AnalysisJobBuilder ajb = injector.getInstance(AnalysisJobBuilder.class);
		Datastore ds = injector.getInstance(DatastoreCatalog.class).getDatastore("orderdb");
		DatastoreConnection con = ds.openConnection();
		Table table = con.getSchemaNavigator().convertToTable("PUBLIC.CUSTOMERS");
		ajb.setDatastore(ds);
		ajb.addSourceColumns(table.getLiteralColumns());
		ajb.addAnalyzer(PatternFinderAnalyzer.class).addInputColumns(ajb.getSourceColumns())
				.setName("Ungrouped pattern finders");

		final AnalyzerComponentBuilder<PatternFinderAnalyzer> groupedPatternFinder = ajb.addAnalyzer(
				PatternFinderAnalyzer.class).setName("Grouped PF");
		ajb.addSourceColumns("PUBLIC.OFFICES.CITY", "PUBLIC.OFFICES.TERRITORY");
		groupedPatternFinder.addInputColumn(ajb.getSourceColumnByName("PUBLIC.OFFICES.CITY"));
		groupedPatternFinder.addInputColumn(ajb.getSourceColumnByName("PUBLIC.OFFICES.TERRITORY"), groupedPatternFinder
				.getDescriptor().getConfiguredProperty("Group column"));

		ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
		resultWindow.setVisible(true);
		resultWindow.startAnalysis();
	}
}
