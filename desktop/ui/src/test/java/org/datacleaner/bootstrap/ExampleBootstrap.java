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
package org.datacleaner.bootstrap;

import java.awt.Image;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.BooleanAnalyzer;
import org.datacleaner.beans.DateAndTimeAnalyzer;
import org.datacleaner.beans.NumberAnalyzer;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.cli.CliArguments;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.ResourceManager;
import org.junit.Ignore;

@Ignore
public class ExampleBootstrap {

    /**
     * An example bootstrap which is similar in function to what Pentaho Data
     * Integration will be using, when embedding DataCleaner.
     */
    public static void main(final String[] args) {
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
            public void initializeSingleDatastoreJob(final AnalysisJobBuilder analysisJobBuilder,
                    final DataContext dataContext, final InjectorBuilder injectorBuilder) {

                // add a few columns by path name
                analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMPLOYEENUMBER");
                analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.LASTNAME");
                analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME");
                analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMAIL");

                // add all columns of a table
                final List<Column> customerColumns = dataContext.getTableByQualifiedLabel("PUBLIC.CUSTOMERS").getColumns();
                analysisJobBuilder.addSourceColumns(customerColumns);

                final List<InputColumn<?>> numberColumns = analysisJobBuilder.getAvailableInputColumns(Number.class);
                if (!numberColumns.isEmpty()) {
                    analysisJobBuilder.addAnalyzer(NumberAnalyzer.class).addInputColumns(numberColumns);
                }

                final List<InputColumn<?>> dateColumns = analysisJobBuilder.getAvailableInputColumns(Date.class);
                if (!dateColumns.isEmpty()) {
                    analysisJobBuilder.addAnalyzer(DateAndTimeAnalyzer.class).addInputColumns(dateColumns);
                }

                final List<InputColumn<?>> booleanColumns = analysisJobBuilder.getAvailableInputColumns(Boolean.class);
                if (!booleanColumns.isEmpty()) {
                    analysisJobBuilder.addAnalyzer(BooleanAnalyzer.class).addInputColumns(booleanColumns);
                }

                final List<InputColumn<?>> stringColumns = analysisJobBuilder.getAvailableInputColumns(String.class);
                if (!stringColumns.isEmpty()) {
                    analysisJobBuilder.addAnalyzer(StringAnalyzer.class).addInputColumns(stringColumns);
                }
            }

            @Override
            public Datastore getSingleDatastore(final DatastoreCatalog datastoreCatalog) {
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
                    final URL url = ResourceManager.get().getUrl("images/pdi_dc_banner.png");
                    return ImageIO.read(url);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }).run();
    }
}
