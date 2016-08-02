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
package org.datacleaner;

import java.util.Comparator;

/**
 * Compares the versions of DataCleaner to determine the latest.
 *
 * Versions are Strings in format: X.Y.Z or X.Y.Z-SNAPSHOT. The comparator
 * expects only correct inputs, the values should be validated before passing it
 * to the comparator otherwise it will crash.
 */
public class VersionComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        String[] o1Split = o1.split("\\.");
        String[] o2Split = o2.split("\\.");

        for (int i = 0; i < Math.min(o1Split.length, o2Split.length); i++) {
            int specialEndCounter = 0;
            Integer o1Part;
            if (o1Split[i].contains("-")) {
                specialEndCounter++;
                o1Part = Integer.parseInt(o1Split[i].substring(0, o1Split[i].lastIndexOf("-")));
            } else {
                o1Part = Integer.parseInt(o1Split[i]);
            }
            Integer o2Part;
            if (o2Split[i].contains("-")) {
                specialEndCounter++;
                o2Part = Integer.parseInt(o2Split[i].substring(0, o2Split[i].lastIndexOf("-")));
            } else {
                o2Part = Integer.parseInt(o2Split[i]);
            }

            int compareTo = o1Part.compareTo(o2Part);
            if (compareTo == 0) {
                // check if there was one SNAPSHOT/RC1/beta and one release - release is
                // ofc newer despite the same number
                if (specialEndCounter == 1) {
                    if (o1Split[i].contains("-")) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (specialEndCounter == 2) {
                    final String endO1 = o1Split[i].substring(o1Split[i].lastIndexOf("-") + 1, o1Split[i].length());
                    final String endO2 = o2Split[i].substring(o2Split[i].lastIndexOf("-") + 1, o2Split[i].length());
                    // SNAPSHOT/RC1/beta lexicographically sort
                    return endO1.toLowerCase().compareTo(endO2.toLowerCase());
                } else {
                    // check another part
                    continue;
                }
            } else {
                return compareTo;
            }
        }

        Integer o1SplitLength = (Integer) o1Split.length;
        Integer o2SplitLength = (Integer) o2Split.length;
        return o1SplitLength.compareTo(o2SplitLength);
    }

}