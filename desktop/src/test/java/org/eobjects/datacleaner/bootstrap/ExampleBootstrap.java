/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.bootstrap;

import java.awt.Image;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.eobjects.analyzer.beans.BooleanAnalyzer;
import org.eobjects.analyzer.beans.DateAndTimeAnalyzer;
import org.eobjects.analyzer.beans.NumberAnalyzer;
import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.cli.CliArguments;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.schema.Column;
import org.junit.Ignore;

@Ignore
public class ExampleBootstrap {

	/**
	 * An example bootstrap which is similar in function to what Pentaho Data
	 * Integration will be using, when embedding DataCleaner.
	 */
	public static void main(String[] args) {
		new Bootstrap(new BootstrapOptions() {

			@Override
			public boolean isSingleDatastoreMode() {
				return true;
			}

			@Override
			public boolean isCommandLineMode() {
				return false;
			}

			@Override
			public void initializeSingleDatastoreJob(AnalysisJobBuilder analysisJobBuilder, DataContext dataContext,
					InjectorBuilder injectorBuilder) {

				// add a few columns by path name
				analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMPLOYEENUMBER");
				analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.LASTNAME");
				analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME");
				analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMAIL");

				// add all columns of a table
				Column[] customerColumns = dataContext.getTableByQualifiedLabel("PUBLIC.CUSTOMERS").getColumns();
				analysisJobBuilder.addSourceColumns(customerColumns);

				List<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(Number.class);
				if (!numberColumns.isEmpty()) {
					analysisJobBuilder.addAnalyzer(NumberAnalyzer.class).addInputColumns(numberColumns);
				}

				List<InputColumn<?>> dateColumns = analysisJobBuilder.getAvailableInputColumns(Date.class);
				if (!dateColumns.isEmpty()) {
					analysisJobBuilder.addAnalyzer(DateAndTimeAnalyzer.class).addInputColumns(dateColumns);
				}

				List<InputColumn<?>> booleanColumns = analysisJobBuilder.getAvailableInputColumns(Boolean.class);
				if (!booleanColumns.isEmpty()) {
					analysisJobBuilder.addAnalyzer(BooleanAnalyzer.class).addInputColumns(booleanColumns);
				}

				List<InputColumn<?>> stringColumns = analysisJobBuilder.getAvailableInputColumns(String.class);
				if (!stringColumns.isEmpty()) {
					analysisJobBuilder.addAnalyzer(StringAnalyzer.class).addInputColumns(stringColumns);
				}
			}

			@Override
			public Datastore getSingleDatastore(DatastoreCatalog datastoreCatalog) {
				return datastoreCatalog.getDatastore("orderdb");
			}

			@Override
			public ExitActionListener getExitActionListener() {
				return null;
			}

			@Override
			public CliArguments getCommandLineArguments() {
				return null;
			}

			@Override
			public Image getWelcomeImage() {
				try {
					URL url = ResourceManager.get().getUrl("images/pdi_dc_banner.png");
					return ImageIO.read(url);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}).run();
	}
}
