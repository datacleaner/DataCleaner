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
package org.eobjects.datacleaner.monitor.shared.widgets;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 * A widget for showing the progress in a wizard.
 */
public class WizardProgressBar extends FlowPanel {

    private boolean _indicatingMore;

    public WizardProgressBar() {
        this(0);
    }

    public WizardProgressBar(int steps) {
        super();
        setStyleName("WizardProgressBar");
        setSteps(steps);
    }

    public int getSteps() {
        return getChildren().size() - 2;
    }

    public void setSteps(final Integer steps) {
        setSteps(steps, false);
    }

    public void setSteps(final Integer steps, boolean indicateMore) {
        if (steps == null) {
            return;
        }
        if (steps.intValue() == getSteps() && _indicatingMore == indicateMore) {
            return;
        }

        clear();

        Widget metaBefore = new Label();
        metaBefore.setStyleName("MetaItem");
        metaBefore.addStyleName("before");
        add(metaBefore);

        for (int i = 0; i < steps; i++) {
            Widget child = new Label("" + (i + 1));
            child.setStyleName("WizardProgressItem");
            if (i == 0) {
                child.addStyleName("first");
            } else if (i == steps - 1) {
                child.addStyleName("last");
            }
            add(child);
        }
        
        _indicatingMore = indicateMore;
        if (indicateMore) {
            Widget metaAfter = new Label("...");
            metaAfter.setStyleName("MetaItem");
            metaAfter.addStyleName("more");
            add(metaAfter);
        }

        Widget metaAfter = new Label();
        metaAfter.setStyleName("MetaItem");
        metaAfter.addStyleName("after");
        add(metaAfter);
    }

    /**
     * Sets the current progress index (0-based)
     * 
     * @param stepIndex
     */
    public void setProgress(final Integer stepIndex) {
        WidgetCollection children = getChildren();
        for (Widget child : children) {
            child.removeStyleName("current");
        }
        if (stepIndex != null) {
            children.get(stepIndex + 1).addStyleName("current");
        }
    }
}
