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
package org.eobjects.datacleaner.monitor.wizard;

import org.eobjects.datacleaner.monitor.shared.widgets.ButtonPanel;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract {@link WizardPanel} implementation.
 */
public abstract class AbstractWizardPanel implements WizardPanel {

    private final FlowPanel _parentPanel;
    private final ButtonPanel _buttonPanel;
    private final SimplePanel _contentPanel;

    public AbstractWizardPanel() {
        _parentPanel = new FlowPanel();
        _contentPanel = createContentPanel();
        _buttonPanel = new ButtonPanel();
    }

    protected abstract SimplePanel createContentPanel();

    @Override
    public Widget getWizardWidget() {
        return _parentPanel;
    }

    @Override
    public void setHeader(String header) {
        final Widget firstWidget = _parentPanel.getWidget(0);
        if (firstWidget instanceof HeadingLabel) {
            HeadingLabel headingLabel = (HeadingLabel) firstWidget;
            headingLabel.setText(header);
        } else {
            HeadingLabel headingLabel = new HeadingLabel(header);
            _parentPanel.insert(headingLabel, 0);
        }
    }

    @Override
    public ButtonPanel getButtonPanel() {
        return _buttonPanel;
    }

    @Override
    public void setContent(IsWidget w) {
        _contentPanel.setWidget(w);
    }
    
    protected Widget getContent() {
        return _contentPanel.getWidget();
    }
    
    protected SimplePanel getContentPanel() {
        return _contentPanel;
    }
}
