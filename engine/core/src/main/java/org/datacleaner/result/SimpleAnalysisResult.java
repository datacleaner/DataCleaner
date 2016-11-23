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
package org.datacleaner.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;

/**
 * A simple (and Serializable!) implementation of the {@link AnalysisResult}
 * interface. Useful for storing and loading in files or other binary
 * destinations, using Java's serialization API.
 */
public class SimpleAnalysisResult extends AbstractAnalysisResult implements Serializable, AnalysisResult {

    private static final long serialVersionUID = 1L;

    private final Map<ComponentJob, AnalyzerResult> _results;
    private final Date _creationDate;

    public SimpleAnalysisResult() {
        this(new HashMap<>());
    }

    public SimpleAnalysisResult(final AnalysisResult result) {
        this(result.getResultMap(), new Date());
    }

    public SimpleAnalysisResult(final Map<ComponentJob, AnalyzerResult> results) {
        this(results, new Date());
    }

    public SimpleAnalysisResult(final Map<ComponentJob, AnalyzerResult> results, final Date creationDate) {
        _results = results;
        _creationDate = creationDate;
    }

    @Override
    public List<AnalyzerResult> getResults() {
        return new ArrayList<>(_results.values());
    }

    @Override
    public Map<ComponentJob, AnalyzerResult> getResultMap() {
        return Collections.unmodifiableMap(_results);
    }

    @Override
    public Date getCreationDate() {
        return _creationDate;
    }
}
