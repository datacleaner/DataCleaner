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
package org.datacleaner.components.fillpattern;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.stream.Collectors;

import org.datacleaner.result.AnalysisResult;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.junit.Test;

public class DeserializationTest {

    // tests that an analysis result of this type can be deserialized. The
    // example result was created manually based on Open Street Map (OSM) data.
    @Test
    public void testDeserializeAnalysisResult() throws Exception {
        final AnalysisResult result;
        try (FileInputStream in =
                new FileInputStream(new File("examples/Fill-pattern-analysis-OSM-example.analysis.result.dat"))) {
            try (ChangeAwareObjectInputStream changeAwareObjectInputStream = new ChangeAwareObjectInputStream(in)) {
                final Object obj = changeAwareObjectInputStream.readObject();

                result = (AnalysisResult) obj;
            }
        }

        assertEquals(1, result.getResults().size());

        final FillPatternResult fillPatternResult = (FillPatternResult) result.getResults().get(0);

        final String str = fillPatternResult.getFillPatternGroups().stream()
                .map(r -> r.getGroupName() + "=" + r.getPatternCount()).collect(Collectors.joining(","));
        assertEquals(
                "<null>=54,US=26,DE=20,GB=20,AT=14,SE=13,CH=12,IT=12,FI=11,ES=10,SK=9,FR=8,NL=7,NO=6,LU=6,CZ=5,BE=5,"
                        + "PL=5,IS=5,EE=4,RU=4,RO=4,DK=3,IE=3,BG=3,SI=2,HU=2,LT=2,LV=2,GR=2,BY=1,HR=1,IM=1,GE=1,RS=1,MT=1,CY=1",
                str);
    }
}
