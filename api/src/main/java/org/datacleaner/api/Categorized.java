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

/**
 * Annotation used to assign a set of categories to a component.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Inherited
public @interface Categorized {

    /**
     * Defines the top-level component category. This categorization is very
     * broad and is normally inferred from the component type itself. Rather the
     * {@link #value()} is a more typical categorization to define, since it
     * defines the finer grained categorization of the components.
     * 
     * @return
     */
    public Class<? extends ComponentSuperCategory> superCategory() default ComponentSuperCategory.class;

    /**
     * Defines the category of component within the top-level structure as
     * defined by {@link #superCategory()}.
     * 
     * @return
     */
    public Class<? extends ComponentCategory>[] value() default ComponentCategory.class;
}
