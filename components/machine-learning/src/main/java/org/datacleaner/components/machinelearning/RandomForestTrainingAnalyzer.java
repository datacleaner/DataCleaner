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
package org.datacleaner.components.machinelearning;

import javax.inject.Named;

import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainer;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainingOptions;
import org.datacleaner.components.machinelearning.impl.RandomForestClassificationTrainer;

@Named("Train Random Forest classifier")
@Description("Train a classifier of the 'Random Forest' type.")
public class RandomForestTrainingAnalyzer extends MLTrainingAnalyzer {

    @Configured
    @NumberProperty(negative = false, zero = false)
    int epochs = 10;

    @Configured("Number of trees")
    @NumberProperty(negative = false, zero = false)
    int numTrees = 64;

    @Override
    protected MLClassificationTrainer createTrainer(MLClassificationTrainingOptions options) {
        return new RandomForestClassificationTrainer(options, epochs, numTrees);
    }

}
