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
package org.datacleaner.widgets;

import java.awt.Cursor;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.ImageManager;
import org.jdesktop.swingx.action.OpenBrowserAction;

public class NeopostToolbarButton extends JButton {

	private static final long serialVersionUID = 1L;

	public NeopostToolbarButton() {
		this(ImageManager.get().getImageIcon("images/powered-by-neopost-dark.png"));
	}

	public NeopostToolbarButton(Icon icon) {
		super(icon);
		addActionListener(new OpenBrowserAction("http://www.neopost.com"));
		setOpaque(false);
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setToolTipText("Powered by Neopost");
	}
}
