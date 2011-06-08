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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.beans.api.ComponentCategory;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Popup menu that groups together bean descriptors by their shared
 * {@link ComponentCategory}.
 * 
 * @author Kasper Sørensen
 * 
 * @param <E>
 */
public abstract class DescriptorPopupMenu<E extends BeanDescriptor<?>> extends JPopupMenu {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DescriptorPopupMenu.class);
	private final List<? extends E> _descriptors;

	public DescriptorPopupMenu(Collection<? extends E> descriptors) {
		_descriptors = CollectionUtils.sorted(descriptors, new DisplayNameComparator());

		initialize();
	}

	private void initialize() {
		final Map<ComponentCategory, DescriptorMenu> descriptorMenus = new HashMap<ComponentCategory, DescriptorMenu>();

		// build sub menus
		{
			for (E descriptor : _descriptors) {
				Set<ComponentCategory> componentCategories = descriptor.getComponentCategories();
				for (ComponentCategory componentCategory : componentCategories) {
					DescriptorMenu menu = descriptorMenus.get(componentCategory);
					if (menu == null) {
						menu = new DescriptorMenu(componentCategory);
						descriptorMenus.put(componentCategory, menu);
					}
					menu.addComponentClass(descriptor.getComponentClass());
				}
			}
		}

		{
			// place sub menus
			List<DescriptorMenu> sortedMenus = CollectionUtils.sorted(descriptorMenus.values());
			for (DescriptorMenu descriptorMenu : sortedMenus) {
				int count = descriptorMenu.getComponentClassCount();
				if (count <= 1) {
					// disregard categories with only a single component in
					// them!
					ComponentCategory category = descriptorMenu.getComponentCategory();
					logger.info("Disregarding menu for category '{}' because of too few components ({})", category, count);
					descriptorMenus.remove(category);
				} else {
					// add menu
					add(descriptorMenu);
				}
			}
		}

		// place items that are not in any submenus
		{
			for (final E descriptor : _descriptors) {
				boolean placedInSubmenu = false;
				Class<?> componentClass = descriptor.getComponentClass();
				JMenuItem menuItem = createMenuItem(descriptor);
				if (menuItem != null) {
					for (DescriptorMenu descriptorMenu : descriptorMenus.values()) {
						if (descriptorMenu.containsComponentClass(componentClass)) {
							descriptorMenu.add(menuItem);
							placedInSubmenu = true;

							// create a new menu item (or else it will be moved
							// instead of added.
							menuItem = createMenuItem(descriptor);
						}
					}

					if (!placedInSubmenu) {
						// add menu item
						add(menuItem);
					}
				}
			}
		}
	}

	protected abstract JMenuItem createMenuItem(E descriptor);

}
