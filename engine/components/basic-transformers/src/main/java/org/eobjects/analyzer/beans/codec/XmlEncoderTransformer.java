/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.codec;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.StringManipulationCategory;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;

import com.google.common.escape.Escaper;
import com.google.common.xml.XmlEscapers;

@TransformerBean("XML encoder")
@Description("Encodes/escapes plain text into XML content")
@Categorized({ StringManipulationCategory.class })
public class XmlEncoderTransformer implements Transformer<String> {

    public static enum TargetFormat {
        Content, Attribute
    }

    @Configured
    InputColumn<String> column;

    @Configured
    TargetFormat targetFormat = TargetFormat.Content;

    public XmlEncoderTransformer() {
    }

    public XmlEncoderTransformer(MockInputColumn<String> column) {
        this();
        this.column = column;
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(column.getName() + " (XML encoded)");
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final String value = inputRow.getValue(column);
        if (value == null) {
            return new String[1];
        }
        final Escaper escaper;
        if (targetFormat == TargetFormat.Content) {
            escaper = XmlEscapers.xmlContentEscaper();
        } else {
            escaper = XmlEscapers.xmlAttributeEscaper();
        }
        final String escaped = escaper.escape(value);
        return new String[] { escaped };
    }

}
