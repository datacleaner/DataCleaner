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
package org.eobjects.analyzer.beans.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.eobjects.analyzer.data.InputColumn;

/**
 * Annotation used for {@link Configured} properties that are mapped to another
 * {@link Configured} property.
 * 
 * Usually the way this works by indicating that two array properties are
 * connected/mapped together. One property would contain an array of
 * {@link InputColumn}s and the other would be an array of Strings, enums or
 * something else. The second would then be mapped to the first, making it
 * possible for each input column to "have" a String or a enum value mapped.
 * 
 * Another way that properties may be mapped is by hierarical structure or
 * dependency. For instance, a {@link ColumnProperty} may be mapped to a
 * {@link TableProperty} which indicates that the column should exist within the
 * table.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Inherited
@Qualifier
public @interface MappedProperty {

    /**
     * Defines the name of the other property that this property is mapped to.
     * 
     * @return
     */
    public String value();
}
