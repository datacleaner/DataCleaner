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
package org.datacleaner.monitor.shared.widgets;

import java.util.List;

import org.datacleaner.monitor.shared.DatastoreServiceAsync;
import org.datacleaner.monitor.shared.model.ColumnIdentifier;
import org.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.datacleaner.monitor.shared.model.HasName;
import org.datacleaner.monitor.shared.model.SchemaIdentifier;
import org.datacleaner.monitor.shared.model.TableIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * A tree widget that displays the schema tree of a Datastore.
 */
public class SchemaTree extends Tree implements OpenHandler<TreeItem> {

    private static final SafeHtml LOADING_ITEM_TEXT = SafeHtmlUtils.fromString("Loading...");

    private final DatastoreIdentifier _datastore;
    private final DatastoreServiceAsync _service;
    private final TenantIdentifier _tenant;

    public SchemaTree(final TenantIdentifier tenant, final DatastoreIdentifier datastore,
            final DatastoreServiceAsync service) {
        super();
        addStyleName("SchemaTree");

        _tenant = tenant;
        _datastore = datastore;
        _service = service;

        final TreeItem rootItem = addItem(SafeHtmlUtils.fromString(datastore.getName()));
        rootItem.setUserObject(datastore);
        rootItem.addStyleName("datastoreItem");
        rootItem.addItem(LOADING_ITEM_TEXT);

        addOpenHandler(this);
    }

    @Override
    public void onOpen(final OpenEvent<TreeItem> event) {
        final TreeItem item = event.getTarget();

        if (item.getChildCount() == 1) {
            final TreeItem child = item.getChild(0);
            final String childText = child.getText();
            if (LOADING_ITEM_TEXT.asString().equals(childText)) {
                loadChildren(item);
            }
        }
    }

    private void loadChildren(final TreeItem item) {
        final Object object = item.getUserObject();
        if (object instanceof DatastoreIdentifier) {
            final AsyncCallback<List<SchemaIdentifier>> callback = createTreeCallback(item, "schemaItem", true);
            _service.getSchemas(_tenant, _datastore, callback);
        } else if (object instanceof SchemaIdentifier) {
            final SchemaIdentifier schema = (SchemaIdentifier) object;
            final AsyncCallback<List<TableIdentifier>> callback = createTreeCallback(item, "tableItem", true);
            _service.getTables(_tenant, schema, callback);
        } else if (object instanceof TableIdentifier) {
            final TableIdentifier table = (TableIdentifier) object;
            final AsyncCallback<List<ColumnIdentifier>> callback = createTreeCallback(item, "columnItem", false);
            _service.getColumns(_tenant, table, callback);
        }
    }

    private <E extends HasName> AsyncCallback<List<E>> createTreeCallback(final TreeItem item,
            final String childStyleName, final boolean addLoadingItem) {
        return new DCAsyncCallback<List<E>>() {
            @Override
            public void onSuccess(final List<E> children) {
                item.removeItems();
                for (final E child : children) {
                    final String name = child.getName();
                    final TreeItem childItem = item.addItem(SafeHtmlUtils.fromString(name));
                    childItem.setUserObject(child);
                    childItem.addStyleName(childStyleName);
                    if (addLoadingItem) {
                        childItem.addItem(LOADING_ITEM_TEXT);
                    }
                }
            }
        };
    }
}
