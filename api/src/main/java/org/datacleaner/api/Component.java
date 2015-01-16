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

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Abstract interface for components in DataCleaner.
 * 
 * Usually components are annotated (at the class level) with the following
 * annotations.
 * 
 * All components must be annotated with {@link Named} at the class-level to be
 * discovered and exposed as functions that can be applied to a job.
 * 
 * Components usually has one (or more) methods that take an {@link InputRow} as
 * an argument. This is typically the main method of the component which will be
 * invoked repeatedly by the framework - once for each row in the data stream.
 * {@link Component}s are usually configured with various {@link InputColumn}s
 * which can be used as keys to retrieve values from the {@link InputRow}s.
 * 
 * If needed one or more aliases can be provided using {@link Alias}. This is
 * especially useful if renaming the main name of the component - then the old
 * name can be provided as an alias to retain backwards compatibility and
 * discoverability.
 * 
 * Furthermore a description for end users can be provided using
 * {@link Description}.
 * 
 * If the {@link Component} also implements the {@link HasLabelAdvice} interface
 * then this will be used to present a configuration-specific label towards the
 * end user. This often helps to recognize the component instance among other
 * instances.
 * 
 * The {@link Categorized} annotation helps to category the component into
 * logical groupings for the user to navigate.
 * 
 * The {@link Concurrent} annotation can be used to influence whether the
 * framework allows for concurrent (on same JVM) invocation of the component
 * during job execution.
 * 
 * The {@link Distributed} annotation determines whether a component allows to
 * be distributed across a cluster of JVM nodes.
 * 
 * Configuration of a component is provided via fields with {@link Inject} and
 * the {@link Configured} annotation.
 * 
 * Access to environment and context classes is possible via fields with the
 * {@link Inject} and {@link Provided} annotation.
 * 
 * Life-cycle methods can be added to the class if properly annotated with
 * {@link Validate}, {@link Initialize} and {@link Close}.
 */
public interface Component {

}
