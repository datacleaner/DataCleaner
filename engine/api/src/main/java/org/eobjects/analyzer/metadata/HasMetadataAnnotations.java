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

import java.util.List;

/**
 * Defines methods for objects that contain {@link MetadataAnnotation}s.
 */
public interface HasMetadataAnnotations {

    /**
     * Gets a {@link MetadataAnnotation} by name.
     * 
     * @param annotationName
     * @return the annotation, or null if no annotation with the particular name
     *         was found.
     */
    public MetadataAnnotation getAnnotation(String annotationName);

    /**
     * Adapts a particular annotation into a specialized object using a
     * {@link MetadataAnnotationAdaptor}.
     * 
     * @param annotationAdaptor
     * @return the specialized object, or null if no applicable annotation to
     *         adapt was found
     */
    public <M> M getAdaptedAnnotation(MetadataAnnotationAdaptor<M> annotationAdaptor);

    /**
     * Gets all available {@link MetadataAnnotation}.
     * 
     * @return
     */
    public List<MetadataAnnotation> getAnnotations();
}
