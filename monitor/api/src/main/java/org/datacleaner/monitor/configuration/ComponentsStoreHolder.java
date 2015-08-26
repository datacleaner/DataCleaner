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

/**
 * Class ComponentsStoreHolder
 * Object for storing to component cache.
 * 
 * @since 11.8.15
 */
public class ComponentsStoreHolder {

    private long timeout;
    private long useTimestamp;
    private CreateInput createInput;
    private String componentId;
    private String componentName;

    public ComponentsStoreHolder() {
    }

    public ComponentsStoreHolder(long timeout, CreateInput createInput, String componentId, String componentName) {
        this.timeout = timeout;
        this.createInput = createInput;
        this.componentId = componentId;
        this.componentName = componentName;
        this.useTimestamp = System.currentTimeMillis();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getUseTimestamp() {
        return useTimestamp;
    }

    public void setUseTimestamp(long useTimestamp) {
        this.useTimestamp = useTimestamp;
    }

    public CreateInput getCreateInput() {
        return createInput;
    }

    public void setCreateInput(CreateInput createInput) {
        this.createInput = createInput;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public void updateTimeStamp(){
        useTimestamp = System.currentTimeMillis();
    }

    /**
     * Check expiration of configuration
     *
     * @return
     */
    @JsonIgnore
    public boolean isValid() {
        long now = System.currentTimeMillis();
        return now < useTimestamp + timeout;
    }
}
