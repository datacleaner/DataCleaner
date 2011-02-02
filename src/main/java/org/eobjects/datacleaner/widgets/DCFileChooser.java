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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetUtils;

/**
 * An extension of the normal JFileChooser, applying various minor tweaks to the
 * presentation of files etc.
 * 
 * @author Kasper Sørensen
 */
public class DCFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();
	private static final String USER_HOME_PATH = System.getProperty("user.home");
	private static final String DESKTOP_PATH = USER_HOME_PATH + File.separatorChar + "Desktop";

	public DCFileChooser() {
		this(null);
	}

	public DCFileChooser(File dir) {
		super(dir);
		setPreferredSize(new Dimension(600, 550));
		setFilePaneBackground(WidgetUtils.BG_COLOR_BRIGHTEST);
		setAcceptAllFileFilterUsed(false);
	}

	public void setFilePaneBackground(Color bg) {
		setFilePaneBackground(this, bg);
	}

	private void setFilePaneBackground(Container container, Color bg) {
		Component[] children = container.getComponents();
		for (Component component : children) {
			if (component instanceof JScrollPane) {
				// the "file pane" (the component containing the list of files)
				// is placed inside a JScrollPane
				JScrollPane scroll = (JScrollPane) component;
				setContainerBackground(scroll.getComponent(0), bg);
			} else if (component instanceof Container) {
				setFilePaneBackground((Container) component, bg);
			}
		}
	}

	private void setContainerBackground(Component component, Color bg) {
		if (component instanceof Container) {
			Container c = (Container) component;
			// drill further down the tree
			Component child = c.getComponent(0);
			if (child instanceof Container) {
				Container childContainer = (Container) child;
				if (childContainer.getComponentCount() == 1) {
					setContainerBackground(childContainer, bg);
				}
			}
		}
		component.setBackground(bg);
	}

	@Override
	public Icon getIcon(File f) {
		if (f.isDirectory()) {
			if (USER_HOME_PATH.equals(f.getAbsolutePath())) {
				return imageManager.getImageIcon("images/filetypes/home-folder.png");
			}
			if (DESKTOP_PATH.equals(f.getAbsolutePath())) {
				return imageManager.getImageIcon("images/filetypes/desktop-folder.png");
			}
			if (f.getName().startsWith(".") || f.isHidden()) {
				return imageManager.getImageIcon("images/filetypes/hidden-folder.png");
			}
			return imageManager.getImageIcon("images/filetypes/folder.png");
		}
		String name = f.getName().toLowerCase();
		if (name.endsWith(FileFilters.CSV.getExtension()) || name.endsWith(FileFilters.TSV.getExtension())
				|| name.endsWith(FileFilters.DAT.getExtension()) || name.endsWith(FileFilters.TXT.getExtension())) {
			return imageManager.getImageIcon(IconUtils.CSV_IMAGEPATH);
		}
		if (name.endsWith(FileFilters.MDB.getExtension())) {
			return imageManager.getImageIcon(IconUtils.ACCESS_IMAGEPATH);
		}
		if (name.endsWith(FileFilters.DBF.getExtension())) {
			return imageManager.getImageIcon(IconUtils.DBASE_IMAGEPATH);
		}
		if (name.endsWith(FileFilters.XLS.getExtension()) || name.endsWith(FileFilters.XLSX.getExtension())) {
			return imageManager.getImageIcon(IconUtils.EXCEL_IMAGEPATH);
		}
		if (name.endsWith(FileFilters.ODB.getExtension())) {
			return imageManager.getImageIcon(IconUtils.ODB_IMAGEPATH);
		}
		if (name.endsWith(FileFilters.ANALYSIS_XML.getExtension())) {
			return imageManager.getImageIcon("images/filetypes/analysis_job.png");
		}
		if (name.endsWith(FileFilters.XML.getExtension())) {
			return imageManager.getImageIcon(IconUtils.XML_IMAGEPATH);
		}
		if (name.endsWith(".zip") || name.endsWith(".tar") || name.endsWith(".gz") || name.endsWith(".jar")
				|| name.endsWith(".war") || name.endsWith(".ear")) {
			return imageManager.getImageIcon("images/filetypes/archive.png");
		}
		return imageManager.getImageIcon("images/filetypes/file.png");
	}

	public static void main(String[] args) {
		LookAndFeelManager.getInstance().init();
		DCFileChooser fc = new DCFileChooser();
		fc.showOpenDialog(null);
	}
}
