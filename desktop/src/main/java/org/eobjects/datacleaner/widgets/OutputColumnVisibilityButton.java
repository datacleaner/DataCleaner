/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.widgets;

import javax.swing.Icon;

import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.datacleaner.util.ImageManager;

/**
 * A toggle button for the visibility of a {@link MutableInputColumn}.
 */
public class OutputColumnVisibilityButton extends DCCheckBox<MutableInputColumn<?>> implements
        MutableInputColumn.Listener, DCCheckBox.Listener<MutableInputColumn<?>> {

    private static final long serialVersionUID = 1L;
    
    private static final Icon SELECTED_ICON = ImageManager.getInstance().getImageIcon("images/widgets/output_column_visibility_visible.png");
    private static final Icon NOT_SELECTED_ICON = ImageManager.getInstance().getImageIcon("images/widgets/output_column_visibility_hidden.png");

    public OutputColumnVisibilityButton(MutableInputColumn<?> column) {
        super(null, !column.isHidden());

        setValue(column);
        column.addListener(this);
        addListener(this);
        
        setIcon(NOT_SELECTED_ICON);
        setSelectedIcon(SELECTED_ICON);
        
        setToolTipText("Toggle visibility of this column to other components");
    }

    @Override
    public void onVisibilityChanged(MutableInputColumn<?> inputColumn, boolean hidden) {
        setSelected(!hidden);
    }

    @Override
    public void onNameChanged(MutableInputColumn<?> inputColumn, String oldName, String newName) {
    }

    @Override
    public void onItemSelected(MutableInputColumn<?> item, boolean selected) {
        item.setHidden(!selected);
    }
}
