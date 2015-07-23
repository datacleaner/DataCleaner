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

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.guice.DCModule;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.windows.ResultWindow;

/**
 * Action listener invoked when the user chooses to run/execute an analysis job
 */
public class RunAnalysisActionListener implements ActionListener {

    private final DCModule _dcModule;
    private final AnalysisJobBuilder _analysisJobBuilder;
    private long _lastClickTime = 0;

    public RunAnalysisActionListener(DCModule dcModule, AnalysisJobBuilder analysisJobBuilder) {
        _dcModule = dcModule;
        _analysisJobBuilder = analysisJobBuilder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        run();
    }

    public void run() {
        synchronized (RunAnalysisActionListener.class) {
            long thisClickTime = System.currentTimeMillis();
            if (thisClickTime - _lastClickTime < 1000) {
                // prevent that double clicks fire two analysis runs!
                return;
            }
            _lastClickTime = thisClickTime;
        }
        
        final InjectorBuilder injectorBuilder = _dcModule.createInjectorBuilder();
        injectorBuilder.with(AnalysisJobBuilder.class, _analysisJobBuilder);
        injectorBuilder.with(DataCleanerConfiguration.class, _analysisJobBuilder.getConfiguration());
        injectorBuilder.with(AnalysisJob.class, _analysisJobBuilder.toAnalysisJob(false));

        final ResultWindow window = injectorBuilder.getInstance(ResultWindow.class);
        window.open();
        window.startAnalysis();
    }

}
