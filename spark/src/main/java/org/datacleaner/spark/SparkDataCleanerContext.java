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
package org.datacleaner.spark;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.Accumulator;


/**
 * A container for for values that need to be passed between Spark workers. All the values need to be {@link Serializable}}.
 *
 */
public class SparkDataCleanerContext implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String _dataCleanerConfigurationPath;
    private final String _analysisJobXmlPath;
    
    private final Map<String, Accumulator<?>> _accumulators;
    
    public SparkDataCleanerContext(final String dataCleanerConfigurationPath, final String analysisJobXmlPath) {
        _dataCleanerConfigurationPath = dataCleanerConfigurationPath;
        _analysisJobXmlPath = analysisJobXmlPath;
        
        _accumulators = new HashMap<String, Accumulator<?>>();
    }
    
    public String getDataCleanerConfigurationPath() {
        return _dataCleanerConfigurationPath;
    }
    
    public String getAnalysisJobXmlPath() {
        return _analysisJobXmlPath;
    }
    
    public void addAccumulator(final String name, final Accumulator<?> accumulator) {
        _accumulators.put(name, accumulator);
    }
    
    public void addAccumulator(final Accumulator<?> accumulator) {
        final String name = accumulator.name().get();
        if (name != null) {
            _accumulators.put(name, accumulator);
        } else {
            throw new IllegalArgumentException("Specified accumulator does not have a name. Use addAccumulator(String name, Accumulator<?> accumulator) instead.");
        }
    }
    
    public Accumulator<?> getAccumulator(final String name) {
        return _accumulators.get(name);
    }
    
}
