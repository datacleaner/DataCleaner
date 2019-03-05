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

import org.apache.metamodel.util.HasName;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainer;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainingOptions;
import org.datacleaner.components.machinelearning.impl.RandomForestClassificationTrainer;
import org.datacleaner.components.machinelearning.impl.SvmClasificationTrainer;

public enum MLAlgorithm implements HasName {

    RANDOM_FOREST("Random Forest"),

    SVM("Support Vector Machine")

    ;

    private final String name;

    private MLAlgorithm(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    MLClassificationTrainer createTrainer(MLClassificationTrainingOptions trainingOptions) {
        switch (this) {
        case RANDOM_FOREST:
            return new RandomForestClassificationTrainer(trainingOptions);
        case SVM:
            return new SvmClasificationTrainer(trainingOptions);
        default:
            throw new UnsupportedOperationException();
        }
    }
}
