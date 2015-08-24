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
import org.datacleaner.monitor.server.components.ComponentHandler;

import java.io.Serializable;

/**
 * Class ComponentConfigHolder
 * Object contains all information for execution components.
 *
 * @since 24.7.15
 */
public class ComponentConfigHolder implements Serializable {

    long timeoutMs;
    CreateInput createInput;
    String componentId;
    String componentName;

    @JsonIgnore
    ComponentHandler handler;

    public ComponentConfigHolder() {
    }

    public ComponentConfigHolder(long timeoutMs, CreateInput createInput, String componentId, String componentName, ComponentHandler handler) {
        this.timeoutMs = timeoutMs;
        this.createInput = createInput;
        this.componentId = componentId;
        this.componentName = componentName;
        this.handler = handler;
    }

    public void close() {
        if(handler != null){
            handler.closeComponent();
        }
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public CreateInput getCreateInput() {
        return createInput;
    }

    public void setCreateInput(CreateInput createInput) {
        this.createInput = createInput;
    }

    public ComponentHandler getHandler() {
        return handler;
    }

    public void setHandler(ComponentHandler handler) {
        this.handler = handler;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }
}