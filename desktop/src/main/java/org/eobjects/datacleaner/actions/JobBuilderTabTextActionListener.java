/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import java.awt.event.MouseEvent;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;

/**
 * ActionListener that will present a popup menu on the tabbed pane to enable
 * the user to rename a component
 * 
 * @author Kasper Sørensen
 */
public class JobBuilderTabTextActionListener extends AbstractJobBuilderPopupListener implements ActionListener {

    private final CloseableTabbedPane _tabbedPane;

    private volatile int _tabIndex;

    public JobBuilderTabTextActionListener(AnalysisJobBuilder analysisJobBuilder,
            AbstractBeanJobBuilder<?, ?, ?> jobBuilder, int tabIndex, CloseableTabbedPane tabbedPane) {
        super(jobBuilder, analysisJobBuilder);
        _tabIndex = tabIndex;
        _tabbedPane = tabbedPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _tabIndex = e.getID();
        MouseEvent mouseEvent = (MouseEvent) e.getSource();
        showPopup(_tabbedPane, mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    protected void onNameChanged() {
        _tabbedPane.setTitleAt(_tabIndex, LabelUtils.getLabel(getJobBuilder()));
    }

    @Override
    protected void onRemoved() {
    }
}
