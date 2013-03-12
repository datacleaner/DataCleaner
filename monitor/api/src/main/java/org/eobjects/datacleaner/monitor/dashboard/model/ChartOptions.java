/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.dashboard.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Defines visual options for a timeline chart
 */
public class ChartOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Defines options pertaining to the horizontal axis (time dimension) of the
     * chart.
     */
    public static interface HorizontalAxisOption extends Serializable {
        public Date getBeginDate();

        public Date getEndDate();
    }

    /**
     * Defines options pertaining to the vertical axis (metric values) of the
     * chart.
     */
    public static interface VerticalAxisOption extends Serializable {
        /**
         * Gets the height (in pixels) of the chart
         * 
         * @return
         */
        public int getHeight();

        public Integer getMinimumValue();

        public Integer getMaximumValue();
        
        public boolean isLogarithmicScale();
    }

    private HorizontalAxisOption _horizontalAxisOption;
    private VerticalAxisOption _verticalAxisOption;

    public ChartOptions() {
        this(new DefaultHAxisOption(), new DefaultVAxisOption());
    }

    public ChartOptions(Date begin, Date end, Integer height, Integer minimumValue, Integer maximumValue, boolean logarithmicScale) {
        this(new DefaultHAxisOption(begin, end), new DefaultVAxisOption(height, minimumValue, maximumValue, logarithmicScale));
    }

    public ChartOptions(HorizontalAxisOption horizontalAxisOption, VerticalAxisOption verticalAxisOption) {
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
