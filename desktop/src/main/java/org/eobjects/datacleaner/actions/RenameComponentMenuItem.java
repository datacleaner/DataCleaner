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
package org.eobjects.datacleaner.actions;

import javax.swing.JMenuItem;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

public class RenameComponentMenuItem extends JMenuItem {

    private static final long serialVersionUID = 1L;

    public RenameComponentMenuItem(AbstractBeanJobBuilder<?, ?, ?> componentBuilder) {
        super("Rename component", ImageManager.get().getImageIcon(IconUtils.ACTION_RENAME));
        addActionListener(new RenameComponentActionListener(componentBuilder) {
            @Override
            protected void onNameChanged() {
            }
        });
    }
}
