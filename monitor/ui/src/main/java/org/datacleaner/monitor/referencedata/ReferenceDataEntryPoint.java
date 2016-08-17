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
package org.datacleaner.monitor.referencedata;

import java.util.Set;

import org.datacleaner.monitor.referencedata.widgets.SectionWidget;
import org.datacleaner.monitor.referencedata.widgets.UploadFormWidget;
import org.datacleaner.monitor.shared.ClientConfig;
import org.datacleaner.monitor.shared.DictionaryClientConfig;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.datacleaner.monitor.util.DCAsyncCallback;
import org.datacleaner.monitor.util.ErrorHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

public class ReferenceDataEntryPoint implements EntryPoint {
    private static final String FILE_UPLOAD_HTML_ID = "FileUpload";
    private static final String DICTIONARIES_HTML_ID = "Dictionaries";
    private static final String SYNONYMS_HTML_ID = "Synonyms";
    private static final String PATTERNS_HTML_ID = "Patterns";
    private static final String DICTIONARIES_TITLE = "Dictionaries";
    private static final String SYNONYMS_TITLE = "Synonyms";
    private static final String PATTERNS_TITLE = "Patterns";

    public void onModuleLoad() {
        GWT.setUncaughtExceptionHandler(ErrorHandler.getUncaughtExceptionHandler());
        final ClientConfig clientConfig = new DictionaryClientConfig();
        final ReferenceDataServiceAsync service = GWT.create(ReferenceDataService.class);
        render(service, clientConfig);
    }

    protected void render(ReferenceDataServiceAsync service, ClientConfig clientConfig) {
        TenantIdentifier tenantId = clientConfig.getTenant();
        RootPanel.get(FILE_UPLOAD_HTML_ID).add(new UploadFormWidget(tenantId.getId()));
        RootPanel.get(DICTIONARIES_HTML_ID).add(new LoadingIndicator());
        RootPanel.get(SYNONYMS_HTML_ID).add(new LoadingIndicator());
        RootPanel.get(PATTERNS_HTML_ID).add(new LoadingIndicator());
        
        service.getDictionaries(tenantId, new DCAsyncCallback<Set<ReferenceDataItem>>() {
            @Override
            public void onSuccess(final Set<ReferenceDataItem> set) {
                RootPanel.get(DICTIONARIES_HTML_ID).clear();
                RootPanel.get(DICTIONARIES_HTML_ID).add(new SectionWidget(DICTIONARIES_TITLE, set));
            }
        });
        service.getSynonymCatalogs(tenantId, new DCAsyncCallback<Set<ReferenceDataItem>>() {
            @Override
            public void onSuccess(final Set<ReferenceDataItem> set) {
                RootPanel.get(SYNONYMS_HTML_ID).clear();
                RootPanel.get(SYNONYMS_HTML_ID).add(new SectionWidget(SYNONYMS_TITLE, set));
            }
        });
        service.getStringPatterns(tenantId, new DCAsyncCallback<Set<ReferenceDataItem>>() {
            @Override
            public void onSuccess(final Set<ReferenceDataItem> set) {
                RootPanel.get(PATTERNS_HTML_ID).clear();
                RootPanel.get(PATTERNS_HTML_ID).add(new SectionWidget(PATTERNS_TITLE, set));
            }
        });
    }
}
