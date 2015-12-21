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
package org.datacleaner.widgets.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.OptionsDialog;

/**
 * The "Memory" panel found in the {@link OptionsDialog}
 */
public class MemoryOptionsPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final Timer _updateMemoryTimer;

    public MemoryOptionsPanel() {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);

        final JLabel maxMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
        final JLabel totalMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
        final JLabel usedMemoryLabel = new JLabel("? kb", JLabel.RIGHT);
        final JLabel freeMemoryLabel = new JLabel("? kb", JLabel.RIGHT);

        WidgetUtils.addToGridBag(new JLabel("Max available memory:"), this, 0, 0);
        WidgetUtils.addToGridBag(maxMemoryLabel, this, 1, 0);
        WidgetUtils.addToGridBag(new JLabel("Allocated memory:"), this, 0, 1);
        WidgetUtils.addToGridBag(totalMemoryLabel, this, 1, 1);
        WidgetUtils.addToGridBag(new JLabel("Used memory:"), this, 0, 2);
        WidgetUtils.addToGridBag(usedMemoryLabel, this, 1, 2);
        WidgetUtils.addToGridBag(new JLabel("Free memory:"), this, 0, 3);
        WidgetUtils.addToGridBag(freeMemoryLabel, this, 1, 3);

        JButton button = WidgetFactory.createDefaultButton("Perform garbage collection");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.gc();
                System.runFinalization();
            }
        });
        WidgetUtils.addToGridBag(button, this, 1, 4);

        _updateMemoryTimer = new Timer(1000, new ActionListener() {
            private final Runtime runtime = Runtime.getRuntime();
            private final NumberFormat nf = NumberFormat.getIntegerInstance();

            @Override
            public void actionPerformed(ActionEvent e) {

                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long maxMemory = runtime.maxMemory();
                long usedMemory = totalMemory - freeMemory;

                if (maxMemory == Long.MAX_VALUE) {
                    maxMemoryLabel.setText("(no limit)");
                } else {
                    maxMemoryLabel.setText(nf.format(maxMemory / 1024) + " kb");
                }
                totalMemoryLabel.setText(nf.format(totalMemory / 1024) + " kb");
                usedMemoryLabel.setText(nf.format(usedMemory / 1024) + " kb");
                freeMemoryLabel.setText(nf.format(freeMemory / 1024) + " kb");
            }
        });
        _updateMemoryTimer.setInitialDelay(0);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _updateMemoryTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _updateMemoryTimer.stop();
    }
}
