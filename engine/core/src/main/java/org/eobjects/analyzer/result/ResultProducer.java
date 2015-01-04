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
package org.eobjects.analyzer.result;


/**
 * Result producers produce AnalyzerResults. Typically they are used to
 * represent command-objects that produce a drill-to-detail/exploration style
 * result in a crosstab, chart or similar.
 * 
 * ResultProducers are not required to be Serializable, but if they are they
 * will be saved with the rest of the AnalyzerResult that contain them, if that
 * is persisted.
 */
public interface ResultProducer {

	public AnalyzerResult getResult();
}
