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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.metamodel.util.EqualsBuilder;
import org.apache.metamodel.util.HasName;
import org.apache.metamodel.util.LegacyDeserializationObjectInputStream;
import org.eobjects.analyzer.beans.api.ComponentCategory;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.MetricDescriptor;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.reference.TextFileDictionary;
import org.eobjects.analyzer.reference.TextFileSynonymCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ObjectInputStream} implementation that is aware of changes such as
 * class or package renaming. This can be used to deserialize classes with
 * historic/legacy class names.
 * 
 * Furthermore the deserialization mechanism is aware of multiple
 * {@link ClassLoader}s. This means that if the object being deserialized
 * pertains to a different {@link ClassLoader}, then this classloader can be
 * added using the {@link #addClassLoader(ClassLoader)} method.
 * 
 * 
 */
public class ChangeAwareObjectInputStream extends LegacyDeserializationObjectInputStream {

    private static final Logger logger = LoggerFactory.getLogger(ChangeAwareObjectInputStream.class);

    /**
     * Table mapping primitive type names to corresponding class objects. As
     * defined in {@link ObjectInputStream}.
     */
    private static final Map<String, Class<?>> PRIMITIVE_CLASSES = new HashMap<String, Class<?>>(8, 1.0F);

    /**
     * Since the change from eobjects.org MetaModel to Apache MetaModel, a lot
     * of interfaces (especially those that extend {@link HasName}) have
     * transparently changed their serialization IDs.
     */
    private static final Set<String> INTERFACES_WITH_SERIAL_ID_CHANGES = new HashSet<String>();

    static {
        PRIMITIVE_CLASSES.put("boolean", boolean.class);
        PRIMITIVE_CLASSES.put("byte", byte.class);
        PRIMITIVE_CLASSES.put("char", char.class);
        PRIMITIVE_CLASSES.put("short", short.class);
        PRIMITIVE_CLASSES.put("int", int.class);
        PRIMITIVE_CLASSES.put("long", long.class);
        PRIMITIVE_CLASSES.put("float", float.class);
        PRIMITIVE_CLASSES.put("double", double.class);
        PRIMITIVE_CLASSES.put("void", void.class);

        INTERFACES_WITH_SERIAL_ID_CHANGES.add(InputColumn.class.getName());
        INTERFACES_WITH_SERIAL_ID_CHANGES.add(ComponentJob.class.getName());
        INTERFACES_WITH_SERIAL_ID_CHANGES.add(Datastore.class.getName());
        INTERFACES_WITH_SERIAL_ID_CHANGES.add(MetricDescriptor.class.getName());
        INTERFACES_WITH_SERIAL_ID_CHANGES.add(PropertyDescriptor.class.getName());
        INTERFACES_WITH_SERIAL_ID_CHANGES.add(ComponentCategory.class.getName());
        INTERFACES_WITH_SERIAL_ID_CHANGES.add("org.eobjects.analyzer.beans.writers.WriteDataResult");
    }

    private static final Comparator<String> comparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            if (EqualsBuilder.equals(o1, o2)) {
                return 0;
            }
            // use length as the primary differentiator, to make sure long
            // packages are placed before short ones.
            int diff = o1.length() - o2.length();
            if (diff == 0) {
                diff = o1.compareTo(o2);
            }
            return diff;
        }
    };

    private final List<ClassLoader> additionalClassLoaders;
    private final Map<String, String> renamedPackages;
    private final Map<String, String> renamedClasses;

    public ChangeAwareObjectInputStream(InputStream in) throws IOException {
        super(in);
        renamedPackages = new TreeMap<String, String>(comparator);
        renamedClasses = new HashMap<String, String>();
        additionalClassLoaders = new ArrayList<ClassLoader>();

        // add analyzerbeans' own renamed classes
        addRenamedClass("org.eobjects.analyzer.reference.TextBasedDictionary", TextFileDictionary.class);
        addRenamedClass("org.eobjects.analyzer.reference.TextBasedSynonymCatalog", TextFileSynonymCatalog.class);

        // analyzer results moved as of ticket #843
        addRenamedClass("org.eobjects.analyzer.result.PatternFinderResult",
                "org.eobjects.analyzer.beans.stringpattern.PatternFinderResult");
        addRenamedClass("org.eobjects.analyzer.result.DateGapAnalyzerResult",
                "org.eobjects.analyzer.beans.dategap.DateGapAnalyzerResult");
        addRenamedClass("org.eobjects.analyzer.util.TimeInterval", "org.eobjects.analyzer.beans.dategap.TimeInterval");
        addRenamedClass("org.eobjects.analyzer.result.StringAnalyzerResult",
                "org.eobjects.analyzer.beans.StringAnalyzerResult");
        addRenamedClass("org.eobjects.analyzer.result.NumberAnalyzerResult",
                "org.eobjects.analyzer.beans.NumberAnalyzerResult");
        addRenamedClass("org.eobjects.analyzer.result.BooleanAnalyzerResult",
                "org.eobjects.analyzer.beans.BooleanAnalyzerResult");
        addRenamedClass("org.eobjects.analyzer.result.DateAndTimeAnalyzerResult",
                "org.eobjects.analyzer.beans.DateAndTimeAnalyzerResult");

        // analyzer results moved as of ticket #993
        addRenamedClass("org.eobjects.analyzer.result.ValueDistributionGroupResult",
                "org.eobjects.analyzer.beans.valuedist.SingleValueDistributionResult");
        addRenamedClass("org.eobjects.analyzer.result.ValueDistributionResult",
                "org.eobjects.analyzer.beans.valuedist.GroupedValueDistributionResult");
        addRenamedClass("org.eobjects.analyzer.beans.valuedist.ValueDistributionGroupResult",
                "org.eobjects.analyzer.beans.valuedist.SingleValueDistributionResult");
        addRenamedClass("org.eobjects.analyzer.beans.valuedist.ValueDistributionResult",
                "org.eobjects.analyzer.beans.valuedist.GroupedValueDistributionResult");
        addRenamedClass("org.eobjects.analyzer.beans.valuedist.ValueCount",
                "org.eobjects.analyzer.result.SingleValueFrequency");
        addRenamedClass("org.eobjects.analyzer.result.ValueCount", "org.eobjects.analyzer.result.SingleValueFrequency");
        addRenamedClass("org.eobjects.analyzer.beans.valuedist.ValueCountList",
                "org.eobjects.analyzer.result.ValueCountList");
        addRenamedClass("org.eobjects.analyzer.beans.valuedist.ValueCountListImpl",
                "org.eobjects.analyzer.result.ValueCountListImpl");

        // duplicate detection analyzer changed
        addRenamedClass("com.hi.contacts.datacleaner.DuplicateDetectionAnalyzer",
                "com.hi.hiqmr.datacleaner.deduplication.Identify7DeduplicationAnalyzer");
        
        // DataCleaner output writers package changed
        addRenamedPackage("org.eobjects.datacleaner.output.beans", "org.eobjects.datacleaner.extension.output");
    }

    public void addClassLoader(ClassLoader classLoader) {
        additionalClassLoaders.add(classLoader);
    }

    public void addRenamedPackage(String originalPackageName, String newPackageName) {
        renamedPackages.put(originalPackageName, newPackageName);
    }

    public void addRenamedClass(String originalClassName, Class<?> newClass) {
        addRenamedClass(originalClassName, newClass.getName());
    }

    public void addRenamedClass(String originalClassName, String newClassName) {
        renamedClasses.put(originalClassName, newClassName);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        final ObjectStreamClass resultClassDescriptor = super.readClassDescriptor();

        final String originalClassName = resultClassDescriptor.getName();

        if (renamedClasses.containsKey(originalClassName)) {
            final String className = renamedClasses.get(originalClassName);
            logger.info("Class '{}' was encountered. Returning class descriptor of new class name: '{}'",
                    originalClassName, className);
            return getClassDescriptor(className, resultClassDescriptor);
        } else {
            final Set<Entry<String, String>> entrySet = renamedPackages.entrySet();
            for (Entry<String, String> entry : entrySet) {
                final String legacyPackage = entry.getKey();
                if (originalClassName.startsWith(legacyPackage)) {
                    final String className = originalClassName.replaceFirst(legacyPackage, entry.getValue());
                    logger.info("Class '{}' was encountered. Returning class descriptor of new class name: '{}'",
                            originalClassName, className);
                    return getClassDescriptor(className, resultClassDescriptor);
                }
            }
        }

        if (INTERFACES_WITH_SERIAL_ID_CHANGES.contains(originalClassName)) {
            final ObjectStreamClass newClassDescriptor = ObjectStreamClass.lookup(resolveClass(originalClassName));
            return newClassDescriptor;
        }

        return resultClassDescriptor;
    }

    private ObjectStreamClass getClassDescriptor(final String className, final ObjectStreamClass originalClassDescriptor)
            throws ClassNotFoundException {
        
        if (originalClassDescriptor == null) {
            logger.warn("Original ClassDescriptor resolved to null for '{}'", className);
        }

        final Class<?> newClass = resolveClass(className);
        final ObjectStreamClass newClassDescriptor = ObjectStreamClass.lookupAny(newClass);
        if (newClassDescriptor == null) {
            logger.warn("New ClassDescriptor resolved to null for {}", newClass);
        }
        
        final String[] newFieldNames = getFieldNames(newClassDescriptor);
        final String[] originalFieldNames = getFieldNames(originalClassDescriptor);
        if (!EqualsBuilder.equals(originalFieldNames, newFieldNames)) {
            logger.warn("Field names of original and new class ({}) does not correspond!", className);

            // try to hack our way out of it by changing the value of the "name"
            // field in the ORIGINAL descriptor
            try {
                Field field = ObjectStreamClass.class.getDeclaredField("name");
                assert field != null;
                assert field.getType() == String.class;
                field.setAccessible(true);
                field.set(originalClassDescriptor, className);
                return originalClassDescriptor;
            } catch (Exception e) {
                logger.error("Unsuccesful attempt at changing the name of the original class descriptor");
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException(e);
            }
        }
        return newClassDescriptor;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        final String className = desc.getName();
        if (className.startsWith("org.eobjects.metamodel") || className.startsWith("[Lorg.eobjects.metamodel")) {
            return super.resolveClass(desc);
        }
        return resolveClass(className);
    }

    private Class<?> resolveClass(String className) throws ClassNotFoundException {
        logger.debug("Resolving class '{}'", className);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            final Class<?> primitiveClass = PRIMITIVE_CLASSES.get(className);
            if (primitiveClass != null) {
                return primitiveClass;
            }

            logger.info("Class '{}' was not resolved in main class loader.", className);
            final List<Exception> exceptions = new ArrayList<Exception>(additionalClassLoaders.size());
            for (ClassLoader classLoader : additionalClassLoaders) {
                try {
                    return Class.forName(className, true, classLoader);
                } catch (ClassNotFoundException minorException) {
                    logger.info("Class '{}' was not resolved in additional class loader '{}'", className, classLoader);
                    exceptions.add(minorException);
                }
            }

            logger.warn("Could not resolve class of name '{}'", className);

            // if we reach this stage, all classloaders have failed, log their
            // issues
            int i = 1;
            for (final Exception exception : exceptions) {
                int numExceptions = exceptions.size();
                logger.error("Exception " + i + " of " + numExceptions, exception);
                i++;
            }

            throw e;
        }
    }

    private String[] getFieldNames(ObjectStreamClass classDescriptor) {
        if (classDescriptor == null) {
            return new String[0];
        }
        final ObjectStreamField[] fields = classDescriptor.getFields();
        final String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fieldNames.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }
}
