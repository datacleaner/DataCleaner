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
package org.datacleaner.components.fillpattern;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FillPatternGroup implements Iterable<FillPattern>, Comparable<FillPatternGroup>, Serializable {

    private static final long serialVersionUID = 1L;

    private final String _groupName;
    private final List<FillPattern> _patterns;

    public FillPatternGroup(String groupName, List<FillPattern> patterns) {
        _groupName = groupName;
        _patterns = patterns;
    }

    public String getGroupName() {
        return _groupName;
    }

    @Override
    public Iterator<FillPattern> iterator() {
        return _patterns.iterator();
    }

    public List<FillPattern> asList() {
        return Collections.unmodifiableList(_patterns);
    }

    public int getPatternCount() {
        return _patterns.size();
    }

    public int getTotalObservationCount() {
        return _patterns.stream().collect(Collectors.summingInt(p -> p.getObservationCount()));
    }

    @Override
    public int compareTo(FillPatternGroup other) {
        int diff = other.getPatternCount() - getPatternCount();
        if (diff == 0) {
            diff = other.getTotalObservationCount() - getTotalObservationCount();
            if (diff == 0) {
                // at this point it does not matter, we just don't want to
                // return them as equal
                diff = other.hashCode() - hashCode();
            }
        }
        return diff;
    }
}
