/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.timeline.model;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Defines visual options for a timeline chart
 */
public class ChartOptions implements IsSerializable {

    /**
     * Defines options pertaining to the height of the chart.
     */
    public static interface HeightOption extends IsSerializable {
        public Integer getHeight();
    }

    /**
     * Defines options pertaining to the horizontal axis (time dimension) of the
     * chart.
     */
    public static interface HorizontalAxisOption extends IsSerializable {
        public Date getBeginDate();

        public Date getEndDate();
    }

    /**
     * Defines options pertaining to the vertical axis (metric values) of the
     * chart.
     */
    public static interface VerticalAxisOption extends IsSerializable {
        public Integer getMinimumValue();

        public Integer getMaximumValue();
    }

    private HeightOption _height;
    private HorizontalAxisOption _horizontalAxisOption;
    private VerticalAxisOption _verticalAxisOption;

    public HeightOption getHeight() {
        return _height;
    }

    public void setHeight(HeightOption height) {
        _height = height;
    }

    public HorizontalAxisOption getHorizontalAxisOption() {
        return _horizontalAxisOption;
    }

    public void setHorizontalAxisOption(HorizontalAxisOption horizontalAxisOption) {
        _horizontalAxisOption = horizontalAxisOption;
    }

    public VerticalAxisOption getVerticalAxisOption() {
        return _verticalAxisOption;
    }

    public void setVerticalAxisOption(VerticalAxisOption verticalAxisOption) {
        _verticalAxisOption = verticalAxisOption;
    }

}
