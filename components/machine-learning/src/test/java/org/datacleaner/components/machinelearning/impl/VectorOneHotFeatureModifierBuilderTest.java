/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.components.machinelearning.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.junit.Test;

public class VectorOneHotFeatureModifierBuilderTest {

    @Test
    public void testBuildSimple() {
        final VectorOneHotEncodingFeatureModifierBuilder builder = new VectorOneHotEncodingFeatureModifierBuilder();
        builder.addRecordValue("RED");
        builder.addRecordValue("GREEN");
        builder.addRecordValue("red ");
        builder.addRecordValue("BLUE");

        final MLFeatureModifier modifier = builder.build();
        assertEquals(3, modifier.getFeatureCount());

        final double[] result = modifier.generateFeatureValues(" Red");
        assertEquals("[0.0, 0.0, 1.0]", Arrays.toString(result));
    }

    @Test
    public void testBuildWithLimitOutput() {
        final VectorOneHotEncodingFeatureModifierBuilder builder = new VectorOneHotEncodingFeatureModifierBuilder(3,
                false);
        builder.addRecordValue("RED");
        builder.addRecordValue("RED");
        builder.addRecordValue("GREEN");
        builder.addRecordValue("GREEN");
        builder.addRecordValue("YELLOW");
        builder.addRecordValue("BLUE");

        final MLFeatureModifier modifier = builder.build();
        assertEquals(2, modifier.getFeatureCount());
        
        final double[] result1 = modifier.generateFeatureValues("YELLOW");
        assertEquals("[0.0, 0.0]", Arrays.toString(result1));
        
        final double[] result2 = modifier.generateFeatureValues("RED");
        assertEquals("[0.0, 1.0]", Arrays.toString(result2));
    }
}
