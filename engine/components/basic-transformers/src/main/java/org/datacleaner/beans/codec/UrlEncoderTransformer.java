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

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.beans.categories.StringManipulationCategory;
import org.datacleaner.data.MockInputColumn;
import org.apache.metamodel.util.HasName;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

@Named("URL encoder")
@Description("Encodes/escapes a URL or part of a URL")
@Categorized({ StringManipulationCategory.class })
public class UrlEncoderTransformer implements Transformer {

    public static enum TargetFormat implements HasName {
        FORM_PARAMETER("Form parameter"), FRAGMENT("URL fragment"), PATH_SEGMENT("Path segment");

        private final String _name;

        private TargetFormat(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    @Configured
    InputColumn<String> column;

    @Configured
    TargetFormat targetFormat = TargetFormat.FRAGMENT;

    public UrlEncoderTransformer() {
    }

    public UrlEncoderTransformer(MockInputColumn<String> column) {
        this();
        this.column = column;
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, column.getName() + " (URL encoded)");
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final String value = inputRow.getValue(column);
        if (value == null) {
            return new String[1];
        }
        final Escaper escaper;
        switch (targetFormat) {
        case FORM_PARAMETER:
            escaper = UrlEscapers.urlFormParameterEscaper();
            break;
        case FRAGMENT:
            escaper = UrlEscapers.urlFragmentEscaper();
            break;
        case PATH_SEGMENT:
            escaper = UrlEscapers.urlPathSegmentEscaper();
            break;
        default:
            throw new UnsupportedOperationException();
        }
        final String escaped = escaper.escape(value);
        return new String[] { escaped };
    }

}
