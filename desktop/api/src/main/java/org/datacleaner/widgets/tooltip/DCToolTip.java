/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.widgets.tooltip;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.border.EmptyBorder;

/**
 * A {@link JToolTip} that presents an arbitrary component as its tooltip
 * contents. The tooltip text (which the {@code ToolTipManager} sets on the
 * tip) is forwarded as a {@code tiptext} property change, but not rendered,
 * so listeners such as {@code SchemaTree} can build the presented component
 * from it while the component itself defines the visual appearance.
 */
public class DCToolTip extends JToolTip {

    private static final long serialVersionUID = 1L;
    private final JComponent _tooltipComponent;

    public DCToolTip(final JComponent owner, final JComponent tooltipComponent) {
        super();
        _tooltipComponent = tooltipComponent;
        setComponent(owner);
        setLayout(new BorderLayout());
        add(tooltipComponent, BorderLayout.CENTER);

        // the presented component defines the whole appearance of the tooltip
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    public JComponent getTooltipComponent() {
        return _tooltipComponent;
    }

    @Override
    public void setTipText(final String tipText) {
        // do not store the text: it is only used as a notification to
        // interested listeners so that they can update the presented component
        firePropertyChange("tiptext", null, tipText);
    }

    @Override
    public String getTipText() {
        // no text is rendered by the tooltip UI; the presented component is
        // the sole content (returns null so the LaF does not paint any text)
        return null;
    }
}
