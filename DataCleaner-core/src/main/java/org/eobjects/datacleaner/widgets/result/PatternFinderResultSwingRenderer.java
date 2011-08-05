/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DataCleanerHome;
import org.eobjects.datacleaner.util.LabelUtils;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCCollapsiblePanel;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.LazyRef;
import org.eobjects.metamodel.util.Ref;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@Provided
	WindowContext windowContext;

	private PatternFinderResultSwingRendererCrosstabDelegate delegateRenderer;

	@Override
	public JComponent render(PatternFinderResult result) {
		delegateRenderer = new PatternFinderResultSwingRendererCrosstabDelegate(windowContext);
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
			final String expandedLabel = "Pattern finder for group '" + LabelUtils.getLabel(entry.getKey() + "'");
			final int patternCount = crosstab.getDimension(
					crosstab.getDimensionIndex(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN)).getCategoryCount();
			final String collapsedLabel = expandedLabel + ": " + patternCount + " patterns";
			final DCCollapsiblePanel collapsiblePanel = new DCCollapsiblePanel(collapsedLabel, expandedLabel, collapsed,
					componentRef);
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
		LookAndFeelManager.getInstance().init();

		// run a small job
		AnalyzerBeansConfiguration conf = new JaxbConfigurationReader().create(new File(DataCleanerHome.get(), "conf.xml"));
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);
		Datastore ds = conf.getDatastoreCatalog().getDatastore("orderdb");
		DataContextProvider dcp = ds.getDataContextProvider();
		Table table = dcp.getSchemaNavigator().convertToTable("PUBLIC.CUSTOMERS");
		ajb.setDatastore(ds);
		ajb.addSourceColumns(table.getLiteralColumns());
		ajb.addRowProcessingAnalyzer(PatternFinderAnalyzer.class).addInputColumns(ajb.getSourceColumns())
				.setName("Ungrouped pattern finders");

		RowProcessingAnalyzerJobBuilder<PatternFinderAnalyzer> groupedPatternFinder = ajb.addRowProcessingAnalyzer(
				PatternFinderAnalyzer.class).setName("Grouped PF");
		ajb.addSourceColumns("PUBLIC.OFFICES.CITY", "PUBLIC.OFFICES.TERRITORY");
		groupedPatternFinder.addInputColumn(ajb.getSourceColumnByName("PUBLIC.OFFICES.CITY"));
		groupedPatternFinder.addInputColumn(ajb.getSourceColumnByName("PUBLIC.OFFICES.TERRITORY"), groupedPatternFinder
				.getDescriptor().getConfiguredProperty("Group column"));

		WindowContext windowContext = new DCWindowContext(conf);
		ResultWindow resultWindow = new ResultWindow(conf, ajb.toAnalysisJob(), null, windowContext);
		resultWindow.setVisible(true);
		resultWindow.startAnalysis();
	}
}
