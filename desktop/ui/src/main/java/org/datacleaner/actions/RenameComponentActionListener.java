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
package org.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.datacleaner.job.builder.AbstractComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.LabelUtils;

/**
 * {@link ActionListener} used when renaming a component, such as an analyzer, a
 * filter or a transformer.
 */
public abstract class RenameComponentActionListener implements ActionListener {

    private final ComponentBuilder _componentBuilder;

    public RenameComponentActionListener(ComponentBuilder componentBuilder) {
        _componentBuilder = componentBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String originalValue = LabelUtils.getLabel(_componentBuilder);
        final String newValue = JOptionPane.showInputDialog("Name:", originalValue);
        if (!originalValue.equals(newValue)) {
            if (_componentBuilder instanceof AbstractComponentBuilder) {
                ((AbstractComponentBuilder<?,?,?>)_componentBuilder).setName(newValue);
                onNameChanged();
            }
        }
    }

    protected abstract void onNameChanged();

}
