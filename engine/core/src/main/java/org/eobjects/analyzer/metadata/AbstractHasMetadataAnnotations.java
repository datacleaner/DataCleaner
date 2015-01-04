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

import java.util.Collection;
import java.util.List;

import org.apache.metamodel.util.HasName;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * Abstract {@link HasMetadataAnnotations} implementation
 */
abstract class AbstractHasMetadataAnnotations implements HasMetadataAnnotations {

    private final ImmutableList<MetadataAnnotation> _annotations;

    public AbstractHasMetadataAnnotations(Collection<? extends MetadataAnnotation> annotations) {
        _annotations = ImmutableList.copyOf(annotations);
    }

    @Override
    public MetadataAnnotation getAnnotation(String annotationName) {
        if (Strings.isNullOrEmpty(annotationName)) {
            return null;
        }

        // first check: exact match
        for (MetadataAnnotation annotation : _annotations) {
            if (annotationName.equals(annotation.getName())) {
                return annotation;
            }
        }

        // second check: case insensitive match
        for (MetadataAnnotation annotation : _annotations) {
            if (annotationName.equalsIgnoreCase(annotation.getName())) {
                return annotation;
            }
        }

        return null;
    }

    @Override
    public <M> M getAdaptedAnnotation(MetadataAnnotationAdaptor<M> annotationAdaptor) {
        if (annotationAdaptor == null) {
            return null;
        }

        final String annotationName = annotationAdaptor.getAnnotationName();
        final MetadataAnnotation annotation = getAnnotation(annotationName);
        if (annotation == null) {
            return null;
        }

        final M result = annotationAdaptor.convertFromAnnotation(annotation);
        return result;
    }

    @Override
    public List<MetadataAnnotation> getAnnotations() {
        return _annotations;
    }

    protected <H extends HasName> H getByName(final String name, final Collection<? extends H> hasNames) {
        if (Strings.isNullOrEmpty(name)) {
            return null;
        }

        // exact match
        for (H hasName : hasNames) {
            if (name.equals(hasName.getName())) {
                return hasName;
            }
        }

        // case insensitive match
        for (H hasName : hasNames) {
            if (name.equalsIgnoreCase(hasName.getName())) {
                return hasName;
            }
        }

        return null;
    }

}
