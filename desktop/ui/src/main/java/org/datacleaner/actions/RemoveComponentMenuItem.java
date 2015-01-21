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

import javax.swing.JMenuItem;

import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;

/**
 * menu item and action listener for removing a component from a job.
 */
public class RemoveComponentMenuItem extends JMenuItem implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final ComponentBuilder _componentBuilder;

    public RemoveComponentMenuItem(AnalysisJobBuilder analysisJobBuilder,
            ComponentBuilder componentBuilder) {
        super("Remove component", ImageManager.get().getImageIcon(IconUtils.ACTION_REMOVE, IconUtils.ICON_SIZE_SMALL));
        _analysisJobBuilder = analysisJobBuilder;
        _componentBuilder = componentBuilder;
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_componentBuilder instanceof AnalyzerComponentBuilder) {
            _analysisJobBuilder.removeAnalyzer((AnalyzerComponentBuilder<?>) _componentBuilder);
        } else if (_componentBuilder instanceof TransformerComponentBuilder) {
            _analysisJobBuilder.removeTransformer((TransformerComponentBuilder<?>) _componentBuilder);
        } else if (_componentBuilder instanceof FilterComponentBuilder) {
            _analysisJobBuilder.removeFilter((FilterComponentBuilder<?, ?>) _componentBuilder);
        } else {
            throw new IllegalStateException("Unexpected component type: " + _componentBuilder);
        }
        onRemoved();
    }

    protected void onRemoved() {
    }
}
