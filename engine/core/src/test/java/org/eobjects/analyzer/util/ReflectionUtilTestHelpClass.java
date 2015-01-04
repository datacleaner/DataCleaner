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
package org.eobjects.analyzer.util;

import org.junit.Ignore;

@Ignore
public class ReflectionUtilTestHelpClass {

    public static class ClassA {
        private int a;

        public int getA() {
            return a;
        }
    }

    public static class ClassB extends ClassA {
        private boolean b;

        public boolean getB() {
            return b;
        }

        @Override
        public int getA() {
            // override a method in ClassA
            return super.getA() + 0;
        }
    }
    
    public static class ClassC extends ClassA {
        private boolean c;

        public boolean getC() {
            return c;
        }
    }
}
