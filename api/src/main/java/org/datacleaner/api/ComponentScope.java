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
 * Annotation used to provide additional information about available services
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Inherited
public @interface ComponentScope {

    String ALL_COUNTRIES = "all";

    enum ServiceType {
        ENRICH, CORRECTION
    }

    enum EntityType {
        PERSON, COMPANY
    }

    /**
     * Specifies the type of component. Enrich or correction
     *
     * @return array with enum ENRICH, CORRECTION
     */
    ServiceType[] serviceTypes() default {};

    /**
     * Component is working with data about PEOPLE or COMPANY.
     *
     * @return array with enum  PERSON, COMPANY
     */
    EntityType[] entityTypes() default {};

    /**
     * Array of Countries, where is component usable.
     *
     * @return array of countries
     */
    String[] countries() default { ALL_COUNTRIES };
}
