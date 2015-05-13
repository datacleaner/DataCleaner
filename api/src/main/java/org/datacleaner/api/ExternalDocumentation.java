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
 * Annotation used to provide links to extra documentation (beyond
 * {@link Description} and similar annotations) about a {@link Component}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Documented
@Inherited
public @interface ExternalDocumentation {

    /**
     * Defines the types of {@link DocumentationLink}s that are available
     */
    public static enum DocumentationType {

        /**
         * Written reference documentation style documents.
         */
        REFERENCE,

        /**
         * Tutorials/use-cases that are explained to put the component in a
         * particular light.
         */
        TUTORIAL,

        /**
         * An video explaining the component.
         */
        VIDEO,

        /**
         * A technical background piece, typically for engineers or people with
         * a specialized interest in this component.
         */
        TECH
    }

    public static @interface DocumentationLink {

        /**
         * Gets the title/name of the documentation item.
         * 
         * @return
         */
        public String title();

        /**
         * Gets the HTTP(S) URL that this documentation item resides at
         * 
         * @return
         */
        public String url();

        /**
         * Gets the {@link DocumentationType} of this link
         * 
         * @return
         */
        public DocumentationType type();

        /**
         * Defines the version of DataCleaner that this documentation item was
         * based on
         * 
         * @return
         */
        public String version();
    }

    /**
     * Defines the external documentation links that are defined for this
     * component.
     * 
     * @return
     */
    public DocumentationLink[] value();
}
