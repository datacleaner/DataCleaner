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
package org.datacleaner.result.renderer;

import java.net.URLEncoder;

import javax.inject.Inject;

import org.datacleaner.beans.api.Renderer;
import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.beans.api.RendererPrecedence;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.beans.writers.WriteDataResultHtmlRenderer;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.monitor.server.security.TenantResolver;
import org.datacleaner.monitor.server.security.UserBean;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A specialized HTML renderer for {@link WriteDataResult}s that reference
 * datastores that are available in the live configuration. In those cases we
 * will make available some actionable buttons to let the user navigate the
 * datastore.
 */
@RendererBean(HtmlRenderingFormat.class)
public class InteractiveWriteDataResultHtmlRenderer implements Renderer<WriteDataResult, HtmlFragment> {

    private static final Logger logger = LoggerFactory.getLogger(InteractiveWriteDataResultHtmlRenderer.class);

    @Inject
    AnalyzerBeansConfiguration _configuration;

    @Inject
    TenantResolver _tenantResolver;

    @Override
    public RendererPrecedence getPrecedence(WriteDataResult res) {
        final DatastoreCatalog datastoreCatalog = _configuration.getDatastoreCatalog();
        final Datastore datastore = res.getDatastore(datastoreCatalog);
        if (datastore == null) {
            return RendererPrecedence.NOT_CAPABLE;
        }

        // higher precedence than the default renderer
        return RendererPrecedence.HIGH;
    }

    @Override
    public HtmlFragment render(WriteDataResult res) {
        // TODO: Dirty way of obtaining the user bean - ideally we would be able
        // to just inject it, but SpringInjectionManager does not support
        // session scoped beans.
        final UserBean user = new UserBean(_tenantResolver);
        user.updateUser();

        final WriteDataResultHtmlRenderer delegateRenderer = new WriteDataResultHtmlRenderer();
        delegateRenderer.setConfiguration(_configuration);

        final HtmlFragment fragment = delegateRenderer.render(res);
        if (fragment instanceof SimpleHtmlFragment) {
            SimpleHtmlFragment frag = (SimpleHtmlFragment) fragment;
            final DatastoreCatalog datastoreCatalog = _configuration.getDatastoreCatalog();
            final Datastore datastore = res.getDatastore(datastoreCatalog);

            if (datastore != null) {
                try {
                    final String encodedName = URLEncoder.encode(datastore.getName(), "UTF8");

                    if (user.isConfigurationEditor()) {
                        if (datastore instanceof FileDatastore) {
                            final String html = "<button onclick=\"window.location='" + "../datastores/" + encodedName
                                    + ".download'\" class=\"DownloadButton\">Download</button>";
                            frag.addBodyElement(html);
                        }
                    }

                    if (user.isQueryAllowed()) {
                        frag.addBodyElement("<button onclick=\"window.open('../../../query.jsf?ds="
                                + encodedName
                                + "','_blank','location=no,width=770,height=400,toolbar=no,menubar=no');\" class=\"QueryButton\">Query</button>");
                    }
                } catch (Exception e) {
                    logger.error("Failed to append interactive HTML fragments to result", e);
                }
            }
        }

        return fragment;
    }

}
