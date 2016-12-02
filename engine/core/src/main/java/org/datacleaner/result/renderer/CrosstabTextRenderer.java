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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;

import org.datacleaner.api.RendererBean;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.ResultProducer;
import org.datacleaner.util.LabelUtils;
import org.datacleaner.util.ReflectionUtils;

@RendererBean(TextRenderingFormat.class)
public class CrosstabTextRenderer extends AbstractRenderer<CrosstabResult, String> {

    private static class TextCrosstabRendererCallback implements CrosstabRendererCallback<String> {

        private final NumberFormat decimalFormat = getUiNumberFormat();

        private boolean leftAligned;
        private StringBuilder sb;
        private int horizontalDimensionWidth;

        @Override
        public void beginTable(final Crosstab<?> crosstab, final List<CrosstabDimension> horizontalDimensions,
                final List<CrosstabDimension> verticalDimensions) {
            sb = new StringBuilder();
            horizontalDimensionWidth = 0;
            for (final CrosstabDimension dimension : horizontalDimensions) {
                final List<String> categories = dimension.getCategories();
                for (final String category : categories) {
                    horizontalDimensionWidth = Math.max(horizontalDimensionWidth, category.length());
                }
            }

            // minimum width = 6
            horizontalDimensionWidth = Math.max(horizontalDimensionWidth, 6);

            if (ReflectionUtils.is(crosstab.getValueClass(), Number.class)) {
                leftAligned = false;
            } else {
                leftAligned = true;
            }
        }

        @Override
        public void endTable() {
        }

        @Override
        public void beginRow() {
        }

        @Override
        public void endRow() {
            sb.append('\n');
        }

        @Override
        public void horizontalHeaderCell(final String category, final CrosstabDimension dimension, final int width) {
            final int trailingBlanks = horizontalDimensionWidth * width - category.length();

            if (leftAligned) {
                sb.append(category);
                for (int i = 0; i < trailingBlanks; i++) {
                    sb.append(' ');
                }
            } else {
                for (int i = 0; i < trailingBlanks; i++) {
                    sb.append(' ');
                }
                sb.append(category);
            }

            // separator
            sb.append(' ');
        }

        @Override
        public void verticalHeaderCell(final String category, final CrosstabDimension dimension, final int height) {
            sb.append(category);

            int dimensionWidth = getWidth(dimension);
            dimensionWidth = dimensionWidth - category.length();
            for (int i = 0; i < dimensionWidth; i++) {
                sb.append(' ');
            }

            // separator
            sb.append(' ');
        }

        @Override
        public void valueCell(final Object value, final ResultProducer drillToDetailResultProducer) {
            boolean leftAligned = this.leftAligned;

            String stringValue = (value == null ? null : value.toString());
            stringValue = LabelUtils.getValueLabel(stringValue);

            if (value instanceof Number) {
                leftAligned = false;
                if (value instanceof Double || value instanceof Float) {
                    stringValue = decimalFormat.format(value);
                }
            }

            final int trailingBlanks = horizontalDimensionWidth - stringValue.length();
            if (leftAligned) {
                sb.append(stringValue);
                for (int i = 0; i < trailingBlanks; i++) {
                    sb.append(' ');
                }
            } else {
                for (int i = 0; i < trailingBlanks; i++) {
                    sb.append(' ');
                }
                sb.append(stringValue);
            }

            // separator
            sb.append(' ');
        }

        @Override
        public void emptyHeader(final CrosstabDimension verticalDimension,
                final CrosstabDimension horizontalDimension) {
            final int dimensionWidth = getWidth(verticalDimension);

            for (int i = 0; i < dimensionWidth; i++) {
                sb.append(' ');
            }

            // separator
            sb.append(' ');
        }

        private int getWidth(final CrosstabDimension verticalDimension) {
            final List<String> categories = verticalDimension.getCategories();
            int longestCategory = 0;
            for (final String category : categories) {
                longestCategory = Math.max(longestCategory, category.length());
            }
            return longestCategory;
        }

        @Override
        public String getResult() {
            if (sb == null) {
                return "";
            }
            return sb.toString();
        }

    }

    public static NumberFormat getUiNumberFormat() {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat format = new DecimalFormat("###.##", symbols);
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(2);
        return format;
    }

    @Override
    public String render(final CrosstabResult result) {
        return render(result.getCrosstab());
    }

    public String render(final Crosstab<?> crosstab) {
        return new CrosstabRenderer(crosstab).render(new TextCrosstabRendererCallback());
    }
}
