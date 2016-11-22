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

import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.datacleaner.api.Description;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.result.ListResult;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.datacleaner.util.ReflectionUtils;

@RendererBean(HtmlRenderingFormat.class)
public class ListResultHtmlRenderer implements Renderer<ListResult<?>, HtmlFragment> {

    @Override
    public RendererPrecedence getPrecedence(final ListResult<?> renderable) {
        return RendererPrecedence.LOW;
    }

    @Override
    public HtmlFragment render(final ListResult<?> result) {
        final SimpleHtmlFragment htmlFragment = new SimpleHtmlFragment();

        final List<?> values = result.getValues();

        final int rowCount = values.size();
        final TableModel tableModel = new DefaultTableModel(rowCount, 1);
        for (int i = 0; i < rowCount; i++) {
            tableModel.setValueAt(values.get(i), i, 0);

        }

        final Description description = ReflectionUtils.getAnnotation(result.getClass(), Description.class);
        final String descriptionText;
        if (description != null) {
            descriptionText = description.value();
        } else {
            descriptionText = "Values";
        }

        htmlFragment.addBodyElement(new SectionHeaderBodyElement(descriptionText + " (" + rowCount + ")"));

        if (rowCount == 0) {
            htmlFragment.addBodyElement("<p>No records to display.</p>");
        } else {
            htmlFragment.addBodyElement(new TableBodyElement(tableModel, "annotatedRowsTable", new int[0]));
        }

        return htmlFragment;
    }
}
