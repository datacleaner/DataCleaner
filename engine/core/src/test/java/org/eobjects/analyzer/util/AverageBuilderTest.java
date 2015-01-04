/**
 * AnalyzerBeans
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
/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eobjects.analyzer.util;

import junit.framework.TestCase;

public class AverageBuilderTest extends TestCase {

    public void testAddValue() throws Exception {
        assertEquals(3d, new AverageBuilder().addValue(2).addValue(4).getAverage());
        assertEquals(4.5d, new AverageBuilder().addValue(2).addValue(7).getAverage());
        assertEquals(4d, new AverageBuilder().addValue(2).addValue(4).addValue(4).addValue(6).addValue(3).addValue(5)
                .getAverage());
    }

    public void testAddValueWithCount() throws Exception {
        assertEquals(4d, new AverageBuilder().addValue(2, 1).addValue(4, 2).addValue(6).addValue(3, 1).addValue(5)
                .getAverage());
        
        AverageBuilder builder = new AverageBuilder();
        builder.addValue(400d, 500000);
        builder.addValue(300d, 500000);
        
        assertEquals(350d, builder.getAverage());
    }
}
