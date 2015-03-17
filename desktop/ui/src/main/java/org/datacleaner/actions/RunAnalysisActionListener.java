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

import javax.inject.Inject;
import javax.inject.Provider;

import org.datacleaner.windows.ResultWindow;

/**
 * Action listener invoked when the user chooses to run/execute an analysis job
 */
public class RunAnalysisActionListener implements ActionListener {

    private final Provider<ResultWindow> _resultWindowProvider;

    private long _lastClickTime = 0;

    @Inject
    protected RunAnalysisActionListener(Provider<ResultWindow> resultWindowProvider) {
        _resultWindowProvider = resultWindowProvider;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        synchronized (RunAnalysisActionListener.class) {
            long thisClickTime = System.currentTimeMillis();
            if (thisClickTime - _lastClickTime < 1000) {
                // prevent that double clicks fire two analysis runs!
                return;
            }
            _lastClickTime = thisClickTime;
        }

        final ResultWindow window = _resultWindowProvider.get();
        window.open();
        window.startAnalysis();
    }

}
