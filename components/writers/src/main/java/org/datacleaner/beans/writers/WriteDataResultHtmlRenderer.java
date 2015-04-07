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
package org.datacleaner.beans.writers;

import javax.inject.Inject;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.datacleaner.result.renderer.HtmlRenderingFormat;

@RendererBean(HtmlRenderingFormat.class)
public class WriteDataResultHtmlRenderer implements Renderer<WriteDataResult, HtmlFragment> {

    @Inject
    DataCleanerConfiguration _configuration;

    @Override
    public RendererPrecedence getPrecedence(WriteDataResult renderable) {
        return RendererPrecedence.MEDIUM;
    }

    public void setConfiguration(DataCleanerConfiguration configuration) {
        _configuration = configuration;
    }

    @Override
    public HtmlFragment render(WriteDataResult r) {
        final int inserts = r.getWrittenRowCount();
        final int updates = r.getUpdatesCount();
        final int errors = r.getErrorRowCount();
        final int total = inserts + updates + errors;
        final Datastore datastore = (_configuration == null ? null : r.getDatastore(_configuration
                .getDatastoreCatalog()));

        final StringBuilder sb = new StringBuilder("<div>");
        if (datastore != null) {
            sb.append("\n  <p>");
            if (total == 0) {
                sb.append("No data");
            } else {
                sb.append("Data");
            }
            sb.append(" written to <span class=\"datastoreName\">");
            sb.append(datastore.getName());
            sb.append("</span></p>");
        }

        if (inserts > 0) {
            sb.append("\n  <p>Executed ");
            sb.append(inserts);
            sb.append(" inserts</p>");
        }

        if (updates > 0) {
            sb.append("\n  <p>Executed ");
            sb.append(updates);
            sb.append(" updates</p>");
        }

        if (errors > 0) {
            sb.append("\n  <p>");
            sb.append(errors);
            sb.append(" Erroneous records</p>");
        }

        sb.append("\n</div>");

        final SimpleHtmlFragment frag = new SimpleHtmlFragment();
        frag.addBodyElement(sb.toString());
        return frag;
    }
}
