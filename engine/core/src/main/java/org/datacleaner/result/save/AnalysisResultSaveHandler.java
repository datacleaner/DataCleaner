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
package org.datacleaner.result.save;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.SimpleAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisResultSaveHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultSaveHandler.class);

    private final AnalysisResult _analysisResult;
    private final Resource _resource;
    private Map<ComponentJob, AnalyzerResult> _unsafeResultElements;

    public AnalysisResultSaveHandler(AnalysisResult analysisResult, Resource resource) {
        _analysisResult = analysisResult;
        _resource = resource;
    }

    public boolean saveAttempt() {
        try {
            saveOrThrow();
            return true;
        } catch (SerializationException e) {
            return false;
        }
    }

    public void saveWithoutUnsafeResultElements() {
        final AnalysisResult safeAnalysisResult = createSafeAnalysisResult();
        saveOrThrow(safeAnalysisResult, _resource);
    }

    public void saveOrThrow() throws SerializationException {
        saveOrThrow(_analysisResult, _resource);
    }

    private static void saveOrThrow(AnalysisResult analysisResult, Resource resource) {
        final SimpleAnalysisResult simpleAnalysisResult;
        if (analysisResult instanceof SimpleAnalysisResult) {
            simpleAnalysisResult = (SimpleAnalysisResult) analysisResult;
        } else {
            simpleAnalysisResult = new SimpleAnalysisResult(analysisResult.getResultMap(),
                    analysisResult.getCreationDate());
        }

        final OutputStream out = resource.write();
        try {
            SerializationUtils.serialize(simpleAnalysisResult, out);
        } catch (SerializationException e) {
            logger.error("Error serializing analysis result: " + analysisResult, e);
            throw e;
        } finally {
            FileHelper.safeClose(out);
        }
    }

    /**
     * Gets a map of unsafe result elements, ie. elements that cannot be saved
     * because serialization fails.
     * 
     * @return
     */
    public Map<ComponentJob, AnalyzerResult> getUnsafeResultElements() {
        if (_unsafeResultElements == null) {
            _unsafeResultElements = new LinkedHashMap<>();
            final Map<ComponentJob, AnalyzerResult> resultMap = _analysisResult.getResultMap();
            for (Entry<ComponentJob, AnalyzerResult> entry : resultMap.entrySet()) {
                AnalyzerResult analyzerResult = entry.getValue();
                try {
                    SerializationUtils.serialize(analyzerResult, new NullOutputStream());
                } catch (SerializationException e) {
                    _unsafeResultElements.put(entry.getKey(), analyzerResult);
                }
            }
        }
        return _unsafeResultElements;
    }

    /**
     * Creates a safe {@link AnalysisResult} for saving
     * 
     * @return a new {@link AnalysisResult} or null if it is not possible to
     *         create a result that is safer than the previous.
     */
    public AnalysisResult createSafeAnalysisResult() {
        final Set<ComponentJob> unsafeKeys = getUnsafeResultElements().keySet();
        if (unsafeKeys.isEmpty()) {
            return _analysisResult;
        }

        final Map<ComponentJob, AnalyzerResult> resultMap = new LinkedHashMap<>(_analysisResult.getResultMap());
        for (ComponentJob unsafeKey : unsafeKeys) {
            resultMap.remove(unsafeKey);
        }

        if (resultMap.isEmpty()) {
            return null;
        }

        return new SimpleAnalysisResult(resultMap, _analysisResult.getCreationDate());
    }
}
