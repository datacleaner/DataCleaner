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
package org.eobjects.analyzer.beans.referentialintegrity;

import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

@Description("Records with unresolved foreign key values")
public class ReferentialIntegrityAnalyzerResult extends AnnotatedRowsResult {

    private static final long serialVersionUID = 1L;

    public ReferentialIntegrityAnalyzerResult(RowAnnotation annotation, RowAnnotationFactory annotationFactory,
            InputColumn<?>[] highlightedColumns) {
        super(annotation, annotationFactory, highlightedColumns);
    }
}
