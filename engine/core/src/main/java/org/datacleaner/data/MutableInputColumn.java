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
package org.datacleaner.data;

/**
 * Represents a column that is mutable (editable by the user).
 * 
 * Mutable columns have editable names but unique id's to identify them (whereas
 * the names identify the immutable columns).
 */
public interface MutableInputColumn<E> extends InputColumn<E> {

    /**
     * Listener interface for changes made to a {@link MutableInputColumn}.
     */
    public static interface Listener {

        public void onNameChanged(MutableInputColumn<?> inputColumn, String oldName, String newName);

        public void onVisibilityChanged(MutableInputColumn<?> inputColumn, boolean hidden);

    }

    /**
     * Sets the name of the column
     * 
     * @param name
     */
    public void setName(String name);

    /**
     * Gets the initial name of the column
     */
    public String getInitialName();

    /**
     * @return an id that is unique within the AnalysisJob that is being built
     *         or executed.
     */
    public String getId();

    /**
     * Determines if this input column is visible or hidden for the user.
     * 
     * @return
     */
    public boolean isHidden();

    /**
     * Sets if 'hidden' flag to indicate if this input column is visibile or
     * hidden for the user.
     * 
     * @param hidden
     */
    public void setHidden(boolean hidden);

    /**
     * Adds a listener to this {@link MutableInputColumn}.
     * 
     * @param listener
     * @return a boolean indicating if the listener was added (returns true) or
     *         if it already existed (return false).
     */
    public boolean addListener(Listener listener);

    /**
     * Removes a listener from this {@link MutableInputColumn}
     * 
     * @param listener
     * @return a boolean indicating if the listener was found and removed or
     *         not.
     */
    public boolean removeListener(Listener listener);
}
