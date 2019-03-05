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
package org.datacleaner.components.machinelearning.impl;

import org.datacleaner.components.machinelearning.api.MLClassification;

public class MLConfidenceClassification implements MLClassification {

    private final double[] scores;

    public MLConfidenceClassification(double[] scores) {
        this.scores = scores;
    }

    @Override
    public int getBestClassificationIndex() {
        int winnerIndex = -1;
        double winnerScore = -1;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > winnerScore) {
                winnerScore = scores[i];
                winnerIndex = i;
            }
        }
        return winnerIndex;
    }

    @Override
    public double getConfidence(int classIndex) throws IndexOutOfBoundsException {
        return scores[classIndex];
    }

}
