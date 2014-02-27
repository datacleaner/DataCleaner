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
package org.eobjects.datacleaner.panels.coalesce;

import java.util.ArrayList;

import javax.swing.JLabel;

import org.eobjects.analyzer.beans.coalesce.CoalesceUnit;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.panels.DCPanel;

public class CoalesceUnitPanel extends DCPanel {

    private static final long serialVersionUID = 1L;
    private final MultipleCoalesceUnitPropertyWidget _parent;

    public CoalesceUnitPanel(MultipleCoalesceUnitPropertyWidget parent, CoalesceUnit unit) {
        _parent = parent;
        
        // TODO Auto-generated constructor stub
        add(new JLabel("foo"));
        
    }

    public CoalesceUnitPanel(MultipleCoalesceUnitPropertyWidget parent) {
        this(parent, null);
    }
    
    public boolean isSet() {
        return false;
    }

    public CoalesceUnit getCoalesceUnit() {
        // TODO Auto-generated method stub
        ArrayList<InputColumn<?>> inputColumns = new ArrayList<InputColumn<?>>();
        return new CoalesceUnit(inputColumns);
    }

}
