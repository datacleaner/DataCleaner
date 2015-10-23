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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Added by jakub on 22.10.15
 */
public class ExecutionMeteringMessage implements ComponentMessage {

    private static Iterable<Map.Entry<String, String>> EMPTY_ITERABLE = new ArrayList<>();

    private final String type;
    private Map<String, String> additionalInfo;

    public ExecutionMeteringMessage(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public ExecutionMeteringMessage putAdditionalInfo(String key, String value) {
        if(additionalInfo == null) {additionalInfo = new HashMap<>();}
        additionalInfo.put(key, value);
        return this;
    }

    public Iterable<Map.Entry<String, String>> getAdditionalInfo() {
        return additionalInfo == null ? EMPTY_ITERABLE : additionalInfo.entrySet();
    }
}
