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
package org.datacleaner.metamodel.datahub.update;

import java.util.Map;

/**
 * POJO for transferring updated records from DataCleaner to the DataHub update
 * REST service.
 *
 */
public class UpdateData {

    private final String grId;
    private final Map<String, Object> fields;

    public UpdateData(String grId, Map<String, Object> fields) {
        this.grId = grId;
        this.fields = fields;
    }

    public String getGrId() {
        return grId;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

}
