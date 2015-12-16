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

import org.datacleaner.api.AnalyzerResult;

public class CrosstabNavigator<E extends Serializable> implements Cloneable {

    private Crosstab<E> crosstab;
    private String[] categories;

    public CrosstabNavigator(Crosstab<E> crosstab) {
        this.crosstab = crosstab;
        categories = new String[crosstab.getDimensionCount()];
    }

    public CrosstabNavigator<E> where(String dimension, String isCategory) {
        int index = crosstab.getDimensionIndex(dimension);
        categories[index] = isCategory;
        return this;
    }

    public CrosstabNavigator<E> where(CrosstabDimension dimension, String isCategory) {
        return where(dimension.getName(), isCategory);
    }

    public void put(E value) throws IllegalArgumentException, NullPointerException {
        put(value, false);
    }

    /**
     * Puts the given value to the navigated position in the crosstab.
     * 
     * @param value
     *            the value to put.
     * @param createCategories
     *            if true, the chosen categories will automatically be created
     *            if they do not already exists in the dimensions of the
     *            crosstab.
     * @throws IllegalArgumentException
     *             if the position or value is invalid, typically because one or
     *             more dimensions lacks a specified category or the value type
     *             is not acceptable (typically because of class casting issues)
     * @throws NullPointerException
     *             if some of the specified categories are null
     */
    public void put(E value, boolean createCategories) throws IllegalArgumentException, NullPointerException {
        if (createCategories) {
            for (int i = 0; i < categories.length; i++) {
                String category = categories[i];
                CrosstabDimension dimension = crosstab.getDimension(i);
                dimension.addCategory(category);
            }
        }
        crosstab.putValue(value, categories);
    }

    /**
     * Gets the value associated with the navigated position of the crosstab.
     * 
     * @return
     * @throws IllegalArgumentException
     *             if the position is invalid, typically because one or more
     *             dimensions lacks a specified category.
     * @throws NullPointerException
     *             if some of the specified categories are null
     */
    public E get() throws IllegalArgumentException, NullPointerException {
        return crosstab.getValue(categories);
    }

    /**
     * Gets the value associated with the navigated position in the crosstab in
     * a safe manner, where any issues in the navigation will not be thrown as
     * an exception, but the parameter value will be returned instead.
     * 
     * @param valueIfError
     * @return
     */
    public E safeGet(E valueIfError) {
        try {
            return get();
        } catch (Exception e) {
            return valueIfError;
        }
    }

    /**
     * Attaches an AnalyzerResult as result-exploration data for the navigated
     * position of the crosstab.
     * 
     * @param explorationResult
     */
    public void attach(AnalyzerResult explorationResult) {
        final ResultProducer resultProducer;
        if (explorationResult == null) {
            resultProducer = null;
        } else {
            resultProducer = new DefaultResultProducer(explorationResult);
        }
        attach(resultProducer);
    }

    /**
     * Attaches a ResultProducer as result-exploration data-provider for the
     * navigated position of the crosstab. Note that if the ResultProducer is
     * Serializable, it will be saved with the crosstab on serialization.
     * 
     * @param explorationResultProducer
     */
    public void attach(ResultProducer explorationResultProducer) {
        crosstab.attachResultProducer(explorationResultProducer, categories);
    }

    public ResultProducer explore() {
        return crosstab.explore(categories);
    }

    @Override
    public CrosstabNavigator<E> clone() {
        try {
            @SuppressWarnings("unchecked")
            CrosstabNavigator<E> n = (CrosstabNavigator<E>) super.clone();
            n.categories = categories.clone();
            return n;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCategory(CrosstabDimension dimension) {
        int index = crosstab.getDimensionIndex(dimension);
        return categories[index];
    }
}
