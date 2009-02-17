/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;

public abstract class BanneredDialog extends JDialog {

	private static final long serialVersionUID = 6803739986587937688L;
	protected final Log _log = LogFactory.getLog(getClass());

	public BanneredDialog() {
		this(400, 500);
	}

	public BanneredDialog(int width, int height) {
		super(DataCleanerGui.getMainWindow().getFrame());
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle(getDialogTitle());
		setLayout(new BorderLayout());
		setSize(width, height);
		setResizable(true);
		add(getBanner(), BorderLayout.NORTH);
		add(getContent(), BorderLayout.CENTER);
		GuiHelper.centerOnScreen(this);
	}

	protected Component getBanner() {
		// Creates a panel with the background tiled/repeated along the x-axis.
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
			private static final long serialVersionUID = -9014625499488304280L;

			@Override
			protected void paintComponent(Graphics g) {
				Icon backgroundIcon = GuiHelper
						.getImageIcon("images/dialog_banner_bg.png");
				if (isOpaque()) {
					super.paintComponent(g);
				}
				Dimension size = getSize();
				for (int x = 0; x < size.width; x = x
						+ backgroundIcon.getIconWidth()) {
					backgroundIcon.paintIcon(this, g, x, 0);
				}
			}
		};
		panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
		Icon logo = GuiHelper.getImageIcon(getBannerIconLabel());
		JLabel label = new JLabel(logo);
		panel.add(label, 0, 0);
		return panel;
	}

	/**
	 * Override this method to provide custom banner headers
	 * 
	 * @return
	 */
	protected String getBannerIconLabel() {
		return "images/dialog_banner.png";
	}

	protected abstract Component getContent();

	protected abstract String getDialogTitle();
}