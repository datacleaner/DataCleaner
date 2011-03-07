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
package org.eobjects.datacleaner.widgets.tooltip;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JMenu;

import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

/**
 * A menu folder used to group together similar descriptors, eg "coalesce"
 * transformers or "conversion" transformers.
 * 
 * @author Kasper Sørensen
 */
public class DescriptorMenu extends JMenu {

	private static final long serialVersionUID = 1L;

	private static final String FOLDER_IMAGE_PATH = "images/filetypes/folder.png";

	private static final ImageManager imageManager = ImageManager.getInstance();
	private final Set<Class<?>> _componentClasses;

	public DescriptorMenu(String name) {
		super(name);
		setIcon(imageManager.getImageIcon(FOLDER_IMAGE_PATH, IconUtils.ICON_SIZE_SMALL));
		_componentClasses = new HashSet<Class<?>>();
	}

	public DescriptorMenu addComponentClass(Class<?> clazz) {
		_componentClasses.add(clazz);
		return this;
	}

	public DescriptorMenu addComponentClasses(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			addComponentClass(clazz);
		}
		return this;
	}

	public boolean containsComponentClass(Class<?> clazz) {
		return _componentClasses.contains(clazz);
	}

	public DescriptorMenu setIconDecoration(String imagePath) {
		int totalSize = IconUtils.ICON_SIZE_SMALL;
		Image decoration = imageManager.getImage(imagePath, totalSize / 2);
		Image folderIcon = imageManager.getImage(FOLDER_IMAGE_PATH, totalSize);

		BufferedImage bufferedImage = new BufferedImage(totalSize, totalSize, BufferedImage.TYPE_INT_ARGB);
		bufferedImage.getGraphics().drawImage(folderIcon, 0, 0, null);
		bufferedImage.getGraphics().drawImage(decoration, totalSize / 2, totalSize / 2, null);
		setIcon(new ImageIcon(bufferedImage));
		return this;
	}
}
