package org.eobjects.datacleaner.classloader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
 * The Extension Classloader insulates extensions from other extensions.
 * Extensions are shipped as single jars. A separate ExtensionClassLoader is
 * used to load each of these extension jars.
 * 
 * Class loading is done as follows: First, the globalParent classloader is used
 * to check if the class to load is present in the DataCleaner itself. Second,
 * the class is loaded by the extension classloader. This parent will contain
 * only classes present in the extension.
 * 
 */
public class ExtensionClassLoader extends ClassLoader {

    private static final char FORWARDSLASH = '/';

    /**
     * The globalParent loads all classes from the DataCleaner lib directory.
     */
    private ClassLoader globalParent;

    /**
     * A name for the class loader (convenient for logging).
     */
    private String name;

    /**
     * Parent ClassLoader passed to this constructor will be used if this
     * ClassLoader can not resolve a particular class. The class loader name
     * will be empty.
     * 
     * @param parent
     *            Parent ClassLoader (may be from getClass().getClassLoader())
     * @param globalParent
     *            This ClassLoader will be used to resolve resources that cannot
     *            be found using parent.
     */
    public ExtensionClassLoader(ClassLoader parent, ClassLoader globalParent) {
        this(parent, globalParent, "");
    }

    /**
     * Constructor with parameters for the global class loader, parent class
     * loader and a name.
     * 
     * @param parent
     *            The parent class loader is used for loading classes in the
     *            extension.
     * @param globalParent
     *            The global parent is used for resolving common classes.
     * @param name
     *            The name to use for the class loader. This is used for
     *            logging.
     */
    public ExtensionClassLoader(ClassLoader parent, ClassLoader globalParent, String name) {
        super(parent);
        this.globalParent = globalParent;
        if (name == null) {
            name = "";
        }
        this.name = name;

    }

    /**
     * Every request for a class passes through this method. If the requested
     * class is in "com.hi.general.classloader" package, it will load it using
     * the {@link CustomClassLoader#getClass()} method. If not, it will use the
     * super.loadClass() method which in turn will pass the request to the
     * parent.
     *
     * @param name
     *            Full class name
     */
    @Override
    public Class<?> loadClass(String name)
            throws ClassNotFoundException {
        return this.loadClass(name, false);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> class1 = null;
        try {
            class1 = globalParent.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            // Ignore - The global parent does not have this class.
        }
        if (class1 == null) {
            // already loaded?
            class1 = findLoadedClass(name);
        }
        if (class1 == null) {
            // Added for debugging.
            // System.out.println(this.name + ", loading class '" + name + "'");
            // No - Get it yourself
            class1 = getClass(name);
            if (class1 != null && resolve) {
                resolveClass(class1);
            }
        }
        return class1;
    }

    @Override
    public URL getResource(String name) {
        URL url = null;
        if (this.globalParent != null) {
            url = this.globalParent.getResource(name);
        }
        if (url == null && this.getParent() != null) {
            url = this.getParent().getResource(name);
        }
        return url;
    }

    @Override
    public String toString() {
        return "Extension classloader for: " + name;
    }

    /**
     * Loads a class.
     *
     * @param name
     *            Full class name
     */
    private Class<?> getClass(String name)
            throws ClassNotFoundException {
        String file = name.replace('.', FORWARDSLASH)
                + ".class";
        byte[] b = null;
        try {
            // This loads the byte code data from the file
            b = loadClassData(file);
            if (b == null) {
                return null;
            }
            // Define the package and the class.
            definePackageForClass(name);
            Class<?> c = defineClass(name, b, 0, b.length);

            return c;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void definePackageForClass(String name) {
        final int i = name.lastIndexOf('.');
        if (i != -1) {
            final String pkgname = name.substring(0, i);
            final Package pkg = getPackage(pkgname);
            if (pkg == null) {
                definePackage(pkgname, null, null, null, null, null, null, null);
            }
        }
    }

    /**
     * Loads a given file (presumably .class) into a byte array. The file should
     * be accessible as a resource.
     * 
     * This method uses getResourceAsStream() method. This will search for the
     * class in the parent class loader. This is normally an URLClassLoader
     * setup to search the extension JAR.
     *
     * @param name
     *            File name to load
     * @return Byte array read from the file
     * @throws IOException
     *             Is thrown when there was some problem reading the file
     */
    private byte[] loadClassData(String name) throws IOException {
        InputStream stream = getResourceAsStream(name);
        if (stream == null) {
            return null;
        }
        int size = stream.available();
        byte buff[] = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        in.readFully(buff);
        in.close();
        return buff;
    }
}
