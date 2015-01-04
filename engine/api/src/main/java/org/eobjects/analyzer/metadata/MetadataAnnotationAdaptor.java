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
package org.eobjects.analyzer.metadata;

/**
 * Component with responsibility to adapt a particular type of
 * {@link MetadataAnnotation} into a specialized class which is easier to
 * consume and use for specialized use-cases.
 * 
 * @param <T>
 *            the object type that will be converted to/from the
 *            {@link MetadataAnnotation}.
 */
public interface MetadataAnnotationAdaptor<T> {

    /**
     * Gets the name of the annotation that this adaptor will be adapting
     * to/from.
     * 
     * @return
     */
    public String getAnnotationName();

    /**
     * Converts a {@link MetadataAnnotation} object into a specialized object.
     * 
     * @param annotation
     * @return
     */
    public T convertFromAnnotation(MetadataAnnotation annotation);

    /**
     * Converts a specialized object back to a {@link MetadataAnnotation}
     * object.
     * 
     * @param object
     * @return
     */
    public MetadataAnnotation convertToAnnotation(T object);
}
