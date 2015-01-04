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
package org.eobjects.analyzer.result.renderer;

import javax.swing.table.TableModel;

import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.html.HtmlFragment;
import org.eobjects.analyzer.result.html.SimpleHtmlFragment;
import org.eobjects.analyzer.util.ReflectionUtils;

@RendererBean(HtmlRenderingFormat.class)
public class AnnotatedRowsHtmlRenderer implements Renderer<AnnotatedRowsResult, HtmlFragment> {

    /**
     * Defines a max number of rows to render in the HTML. Since annotated rows
     * are considered "samples" we put a rather low maximum here to avoid
     * creating massive-size HTML renderings.
     */
    private static final int MAX_ROWS = 100;

    @Override
    public RendererPrecedence getPrecedence(AnnotatedRowsResult renderable) {
        return RendererPrecedence.MEDIUM;
    }

    @Override
    public HtmlFragment render(final AnnotatedRowsResult result) {
        SimpleHtmlFragment htmlFragment = new SimpleHtmlFragment();

        InputColumn<?>[] highlightedColumns = result.getHighlightedColumns();
        int[] highlightedIndexes = new int[highlightedColumns.length];
        for (int i = 0; i < highlightedColumns.length; i++) {
            highlightedIndexes[i] = result.getColumnIndex(highlightedColumns[i]);
        }

        final TableModel tableModel = result.toTableModel(MAX_ROWS);

        final Description description = ReflectionUtils.getAnnotation(result.getClass(), Description.class);
        final String descriptionText;
        if (description != null) {
            descriptionText = description.value();
        } else {
            descriptionText = "Records";
        }
        final int rowCount = result.getAnnotatedRowCount();
        htmlFragment.addBodyElement(new SectionHeaderBodyElement(descriptionText + " (" + rowCount
                + ")"));

        if (rowCount == 0) {
            htmlFragment.addBodyElement("<p>No records to display.</p>");
        } else {
            htmlFragment.addBodyElement(new TableBodyElement(tableModel, "annotatedRowsTable", highlightedIndexes));
        }

        return htmlFragment;
    }
}