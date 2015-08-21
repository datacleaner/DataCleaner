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
package org.datacleaner.beans;

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;

public class StringAnalyzerResultTest extends TestCase {

    public void testDeserializeOldVersion() throws Exception {
        FileInputStream in = new FileInputStream(
                "src/test/resources/string_analyzer_result_old.analysis.result.dat");
        try {
            ChangeAwareObjectInputStream ois = new ChangeAwareObjectInputStream(in);
            try {
                Object obj = ois.readObject();
                assertNotNull(obj);

                AnalysisResult result = (AnalysisResult) obj;

                AnalyzerResult analyzerResult = result.getResults().get(0);
                assertNotNull(analyzerResult);

                StringAnalyzerResult stringAnalyzerResult = (StringAnalyzerResult) analyzerResult;

                InputColumn<String>[] cols = stringAnalyzerResult.getColumns();
                assertEquals("[id, address1, address2]", CollectionUtils.map(cols, new HasNameMapper()).toString());

                assertNotNull(stringAnalyzerResult.getNullCount(cols[0]));
                assertNotNull(stringAnalyzerResult.getNullCount(cols[1]));
                assertNotNull(stringAnalyzerResult.getNullCount(cols[2]));

                assertNull(stringAnalyzerResult.getBlankCount(cols[0]));
                assertNull(stringAnalyzerResult.getBlankCount(cols[1]));
                assertNull(stringAnalyzerResult.getBlankCount(cols[2]));
            } finally {
                ois.close();
            }
        } finally {
            in.close();
        }
    }
}
