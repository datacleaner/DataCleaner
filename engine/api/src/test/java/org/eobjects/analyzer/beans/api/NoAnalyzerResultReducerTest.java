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
package org.eobjects.analyzer.beans.api;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.eobjects.analyzer.result.AnalyzerResult;

import junit.framework.TestCase;

public class NoAnalyzerResultReducerTest extends TestCase {

    public void testThrowException() throws Exception {
        Constructor<?> constructor = NoAnalyzerResultReducer.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        NoAnalyzerResultReducer reducer = (NoAnalyzerResultReducer) constructor.newInstance();

        try {
            reducer.reduce(new ArrayList<AnalyzerResult>());
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
        }
    }
}
