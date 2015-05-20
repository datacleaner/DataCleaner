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
package org.datacleaner.util;

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
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultReducer;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Metric;
import org.datacleaner.api.Renderable;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.descriptors.MetricDescriptor;
import org.datacleaner.job.ComponentConfiguration;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.reference.TextFileDictionary;
import org.datacleaner.reference.TextFileSynonymCatalog;
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
        INTERFACES_WITH_SERIAL_ID_CHANGES.add("org.datacleaner.beans.writers.WriteDataResult");
    }

    private static final Comparator<String> packageNameComparator = new Comparator<String>() {
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
        renamedPackages = new TreeMap<String, String>(packageNameComparator);
        renamedClasses = new HashMap<String, String>();
        additionalClassLoaders = new ArrayList<ClassLoader>();

        // add analyzerbeans' own renamed classes
        addRenamedClass("org.datacleaner.reference.TextBasedDictionary", TextFileDictionary.class);
        addRenamedClass("org.datacleaner.reference.TextBasedSynonymCatalog", TextFileSynonymCatalog.class);

        // analyzer results moved as of ticket #843
        addRenamedClass("org.datacleaner.result.PatternFinderResult",
                "org.datacleaner.beans.stringpattern.PatternFinderResult");
        addRenamedClass("org.datacleaner.result.DateGapAnalyzerResult",
                "org.datacleaner.beans.dategap.DateGapAnalyzerResult");
        addRenamedClass("org.datacleaner.util.TimeInterval", "org.datacleaner.beans.dategap.TimeInterval");
        addRenamedClass("org.datacleaner.result.StringAnalyzerResult", "org.datacleaner.beans.StringAnalyzerResult");
        addRenamedClass("org.datacleaner.result.NumberAnalyzerResult", "org.datacleaner.beans.NumberAnalyzerResult");
        addRenamedClass("org.datacleaner.result.BooleanAnalyzerResult", "org.datacleaner.beans.BooleanAnalyzerResult");
        addRenamedClass("org.datacleaner.result.DateAndTimeAnalyzerResult",
                "org.datacleaner.beans.DateAndTimeAnalyzerResult");

        // analyzer results moved as of ticket #993
        addRenamedClass("org.datacleaner.result.ValueDistributionGroupResult",
                "org.datacleaner.beans.valuedist.SingleValueDistributionResult");
        addRenamedClass("org.datacleaner.result.ValueDistributionResult",
                "org.datacleaner.beans.valuedist.GroupedValueDistributionResult");
        addRenamedClass("org.datacleaner.beans.valuedist.ValueDistributionGroupResult",
                "org.datacleaner.beans.valuedist.SingleValueDistributionResult");
        addRenamedClass("org.datacleaner.beans.valuedist.ValueDistributionResult",
                "org.datacleaner.beans.valuedist.GroupedValueDistributionResult");
        addRenamedClass("org.datacleaner.beans.valuedist.ValueCount", "org.datacleaner.result.SingleValueFrequency");
        addRenamedClass("org.datacleaner.result.ValueCount", "org.datacleaner.result.SingleValueFrequency");
        addRenamedClass("org.datacleaner.beans.valuedist.ValueCountList", "org.datacleaner.result.ValueCountList");
        addRenamedClass("org.datacleaner.beans.valuedist.ValueCountListImpl",
                "org.datacleaner.result.ValueCountListImpl");

        // duplicate detection analyzer changed
        final String duplicateDetectionClassName = "com.hi.hiqmr.packaging.datacleaner.deduplication.DuplicateDetectionAnalyzer";
        addRenamedClass("com.hi.contacts.datacleaner.DuplicateDetectionAnalyzer", duplicateDetectionClassName);
        addRenamedClass("com.hi.hiqmr.datacleaner.deduplication.Identify7DeduplicationAnalyzer",
                duplicateDetectionClassName);
        addRenamedClass("com.hi.hiqmr.datacleaner.deduplication.DuplicateDetectionAnalyzer",
                duplicateDetectionClassName);
        addRenamedClass("com.hi.hiqmr.deduplication.DuplicateDetectionAnalyzer", duplicateDetectionClassName);

        addRenamedPackage("com.hi.contacts.security", "com.hi.common.client.security");

        // Classes moved in DC 4.0
        addRenamedClass("org.datacleaner.data.InputRow", InputRow.class);
        addRenamedClass("org.datacleaner.data.InputColumn", InputColumn.class);
        addRenamedClass("org.datacleaner.result.Metric", Metric.class);
        addRenamedClass("org.datacleaner.job.BeanConfiguration", ComponentConfiguration.class);
        addRenamedClass("org.datacleaner.job.ImmutableBeanConfiguration", ImmutableComponentConfiguration.class);
        addRenamedClass("org.datacleaner.descriptors.AnnotationBasedAnalyzerBeanDescriptor",
                "org.datacleaner.descriptors.AnnotationBasedAnalyzerComponentDescriptor");
        addRenamedClass("org.datacleaner.descriptors.AnnotationBasedTransformerBeanDescriptor",
                "org.datacleaner.descriptors.AnnotationBasedTransformerComponentDescriptor");
        addRenamedClass("org.datacleaner.descriptors.AnnotationBasedFilterBeanDescriptor",
                "org.datacleaner.descriptors.AnnotationBasedFilterComponentDescriptor");
        addRenamedClass("org.datacleaner.descriptors.AbstractHasAnalyzerResultBeanDescriptor",
                "org.datacleaner.descriptors.AbstractHasAnalyzerResultComponentDescriptor");
        addRenamedClass("org.datacleaner.descriptors.AbstractBeanDescriptor",
                "org.datacleaner.descriptors.AbstractComponentDescriptor");

        addRenamedClass("org.eobjects.analyzer.result.AnalyzerResult", AnalyzerResult.class);
        addRenamedClass("org.eobjects.analyzer.result.AnalyzerResultReducer", AnalyzerResultReducer.class);
        addRenamedClass("org.eobjects.analyzer.result.HasAnalyzerResult", HasAnalyzerResult.class);
        addRenamedClass("org.eobjects.analyzer.result.Metric", Metric.class);
        addRenamedClass("org.eobjects.analyzer.result.renderer.Renderable", Renderable.class);
        addRenamedClass("org.eobjects.analyzer.util.SchemaNavigator", SchemaNavigator.class);

        // General namespace change as of DC 4.0
        addRenamedPackage("org.eobjects.datacleaner", "org.datacleaner");
        addRenamedPackage("org.eobjects.analyzer", "org.datacleaner");
        addRenamedPackage("org.datacleaner.beans.api", "org.datacleaner.api");
        addRenamedPackage("org.datacleaner.beans.categories", "org.datacleaner.components.categories");

        // Change from eobjects.org MetaModel to Apache MetaModel
        addRenamedPackage("org.eobjects.metamodel", "org.apache.metamodel");

        // DataCleaner output writers package changed
        addRenamedPackage("org.datacleaner.output.beans", "org.datacleaner.extension.output");
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

        final String className = getClassNameRenamed(originalClassName);
        if (className != originalClassName) {
            return getClassDescriptor(className, false, resultClassDescriptor);
        }

        if (INTERFACES_WITH_SERIAL_ID_CHANGES.contains(originalClassName)) {
            final ObjectStreamClass newClassDescriptor = ObjectStreamClass
                    .lookup(resolveClass(originalClassName, false));
            return newClassDescriptor;
        }

        return resultClassDescriptor;
    }

    private ObjectStreamClass getClassDescriptor(final String className, final boolean checkRenames,
            final ObjectStreamClass originalClassDescriptor) throws ClassNotFoundException {

        if (originalClassDescriptor == null) {
            logger.warn("Original ClassDescriptor resolved to null for '{}'", className);
        }

        final Class<?> newClass = resolveClass(className, checkRenames);
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
        return resolveClass(className, true);
    }

    private Class<?> resolveClass(final String classNameParameter, boolean checkRenames) throws ClassNotFoundException {
        logger.debug("Resolving class '{}'", classNameParameter);

        final String className;
        if (checkRenames) {
            className = getClassNameRenamed(classNameParameter);
        } else {
            className = classNameParameter;
        }

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

    private String getClassNameRenamed(String className) {
        return getClassNameRenamed(className, true);
    }

    private String getClassNameRenamed(String className, boolean includeRenamedPackages) {
        // handle array definitions
        if (className.startsWith("[L")) {
            final String classNameWithoutArrayDef = className.substring(2, className.length() - 1);
            return "[L" + getClassNameRenamed(classNameWithoutArrayDef) + ";";
        }

        // handle direct entries for renamed class
        final String directlyRenamedClassName = renamedClasses.get(className);
        if (directlyRenamedClassName != null) {
            logger.info("Class '{}' was encountered. Returning new class name: '{}'", className,
                    directlyRenamedClassName);
            return directlyRenamedClassName;
        }

        if (includeRenamedPackages) {
            // handle renamed packages
            final Set<Entry<String, String>> entrySet = renamedPackages.entrySet();
            for (Entry<String, String> entry : entrySet) {
                final String legacyPackage = entry.getKey();
                if (className.startsWith(legacyPackage)) {
                    final String renamedClassName = className.replaceFirst(legacyPackage, entry.getValue());
                    logger.info("Class '{}' was encountered. Adapting to new class name: '{}'", className,
                            renamedClassName);
                    return getClassNameRenamed(renamedClassName, includeRenamedPackages);
                }
            }
        }

        // ok no rename happened
        return className;
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
