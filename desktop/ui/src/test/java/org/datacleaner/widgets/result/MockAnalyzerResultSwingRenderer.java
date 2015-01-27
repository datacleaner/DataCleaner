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

import java.awt.Color;

import javax.swing.JComponent;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.MockAnalyzerFutureResult;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.test.MockFutureAnalyzer;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.ResultWindow;

import com.google.inject.Guice;
import com.google.inject.Injector;

@RendererBean(SwingRenderingFormat.class)
public class MockAnalyzerResultSwingRenderer implements Renderer<MockAnalyzerFutureResult, JComponent> {
    
    @Override
    public RendererPrecedence getPrecedence(MockAnalyzerFutureResult renderable) {
        return RendererPrecedence.MEDIUM;
    }

    @Override
    public JComponent render(MockAnalyzerFutureResult renderable) {
        DCLabel resultLabel = new DCLabel(false, renderable.getMockMessage(), Color.GREEN, null);
        
        final DCPanel resultPanel = new DCPanel();
        resultPanel.add(resultLabel);
        
        return resultPanel;
    }
    
    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        Injector injector = Guice.createInjector(new DCModuleImpl());

        // run a small job
        final AnalysisJobBuilder ajb = injector.getInstance(AnalysisJobBuilder.class);
        Datastore ds = injector.getInstance(DatastoreCatalog.class).getDatastore("orderdb");
        DatastoreConnection con = ds.openConnection();
        SchemaNavigator sn = con.getSchemaNavigator();
        ajb.setDatastore(ds);
        ajb.addSourceColumns(sn.convertToTable("PUBLIC.CUSTOMERS").getColumns());

        AnalyzerComponentBuilder<MockFutureAnalyzer> mockAnalyzerResultFutureAnalyzerBuilder = ajb
                .addAnalyzer(MockFutureAnalyzer.class);
        mockAnalyzerResultFutureAnalyzerBuilder.addInputColumn(ajb.getSourceColumnByName("PUBLIC.CUSTOMERS.ADDRESSLINE2"));

        ResultWindow resultWindow = injector.getInstance(ResultWindow.class);
        resultWindow.setVisible(true);
        resultWindow.startAnalysis();
    }

}
