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
package org.eobjects.datacleaner.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;

/**
 * A panel that has a title, a small collapse/expand button and a label, which
 * is used to show/hide a panel underneath it.
 * 
 * @author Kasper Sørensen
 */
public class DCCollapsiblePanel {

	private static final long serialVersionUID = 1L;

	private static final Icon _expandedIcon = ImageManager.getInstance().getImageIcon("images/actions/add.png",
			IconUtils.ICON_SIZE_SMALL);
	private static final Icon _collapsedIcon = ImageManager.getInstance().getImageIcon("images/actions/remove.png",
			IconUtils.ICON_SIZE_SMALL);

	private final JXCollapsiblePane _collapsiblePane;
	private final DCLabel _label;
	private String _collapsedText;
	private String _expandedText;

	public DCCollapsiblePanel() {
		this(null, null, true, null);
	}

	public DCCollapsiblePanel(String collapsedText, String expandedText, boolean collapsed, JComponent component) {
		_collapsedText = collapsedText;
		_expandedText = expandedText;
		_collapsiblePane = WidgetFactory.createCollapsiblePane(Direction.DOWN);
		setComponent(component);
		if (collapsed) {
			_label = DCLabel.dark(collapsedText);
			collapse();
		} else {
			_label = DCLabel.dark(expandedText);
			expand();
		}
		_label.setFont(WidgetUtils.FONT_HEADER2);

		_label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				toggle();
			}
		});
	}

	public DCPanel toPanel() {
		DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(_label, BorderLayout.NORTH);
		panel.add(_collapsiblePane, BorderLayout.CENTER);
		return panel;
	}

	public void toggle() {
		setCollapsed(!isCollapsed());
	}

	public void collapse() {
		setCollapsed(true);
	}

	public void expand() {
		setCollapsed(false);
	}

	public boolean isCollapsed() {
		return _collapsiblePane.isCollapsed();
	}

	public void setCollapsed(boolean collapsed) {
		if (collapsed) {
			_label.setText(getCollapsedText());
			_label.setIcon(_collapsedIcon);
		} else {
			_label.setText(getExpandedText());
			_label.setIcon(_expandedIcon);
		}
		_collapsiblePane.setCollapsed(collapsed);
		_collapsiblePane.updateUI();
	}

	public String getCollapsedText() {
		return _collapsedText;
	}

	public void setCollapsedText(String collapsedText) {
		_collapsedText = collapsedText;
	}

	public String getExpandedText() {
		return _expandedText;
	}

	public void setExpandedText(String expandedText) {
		_expandedText = expandedText;
	}

	public Component getComponent() {
		Container contentPane = _collapsiblePane.getContentPane();
		if (contentPane.getComponentCount() == 0) {
			return null;
		}
		DCPanel innerPanel = (DCPanel) contentPane.getComponent(0);
		return innerPanel.getComponent(0);
	}

	public void setComponent(Component component) {
		Container contentPane = _collapsiblePane.getContentPane();
		contentPane.removeAll();
		if (component != null) {
			contentPane.add(component);
		}
	}
}
