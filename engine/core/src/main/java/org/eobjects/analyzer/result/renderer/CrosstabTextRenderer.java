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

import java.text.NumberFormat;
import java.util.List;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.apache.metamodel.util.FormatHelper;

@RendererBean(TextRenderingFormat.class)
public class CrosstabTextRenderer extends AbstractRenderer<CrosstabResult, String> {

    private static class TextCrosstabRendererCallback implements CrosstabRendererCallback<String> {

        private NumberFormat decimalFormat = FormatHelper.getUiNumberFormat();

        private boolean leftAligned;
        private StringBuilder sb;
        private int horizontalDimensionWidth;

        @Override
        public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
                List<CrosstabDimension> verticalDimensions) {
            sb = new StringBuilder();
            horizontalDimensionWidth = 0;
            for (CrosstabDimension dimension : horizontalDimensions) {
                List<String> categories = dimension.getCategories();
                for (String category : categories) {
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
        public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width) {
            int trailingBlanks = horizontalDimensionWidth * width - category.length();

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
        public void verticalHeaderCell(String category, CrosstabDimension dimension, int height) {
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
        public void valueCell(Object value, ResultProducer drillToDetailResultProducer) {
            boolean leftAligned = this.leftAligned;

            String stringValue = (value == null ? null : value.toString());
            stringValue = LabelUtils.getValueLabel(stringValue);

            if (value instanceof Number) {
                leftAligned = false;
                if (value instanceof Double || value instanceof Float) {
                    stringValue = decimalFormat.format(value);
                }
            }

            int trailingBlanks = horizontalDimensionWidth - stringValue.length();
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
        public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension) {
            int dimensionWidth = getWidth(verticalDimension);

            for (int i = 0; i < dimensionWidth; i++) {
                sb.append(' ');
            }

            // separator
            sb.append(' ');
        }

        private int getWidth(CrosstabDimension verticalDimension) {
            List<String> categories = verticalDimension.getCategories();
            int longestCategory = 0;
            for (String category : categories) {
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

    @Override
    public String render(CrosstabResult result) {
        return render(result.getCrosstab());
    }

    public String render(Crosstab<?> crosstab) {
        return new CrosstabRenderer(crosstab).render(new TextCrosstabRendererCallback());
    }

}
