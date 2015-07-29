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
package org.datacleaner.monitor.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.datacleaner.api.Component;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.monitor.server.crates.ComponentConfiguration;

import java.io.Serializable;

/**
 * Class ComponentConfigHolder
 * Object contains all information for execution components.
 *
 * @author k.houzvicka
 * @since 24.7.15
 */
public class ComponentConfigHolder implements Serializable {
    long timeoutMs;
    ComponentConfiguration configuration;
    String componentId;

    @JsonIgnore
    ComponentDescriptor descriptor;

    @JsonIgnore
    LifeCycleHelper lifeCycleHelper;

    @JsonIgnore
    Component component;

    public ComponentConfigHolder() {
    }

    public ComponentConfigHolder(long timeoutMs, String componentId, ComponentConfiguration configuration,
                                 ComponentDescriptor descriptor, LifeCycleHelper lifeCycleHelper, Component component) {
        this.timeoutMs = timeoutMs;
        this.configuration = configuration;
        this.componentId = componentId;
        this.component = component;
        this.descriptor = descriptor;
        this.lifeCycleHelper = lifeCycleHelper;
    }

    public void close() {
        if (lifeCycleHelper != null) {
            lifeCycleHelper.close(descriptor, component, true);
        }
    }


    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public ComponentConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ComponentConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public ComponentDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(ComponentDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public LifeCycleHelper getLifeCycleHelper() {
        return lifeCycleHelper;
    }

    public void setLifeCycleHelper(LifeCycleHelper lifeCycleHelper) {
        this.lifeCycleHelper = lifeCycleHelper;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }
}