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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.apache.metamodel.schema.Table;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

public class RemoveSourceTableMenuItem extends JMenuItem implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final Table _table;

    public RemoveSourceTableMenuItem(AnalysisJobBuilder analysisJobBuilder, Table table) {
        super("Remove table from source", ImageManager.get().getImageIcon(IconUtils.ACTION_REMOVE));
        _analysisJobBuilder = analysisJobBuilder;
        _table = table;
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _analysisJobBuilder.removeSourceTable(_table);
    }

}
