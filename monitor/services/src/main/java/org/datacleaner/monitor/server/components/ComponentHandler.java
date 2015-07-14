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
package org.datacleaner.monitor.server.components;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.monitor.server.crates.ComponentConfiguration;
import org.datacleaner.monitor.server.crates.ComponentDataInput;
import org.datacleaner.monitor.server.crates.ComponentStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is a component type independent wrapper that decides the proper handler and provides its results.
 * @author j.horcicka (GMC)
 * @since 14. 07. 2015
 */
public class ComponentHandler {
    private AtomicInteger idGenerator = new AtomicInteger(0);
    private DataCleanerConfiguration dcConfiguration;
    private ComponentDataInput componentDataInput;

    public ComponentHandler(DataCleanerConfiguration dcConfiguration, ComponentDataInput componentDataInput) {
        this.dcConfiguration = dcConfiguration;
        this.componentDataInput = componentDataInput;
        componentDataInput.init();
        componentDataInput.getConfiguration().setStatus(ComponentStatus.CREATED);
        componentDataInput.getConfiguration().setId(idGenerator.getAndIncrement());
    }

    public Serializable getResults() {
        ComponentResultsProvider componentResultsProvider = new TransformerProvider(dcConfiguration, componentDataInput);
        String componentName = componentDataInput.getConfiguration().getComponentName();
        Serializable results = null;

        if (componentResultsProvider.exists(componentName)) {
            componentDataInput.getConfiguration().setComponentType(ComponentConfiguration.ComponentType.TRANSFORMER);
            results = componentResultsProvider.getComponentResults();
        }
        else {
            componentResultsProvider = new AnalyzerProvider(dcConfiguration, componentDataInput);

            if (componentResultsProvider.exists(componentName)) {
                componentDataInput.getConfiguration().setComponentType(ComponentConfiguration.ComponentType.ANALYZER);
                results = componentResultsProvider.getComponentResults();
            }
        }

        return results;
    }
}
