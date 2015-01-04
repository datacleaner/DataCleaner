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

/**
 * Methods and fields with the @Provided annotation are used to let
 * AnalyzerBeans, TransformerBeans and FilterBeans retrieve service-objects such
 * as persistent collections, the current SchemaNavigator or DataContext.
 * 
 * This features ensures separation of concerns: The AnalyzerBeans framework
 * will make sure that persistence is handled and the bean-developer will not
 * have to worry about memory problems related to his/her collection(s).
 * 
 * Additionally Analyzerbeans can use the @Provided annotation to inject a
 * SchemaNavigator in order to perform metadata-based analysis. AnalyzerBeans
 * can also inject a DataContext, but this is generally discouraged for normal
 * use-cases since it will either be provided with the run(...)-method if the
 * AnalyzerBean is an ExploringAnalyzer or be out of scope if the AnalyzerBean
 * is a RowProcessingAnalyzer. For some use-cases it is helpful though, for
 * example initialization that requires some simple querying.
 * 
 * Valid types for @Provided annotated fields and method arguments are:
 * <ul>
 * <li>List</li>
 * <li>Set</li>
 * <li>Map</li>
 * <li>org.eobjects.analyzer.storage.CollectionFactory</li>
 * <li>org.eobjects.analyzer.storage.RowAnnotationFactory</li>
 * <li>org.eobjecta.analyzer.util.SchemaNavigator</li>
 * <li>org.apache.metamodel.DataContext</li>
 * </ul>
 * Generic/parameterized types for the List, Set or Map can be any of:
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
 * <li>Byte[] or byte[]</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
@Inherited
public @interface Provided {
}