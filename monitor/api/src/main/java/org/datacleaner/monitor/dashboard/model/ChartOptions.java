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
package org.datacleaner.monitor.dashboard.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Defines visual options for a timeline chart
 */
public class ChartOptions implements Serializable {

    /**
     * Defines options pertaining to the horizontal axis (time dimension) of the
     * chart.
     */
    public interface HorizontalAxisOption extends Serializable {
        Date getBeginDate();

        Date getEndDate();
    }

    /**
     * Defines options pertaining to the vertical axis (metric values) of the
     * chart.
     */
    public interface VerticalAxisOption extends Serializable {
        /**
         * Gets the height (in pixels) of the chart
         *
         * @return
         */
        int getHeight();

        Integer getMinimumValue();

        Integer getMaximumValue();

        boolean isLogarithmicScale();
    }

    private static final long serialVersionUID = 1L;
    private HorizontalAxisOption _horizontalAxisOption;
    private VerticalAxisOption _verticalAxisOption;

    public ChartOptions() {
        this(new DefaultHAxisOption(), new DefaultVAxisOption());
    }

    public ChartOptions(final Date begin, final Date end, final Integer height, final Integer minimumValue,
            final Integer maximumValue, final boolean logarithmicScale) {
        this(new DefaultHAxisOption(begin, end),
                new DefaultVAxisOption(height, minimumValue, maximumValue, logarithmicScale));
    }

    public ChartOptions(final HorizontalAxisOption horizontalAxisOption, final VerticalAxisOption verticalAxisOption) {
        _horizontalAxisOption = horizontalAxisOption;
        _verticalAxisOption = verticalAxisOption;
    }

    public HorizontalAxisOption getHorizontalAxisOption() {
        return _horizontalAxisOption;
    }

    public VerticalAxisOption getVerticalAxisOption() {
        return _verticalAxisOption;
    }


}
