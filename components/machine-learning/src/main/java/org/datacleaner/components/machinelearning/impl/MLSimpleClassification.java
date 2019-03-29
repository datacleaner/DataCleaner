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

import org.datacleaner.components.machinelearning.api.MLClassification;

public class MLSimpleClassification implements MLClassification {

    private final int prediction;

    public MLSimpleClassification(int prediction) {
        this.prediction = prediction;
    }

    @Override
    public int getBestClassificationIndex() {
        return prediction;
    }

    @Override
    public double getConfidence(int classIndex) throws IndexOutOfBoundsException {
        if (classIndex == prediction) {
            return 1;
        }
        return 0;
    }

}
