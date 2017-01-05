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
package org.datacleaner.metadata;

import java.util.HashMap;
import java.util.Map;

public class TemplateMetadata {
    
    public static final String TEMPLATE_VALUE = "template-value.";  
    
    public static Map<String, String> createMetadataProperty(String propertyName, String propertyString) {
        final Map<String, String> metadataProperties = new HashMap<>();
        metadataProperties.put(TEMPLATE_VALUE + propertyName, propertyString);
        return metadataProperties;
    }
}
