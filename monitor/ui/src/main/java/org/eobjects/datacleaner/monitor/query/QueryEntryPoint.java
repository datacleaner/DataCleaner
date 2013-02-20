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
package org.eobjects.datacleaner.monitor.query;

import java.util.List;

import org.eobjects.datacleaner.monitor.query.widgets.QueryPanel;
import org.eobjects.datacleaner.monitor.shared.ClientConfig;
import org.eobjects.datacleaner.monitor.shared.DatastoreService;
import org.eobjects.datacleaner.monitor.shared.DatastoreServiceAsync;
import org.eobjects.datacleaner.monitor.shared.DictionaryClientConfig;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SchemaIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TableIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.ErrorHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * GWT Entry point for the Query module
 */
public class QueryEntryPoint implements EntryPoint {

    private static final DatastoreServiceAsync datastoreService = GWT.create(DatastoreService.class);

    @Override
    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(ErrorHandler.getUncaughtExceptionHandler());

        final ClientConfig clientConfig = new DictionaryClientConfig();
        final TenantIdentifier tenant = clientConfig.getTenant();

        final String datastoreName = Window.Location.getParameter("ds");
        if (datastoreName == null || datastoreName.length() == 0) {
            GWT.log("No 'ds' parameter found in URL");
            return;
        }

        final DatastoreIdentifier datastore = new DatastoreIdentifier(datastoreName);
        datastoreService.getDefaultSchema(tenant, datastore, new DCAsyncCallback<SchemaIdentifier>() {
            @Override
            public void onSuccess(final SchemaIdentifier schema) {
                datastoreService.getTables(tenant, schema, new DCAsyncCallback<List<TableIdentifier>>() {

                    @Override
                    public void onSuccess(final List<TableIdentifier> tables) {
                        render(tenant, datastore, schema, tables);
                    }
                });
            }
        });
    }

    private void render(TenantIdentifier tenant, DatastoreIdentifier datastore, SchemaIdentifier schema,
            List<TableIdentifier> tables) {
        final QueryPanel queryPanel = new QueryPanel(tenant, datastoreService, datastore, schema, tables);

        final RootPanel rootPanel = RootPanel.get("RootPanelTarget");
        rootPanel.add(queryPanel);
    }
}
