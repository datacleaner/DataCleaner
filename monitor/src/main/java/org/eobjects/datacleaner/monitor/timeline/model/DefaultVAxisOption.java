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

public class DefaultVAxisOption implements ChartOptions.VerticalAxisOption {

    public static final int DEFAULT_HEIGHT = 350;

    private Integer _height;
    private Integer _minimumValue;
    private Integer _maximumValue;
    private boolean _logarithmicScale;

    public DefaultVAxisOption(Integer height, Integer minimumValue, Integer maximumValue, boolean logarithmicScale) {
        _height = height;
        _minimumValue = minimumValue;
        _maximumValue = maximumValue;
        _logarithmicScale = logarithmicScale;
    }

    public DefaultVAxisOption() {
        this(null, null, null, false);
    }

    @Override
    public Integer getMinimumValue() {
        return _minimumValue;
    }

    @Override
    public Integer getMaximumValue() {
        return _maximumValue;
    }

    @Override
    public int getHeight() {
        if (_height == null) {
            return DEFAULT_HEIGHT;
        }
        return _height;
    }

    @Override
    public boolean isLogarithmicScale() {
        return _logarithmicScale;
    }

}
