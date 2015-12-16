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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.DCModuleImpl;
import org.datacleaner.guice.Nullable;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.user.QuickAnalysisStrategy;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.WidgetUtils;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * ActionListener for performing a quick analysis based on standard analyzers.
 */
public class QuickAnalysisActionListener implements ActionListener {

    private final Datastore _datastore;
    private final Table _table;
    private final Column[] _columns;
    private final DCModule _dcModule;
    private final UserPreferences _userPreferences;
    private final DataCleanerConfiguration _configuration;

    @Inject
    protected QuickAnalysisActionListener(Datastore datastore, @Nullable Table table, @Nullable Column[] columns,
            DCModule dcModule, UserPreferences userPreferences, DataCleanerConfiguration configuration) {
        _datastore = datastore;
        _table = table;
        _columns = columns;
        _dcModule = dcModule;
        _userPreferences = userPreferences;
        _configuration = configuration;
    }

    public Column[] getColumns() {
        if (_columns == null) {
            return _table.getColumns();
        }
        return _columns;
    }

    public Table getTable() {
        if (_table == null) {
            return _columns[0].getTable();
        }
        return _table;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final AnalysisJobBuilder ajb = new AnalysisJobBuilder(_configuration);
        ajb.setDatastore(_datastore);
        ajb.addSourceColumns(getColumns());

        final QuickAnalysisStrategy quickAnalysisStrategy = QuickAnalysisStrategy
                .loadFromUserPreferences(_userPreferences);

        quickAnalysisStrategy.configureAnalysisJobBuilder(ajb);

        try {
            if (!ajb.isConfigured(true)) {
                throw new IllegalStateException("Unknown job configuration issue!");
            }

            Injector injector = Guice.createInjector(new DCModuleImpl(_dcModule, ajb));

            RunAnalysisActionListener actionListener = injector.getInstance(RunAnalysisActionListener.class);
            actionListener.actionPerformed(event);
        } catch (Exception e) {
            WidgetUtils.showErrorMessage("Error", "Could not perform quick analysis on table " + _table.getName(), e);
        }

    }

}
