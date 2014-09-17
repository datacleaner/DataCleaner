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
package org.eobjects.datacleaner.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.scheduling.api.VariableProvider;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.springframework.core.env.Environment;

/**
 * Implementation of {@link VariableProvider} which consults Spring's
 * {@link Environment} for variables. Variables which are demarcated as
 * ${variable} or #{variable} are replaced.
 */
public class SpringVariableProvider implements VariableProvider {

    @Inject
    @Provided
    Environment environment;

    @Override
    public Map<String, String> provideValues(JobContext job, ExecutionLog execution) {
        final Map<String, String> result = new HashMap<String, String>();

        final Map<String, String> variables = job.getVariables();
        final Set<Entry<String, String>> entries = variables.entrySet();
        for (Entry<String, String> entry : entries) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (isEnvironmentProperty(key)) {
                final String propertyKey = getEnvironmentProperty(key);
                final String propertyValue = environment.getProperty(propertyKey);
                if (propertyValue != null) {
                    result.put(key, propertyValue);
                }
            } else if (isEnvironmentProperty(value)) {
                final String propertyKey = getEnvironmentProperty(value);
                final String propertyValue = environment.getProperty(propertyKey);
                if (propertyValue != null) {
                    result.put(key, propertyValue);
                }
            }
        }

        return result;
    }

    private String getEnvironmentProperty(String key) {
        key = key.trim();
        final String property = key.substring(2, key.length() - 1);
        return property;
    }

    private boolean isEnvironmentProperty(String key) {
        if (key == null) {
            return false;
        }
        key = key.trim();
        if (key.startsWith("#{") || key.startsWith("${")) {
            if (key.endsWith("}")) {
                return true;
            }
        }
        return false;
    }

}
