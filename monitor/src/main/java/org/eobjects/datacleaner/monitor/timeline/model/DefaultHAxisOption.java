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

public class DefaultHAxisOption implements ChartOptions.HorizontalAxisOption {

    private final Date _endDate;
    private final Date _beginDate;

    public DefaultHAxisOption() {
        this(null, null);
    }

    public DefaultHAxisOption(Date beginDate, Date endDate) {
        _beginDate = beginDate;
        _endDate = endDate;
    }

    @Override
    public Date getBeginDate() {
        return _beginDate;
    }

    @Override
    public Date getEndDate() {
        return _endDate;
    }

}
