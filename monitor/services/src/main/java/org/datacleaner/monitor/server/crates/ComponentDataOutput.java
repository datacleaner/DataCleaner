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
package org.datacleaner.monitor.server.crates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Crate for a component output.
 *
 * @author k.houzvicka
 * @since 9. 7. 2015
 */
public class ComponentDataOutput implements Serializable {
    private Serializable results = null;
    private ComponentConfiguration configuration = new ComponentConfiguration();

    public Serializable getResults() {
        return results;
    }

    public void setResults(Serializable results) {
        if (results != null) {
            this.results = results;
        }
    }

    public ComponentConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ComponentConfiguration configuration) {
        if (configuration != null) {
            this.configuration = configuration;
        }
    }
}
