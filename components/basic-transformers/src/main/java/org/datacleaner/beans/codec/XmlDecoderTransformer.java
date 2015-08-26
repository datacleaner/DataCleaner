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
package org.datacleaner.beans.codec;

import org.apache.commons.lang.StringEscapeUtils;
import org.datacleaner.api.*;
import org.datacleaner.components.categories.StringManipulationCategory;
import org.datacleaner.data.MockInputColumn;

import javax.inject.Named;

@Named("XML decoder")
@Description("Decodes XML content into plain text")
@Categorized(StringManipulationCategory.class)
@WSStatelessComponent
public class XmlDecoderTransformer implements Transformer {

    @Configured
    InputColumn<String> column;

    public XmlDecoderTransformer() {
    }

    public XmlDecoderTransformer(MockInputColumn<String> column) {
        this();
        this.column = column;
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, column.getName() + " (XML decoded)");
    }

    @Override
    public String[] transform(InputRow inputRow) {
        final String value = inputRow.getValue(column);
        if (value == null) {
            return new String[1];
        }
        final String unescaped = StringEscapeUtils.unescapeXml(value);
        return new String[] { unescaped };
    }

}
