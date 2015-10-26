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

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Message that can be used to report a component usage (by the component). Consumer of this message type can
 * use the information provided to gather usage statistics/metering etc.
 */
public class UsageMeteringMessage implements Serializable {

    private final String type;
    private final Map<String, String> additionalInfo;

    public UsageMeteringMessage(String type) {
        this.type = type;
        this.additionalInfo = Collections.emptyMap();
    }

    public UsageMeteringMessage(String type, Map<String, String> additionalInfo) {
        this.type = type;
        this.additionalInfo = additionalInfo;
    }

    /**
     * Returns a basic type of the message. To be used as a basic differentiator
     * for usage statistics by the consuming system.
     */
    public String getType() {
        return type;
    }

    /**
     * Additional info, which sematic is defined by the message producer
     * and should be understandable by consumer.
     */
    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public String toString() {
        return "UsageMeteringMessage[" + type + ", " + additionalInfo + "]";
    }
}
