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

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.apache.metamodel.util.Resource;

/**
 * Annotation containing supplementary metadata about a {@link File} or
 * {@link Resource} property. This metadata can be used as a way to give hints
 * to the UI as to how the content should be presented.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Inherited
@Qualifier
public @interface FileProperty {

    /**
     * Defines the access modes available for a file property
     */
    public static enum FileAccessMode {
        OPEN, SAVE;
    }

    /**
     * Defines whether or not the property is used to open an existing file or
     * to specify a file to save.
     * 
     * @return the access mode of this file property
     */
    public FileAccessMode accessMode() default FileAccessMode.OPEN;

    /**
     * Defines an (array of) file extension to suggest/expect. Extensions will
     * be automatically prefixed with a dot and are case insensitive. Examples:
     * "csv", "xls", "txt" etc.
     * 
     * @return the expected file extension of this file property
     */
    public String[] extension() default {};
}
