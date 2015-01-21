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
package org.datacleaner.result.renderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.result.ResultProducer;
import org.apache.metamodel.util.CollectionUtils;

/**
 * A class that encapsulates all the complicated logic of traversing a crosstab
 * in the correct order, if it is to be rendered using two axes (horizontal and
 * vertical). The actual output of the rendering is not specified by this class
 * - it uses a callback class for all the concrete rendering.
 * 
 * @see CrosstabRendererCallback
 * 
 * 
 */
public class CrosstabRenderer {

    private static final int MAX_HORIZONTAL_CELLS = 10;

    private final Crosstab<?> crosstab;
    private final List<CrosstabDimension> horizontalDimensions;
    private final List<CrosstabDimension> verticalDimensions;
    private int horizontalCells = 1;
    private int verticalCells = 1;

    public CrosstabRenderer(Crosstab<?> crosstab) {
        if (crosstab == null) {
            throw new IllegalArgumentException("Crosstab cannot be null");
        }
        this.crosstab = crosstab;
        this.horizontalDimensions = new ArrayList<CrosstabDimension>();
        this.verticalDimensions = new ArrayList<CrosstabDimension>();
    }

    public void autoAssignDimensions() {
        // create a list of dimensions (with max 10 categories) to be layed
        // out horizontally

        List<CrosstabDimension> dimensions = crosstab.getDimensions();
        List<CrosstabDimension> autoAssignDimensions = new LinkedList<CrosstabDimension>();

        for (CrosstabDimension dimension : dimensions) {
            // boolean hasCategories = dimension.getCategoryCount() > 0;
            // if (hasCategories && !isAssigned(dimension)) {
            if (!isAssigned(dimension)) {
                autoAssignDimensions.add(dimension);
            }
        }

        if (autoAssignDimensions.size() == 2) {
            makeHorizontal(autoAssignDimensions.get(0));
            makeVertical(autoAssignDimensions.get(1));
        } else {
            for (CrosstabDimension dimension : autoAssignDimensions) {
                boolean horizontal = false;
                int categoryCount = dimension.getCategoryCount();
                if (horizontalCells <= MAX_HORIZONTAL_CELLS && categoryCount <= MAX_HORIZONTAL_CELLS) {
                    if (horizontalCells * categoryCount <= MAX_HORIZONTAL_CELLS) {
                        makeHorizontal(dimension);
                        horizontal = true;
                    }
                }

                if (!horizontal) {
                    makeVertical(dimension);
                }
            }
        }
    }

    public boolean isAssigned(CrosstabDimension dimension) {
        return verticalDimensions.contains(dimension) || horizontalDimensions.contains(dimension);
    }

    public void makeHorizontal(CrosstabDimension dimension) {
        if (verticalDimensions.contains(dimension)) {
            verticalDimensions.remove(dimension);
            verticalCells = verticalCells / dimension.getCategoryCount();
        }
        if (!horizontalDimensions.contains(dimension)) {
            horizontalDimensions.add(dimension);
            horizontalCells = horizontalCells * dimension.getCategoryCount();
        }
    }

    public void makeVertical(CrosstabDimension dimension) {
        if (horizontalDimensions.contains(dimension)) {
            horizontalDimensions.remove(dimension);
            horizontalCells = horizontalCells / dimension.getCategoryCount();
        }
        if (!verticalDimensions.contains(dimension)) {
            verticalDimensions.add(dimension);
            verticalCells = verticalCells * dimension.getCategoryCount();
        }
    }

    public <E> E render(CrosstabRendererCallback<E> callback) {
        autoAssignDimensions();

        List<CrosstabDimension> dimensions = crosstab.getDimensions();
        if (CollectionUtils.isNullOrEmpty(dimensions)) {
            return callback.getResult();
        }

        if (CollectionUtils.isNullOrEmpty(horizontalDimensions) && CollectionUtils.isNullOrEmpty(verticalDimensions)) {
            return callback.getResult();
        }

        callback.beginTable(crosstab, horizontalDimensions, verticalDimensions);

        // print the (horizontal) headers
        {
            int colspan = horizontalCells;
            int repeatHeaders = 1;
            for (int i = 0; i < horizontalDimensions.size(); i++) {
                CrosstabDimension dimension = horizontalDimensions.get(i);
                if (dimension.getCategoryCount() > 0) {
                    callback.beginRow();

                    // empty cells for each vertical dimension
                    for (CrosstabDimension verticalDimension : verticalDimensions) {
                        callback.emptyHeader(verticalDimension, dimension);
                    }

                    colspan = colspan / dimension.getCategoryCount();
                    for (int j = 0; j < repeatHeaders; j++) {
                        for (String category : dimension.getCategories()) {
                            callback.horizontalHeaderCell(category, dimension, colspan);
                        }
                    }
                    repeatHeaders = repeatHeaders * dimension.getCategoryCount();
                    callback.endRow();
                }
            }
        }

        // print the content rows
        {
            CrosstabNavigator<?> navigator = crosstab.navigate();

            for (int i = 0; i < verticalCells; i++) {
                callback.beginRow();

                navigateOnAxis(verticalDimensions, i, verticalCells, navigator);

                // print the vertical headers
                {

                    int rowspan = verticalCells;
                    for (int j = 0; j < verticalDimensions.size(); j++) {
                        CrosstabDimension dimension = verticalDimensions.get(j);
                        rowspan = rowspan / dimension.getCategoryCount();

                        if (i % rowspan == 0) {
                            String category = navigator.getCategory(dimension);

                            callback.verticalHeaderCell(category, dimension, rowspan);
                        }
                    }
                }

                for (int j = 0; j < horizontalCells; j++) {

                    navigateOnAxis(horizontalDimensions, j, horizontalCells, navigator);

                    final Object value = navigator.get();
                    final ResultProducer resultProducer = navigator.explore();
                    callback.valueCell(value, resultProducer);
                }

                callback.endRow();
            }
        }

        callback.endTable();

        return callback.getResult();
    }

    private void navigateOnAxis(List<CrosstabDimension> dimensionsOnAxis, int cellIndex, int cellCount,
            CrosstabNavigator<?> navigator) {
        int colspan = cellCount;
        int category = 0;
        int localIndex = cellIndex;

        for (int k = 0; k < dimensionsOnAxis.size(); k++) {
            CrosstabDimension dimension = dimensionsOnAxis.get(k);

            int categoryCount = dimension.getCategoryCount();

            int offset = category * colspan;

            colspan = colspan / categoryCount;

            localIndex = localIndex - offset;

            category = localIndex / colspan;

            String categoryName = dimension.getCategories().get(category);
            navigator.where(dimension, categoryName);
        }
    }
}
