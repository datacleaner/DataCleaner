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
package org.datacleaner.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;

/**
 * Fields with the {@link Inject} and {@link Configured} annotation are used to
 * configure a {@link Component} before execution. Typically, the
 * {@link Configured} annotated fields will be used to prompt the user for
 * configuration in the UI or job definition that is instructing the framework
 * 
 * In principle any field type can be annotated with {@link Configured}. For
 * serialization and deserialization purposes it may be needed with a
 * {@link Convertable} annotation as well.
 * 
 * In the list of classes below there's a reference of the types that do not
 * need any {@link Convertable} annotation. Furthermore arrays of all these
 * types are supported:
 * <ul>
 * <li>Boolean</li>
 * <li>Byte</li>
 * <li>Short</li>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Character</li>
 * <li>String</li>
 * <li>java.io.File</li>
 * <li>enum types</li>
 * <li>java.util.regex.Pattern</li>
 * <li>org.datacleaner.data.InputColumn</li>
 * <li>org.datacleaner.reference.Dictionary</li>
 * <li>org.datacleaner.reference.SynonymCatalog</li>
 * <li>org.datacleaner.reference.StringPattern</li>
 * <li>org.apache.metamodel.schema.Column</li>
 * <li>org.apache.metamodel.schema.Table</li>
 * <li>org.apache.metamodel.schema.Schema</li>
 * </ul>
 * 
 * In addition to the name of the {@link Configured} property (provided via
 * {@link #value()}) a number of aliases can be provided via the {@link Alias}
 * annotation. This is particularly useful when renaming properties - adding an
 * alias with the old names will help retain backwards compatibility.
 * 
 * Details of the property can be provided to the end user via the
 * {@link Description} annotation.
 * 
 * Fields may also be annotated with {@link StringProperty},
 * {@link NumberProperty}, {@link ColumnProperty}, {@link TableProperty},
 * {@link SchemaProperty} or {@link FileProperty}. These annotations provide
 * extra type-specific metadata relevant for corresponding property types.
 *
 * If a property represents an array, and this array is mapped to another
 * configured array, then the {@link MappedProperty} annotation can be applied
 * to indicate this relationship.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Inherited
@Qualifier
public @interface Configured {

    /**
     * Defines the name of the required configuration property.
     * 
     * @return the name of the configuration property
     */
    String value() default "";

    /**
     * Defines whether or not this configured property is required
     * 
     * @return true if the configured property is required
     */
    boolean required() default true;

    /**
     * Defines the display order of this configured property, relative to other
     * properties.
     * 
     * @return the order (if any) of this configured property when sorting
     *         properties of a component. A low order will place the property
     *         before higher order properties.
     */
    int order() default Integer.MAX_VALUE;
}
