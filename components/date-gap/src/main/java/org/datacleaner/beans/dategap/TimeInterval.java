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
package org.datacleaner.beans.dategap;

import java.io.Serializable;
import java.util.Date;

import org.datacleaner.util.CompareUtils;

/**
 * Represents an interval consisting of two points in time. Either points (from
 * and to) in time can also be infinite, meaning that they span from "the past"
 * to a point in time or from a point in time to "the future".
 *
 *
 *
 */
public class TimeInterval implements Serializable, Comparable<TimeInterval>, Cloneable {

    private static final long serialVersionUID = 1L;

    private long _from;
    private long _to;

    public TimeInterval(final Date from, final Date to) {
        if (from == null) {
            throw new IllegalArgumentException("from cannot be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to cannot be null");
        }
        _from = from.getTime();
        _to = to.getTime();
    }

    public TimeInterval(final long from, final long to) {
        _from = from;
        _to = to;
    }

    public static TimeInterval merge(final TimeInterval o1, final TimeInterval o2) {
        final long from = Math.min(o1.getFrom(), o2.getFrom());
        final long to = Math.max(o1.getTo(), o2.getTo());
        return new TimeInterval(from, to);
    }

    public boolean before(final TimeInterval o) {
        return compareTo(o) < 0;
    }

    public boolean after(final TimeInterval o) {
        return compareTo(o) > 0;
    }

    public long getFrom() {
        return _from;
    }

    public long getTo() {
        return _to;
    }

    @Override
    public int compareTo(final TimeInterval o) {
        int diff = CompareUtils.compare(getFrom(), o.getFrom());
        if (diff == 0) {
            diff = CompareUtils.compare(getTo(), o.getTo());
        }
        return diff;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (_from ^ (_from >>> 32));
        result = prime * result + (int) (_to ^ (_to >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimeInterval other = (TimeInterval) obj;
        if (_from != other._from) {
            return false;
        }
        if (_to != other._to) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TimeInterval[" + getFrom() + "->" + getTo() + "]";
    }

    @Override
    public TimeInterval clone() {
        try {
            return (TimeInterval) super.clone();
        } catch (final CloneNotSupportedException e) {
            // should never happen
            throw new IllegalStateException("Clone failed.");
        }
    }

    public boolean overlapsWith(final TimeInterval interval) {
        return getOverlap(interval) != null;
    }

    public TimeInterval getOverlap(final TimeInterval interval) {
        final long from = interval.getFrom();
        final long to = interval.getTo();

        final long maximumFrom = Math.max(_from, from);
        final long minimumTo = Math.min(_to, to);

        if (maximumFrom <= minimumTo) {
            return new TimeInterval(maximumFrom, minimumTo);
        }
        return null;
    }

    /**
     * @return true if this interval represents a single point in time (ie. from
     *         and to are the same)
     */
    public boolean isSingleTimeInstance() {
        return _from == _to;
    }
}
