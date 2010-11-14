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
package org.eobjects.datacleaner.util;

import java.awt.Image;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;

/**
 * Factory class for various commonly used widgets in DataCleaner. Typically the
 * factory is being used to cut down boilerplate code for typical features such
 * as setting mnemonics, tooltips etc.
 * 
 * @author Kasper Sørensen
 */
public final class WidgetFactory {

	public static JMenu createMenu(String text, char mnemonic) {
		JMenu menu = new JMenu();
		menu.setToolTipText(text);
		menu.setText(text);
		menu.setMnemonic(mnemonic);
		return menu;
	}

	public static JMenuItem createMenuItem(String text, String iconPath) {
		JMenuItem menu = new JMenuItem();
		menu.setToolTipText(text);
		menu.setText(text);
		if (iconPath != null) {
			menu.setIcon(ImageManager.getInstance().getImageIcon(iconPath));
		}
		return menu;
	}

	public static JButton createButton(String text, String imagePath) {
		JButton b = new JButton(text);
		if (imagePath != null) {
			b.setIcon(ImageManager.getInstance().getImageIcon(imagePath));
		}
		return b;
	}

	public static JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setRollover(true);
		toolbar.setFloatable(false);
		toolbar.setAlignmentY(JToolBar.LEFT_ALIGNMENT);
		return toolbar;
	}

	public static JButton createSmallButton(String imagePath) {
		Image image = ImageManager.getInstance().getImage(imagePath, 16);
		ImageIcon imageIcon = new ImageIcon(image);
		JButton button = new JButton(imageIcon);
		button.setMargin(new Insets(0, 0, 0, 0));
		return button;
	}

	public static JXTaskPaneContainer createTaskPaneContainer() {
		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		taskPaneContainer.setOpaque(false);
		taskPaneContainer.setBackgroundPainter(null);
		return taskPaneContainer;
	}

	public static JXTextField createTextField() {
		return createTextField(null);
	}

	public static JXTextField createTextField(String promptText) {
		JXTextField tf = new JXTextField(promptText);
		tf.setColumns(17);
		return tf;
	}

	public static JXTextArea createTextArea(String promptText) {
		JXTextArea ta = new JXTextArea(promptText);
		ta.setColumns(17);
		return ta;
	}
}
