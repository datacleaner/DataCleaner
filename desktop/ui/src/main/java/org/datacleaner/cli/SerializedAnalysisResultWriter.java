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
package org.datacleaner.cli;

import java.io.OutputStream;
import java.io.Writer;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.AnalysisResultWriter;
import org.datacleaner.result.SimpleAnalysisResult;
import org.apache.metamodel.util.Ref;

public class SerializedAnalysisResultWriter implements AnalysisResultWriter {

    @Override
    public void write(AnalysisResult result, AnalyzerBeansConfiguration configuration, Ref<Writer> writerRef,
            Ref<OutputStream> outputStreamRef) {
        final SimpleAnalysisResult simpleAnalysisResult;

        if (result instanceof SimpleAnalysisResult) {
            simpleAnalysisResult = (SimpleAnalysisResult) result;
        } else {
            simpleAnalysisResult = new SimpleAnalysisResult(result.getResultMap());
        }

        SerializationUtils.serialize(simpleAnalysisResult, outputStreamRef.get());
    }

}
