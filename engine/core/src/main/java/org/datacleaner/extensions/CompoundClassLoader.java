/**
 * DataCleaner (community edition) Copyright (C) 2014 Neopost - Customer
 * Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to: Free Software Foundation,
 * Inc. 51 Franklin Street, Fifth Floor Boston, MA 02110-1301 USA
 * 
 */
package org.datacleaner.extensions;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This class loader scans a set of class loaders for class definitions.
 *
 */
public class CompoundClassLoader extends ClassLoader {

    private Collection<ClassLoader> loaders;

    /**
     * Constructor initializes the class loaders to scan.
     * 
     * @param loaders
     *            The class loaders that will be searched.
     */
    public CompoundClassLoader(Collection<ClassLoader> loaders) {
        this.loaders = new ArrayList<ClassLoader>(loaders);
    }

    @Override
    public Class<?> loadClass(String name)
            throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> class1 = null;
        if (class1 == null) {
            // already loaded?
            class1 = locateClass(name);
        }
        if (class1 == null) {
            throw new ClassNotFoundException("Could not resolve class: " + name);
        }
        return class1;
    }

    @Override
    public URL getResource(String name) {
        URL url = null;
        for (final ClassLoader cl : this.loaders) {
            if (url != null) {
                break;
            }
            url = cl.getResource(name);
        }
        return url;
    }

    private Class<?> locateClass(String name) {
        Class<?> result = null;
        for (final ClassLoader cl : this.loaders) {
            if (result != null) {
                break;
            }
            try {
                result = cl.loadClass(name);
            } catch (ClassNotFoundException e) {
                // Ignore - try the next class loader.
            }
        }
        return result;
    }

}
