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
package org.datacleaner.descriptors;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;

import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.mock.AnalyzerMock;
import org.datacleaner.test.MockFilter;

import junit.framework.TestCase;

public class ConfiguredPropertyDescriptorImplTest extends TestCase {

    @Configured
    String str1;
    @Configured
    String str2;
    @Configured
    InputColumn<byte[]> someBytes;
    @Configured
    InputColumn<String>[] stringColumns;
    private FilterDescriptor<MockFilter, MockFilter.Category> _descriptor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _descriptor = Descriptors.ofFilter(MockFilter.class);
    }

    public void testGetTypeParameterOfArrayType1() throws Exception {
        final ConfiguredPropertyDescriptor descriptor =
                new ConfiguredPropertyDescriptorImpl(getClass().getDeclaredField("someBytes"), null);
        assertEquals(1, descriptor.getTypeArgumentCount());
        final Class<?> typeArgument = descriptor.getTypeArgument(0);
        assertEquals(byte[].class, typeArgument);
    }

    public void testGetTypeParameterOfArrayType2() throws Exception {
        final ConfiguredPropertyDescriptor descriptor =
                new ConfiguredPropertyDescriptorImpl(getClass().getDeclaredField("stringColumns"), null);
        assertEquals(1, descriptor.getTypeArgumentCount());
        final Class<?> typeArgument = descriptor.getTypeArgument(0);
        assertEquals(String.class, typeArgument);
    }

    public void testCompareTo() throws Exception {
        final Set<ConfiguredPropertyDescriptor> properties =
                Descriptors.ofAnalyzer(AnalyzerMock.class).getConfiguredProperties();
        assertEquals(4, properties.size());
        final Iterator<ConfiguredPropertyDescriptor> it = properties.iterator();
        assertTrue(it.hasNext());
        assertEquals("Columns", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Configured1", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Configured2", it.next().getName());
        assertTrue(it.hasNext());
        assertEquals("Some string property", it.next().getName());
        assertFalse(it.hasNext());

        final Field f1 = getClass().getDeclaredField("str1");
        final Field f2 = getClass().getDeclaredField("str2");

        final ConfiguredPropertyDescriptorImpl d1 = new ConfiguredPropertyDescriptorImpl(f1, null);
        final ConfiguredPropertyDescriptorImpl d2 = new ConfiguredPropertyDescriptorImpl(f2, null);
        assertTrue(d1.compareTo(d2) < 0);
    }

    public void testToString() throws Exception {
        final ConfiguredPropertyDescriptor cp = _descriptor.getConfiguredProperty("Some enum");
        assertEquals("ConfiguredPropertyDescriptorImpl[name=Some enum]", cp.toString());
    }

    public void testEnum() throws Exception {
        final Set<ConfiguredPropertyDescriptor> properties = _descriptor.getConfiguredProperties();
        assertEquals(3, properties.size());

        final ConfiguredPropertyDescriptor cp = _descriptor.getConfiguredProperty("Some enum");
        assertFalse(cp.isArray());
        assertTrue(cp.getType().isEnum());

        final MockFilter filter = new MockFilter();
        assertNull(filter.getSomeEnum());
        cp.setValue(filter, MockFilter.Category.VALID);
        assertEquals(MockFilter.Category.VALID, filter.getSomeEnum());
    }

    public void testFile() throws Exception {
        final ConfiguredPropertyDescriptor cp = _descriptor.getConfiguredProperty("Some file");
        assertFalse(cp.isArray());
        assertTrue(cp.getType() == File.class);

        final MockFilter filter = new MockFilter();
        assertNull(filter.getSomeFile());
        cp.setValue(filter, new File("."));
        assertEquals(new File("."), filter.getSomeFile());
    }

    public void testGetConfiguredPropertyByAlias() throws Exception {
        final ConfiguredPropertyDescriptor cp1 = _descriptor.getConfiguredProperty("Some file");
        final ConfiguredPropertyDescriptor cp2 = _descriptor.getConfiguredProperty("a file");
        assertSame(cp1, cp2);
    }

}
