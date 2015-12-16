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

import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;

import org.apache.metamodel.util.Resource;

/**
 * Interface for widgets that present a {@link Resource} to the user and
 * possibly allows him to edit it
 * 
 * @param <R>
 *            the type of resource
 */
public interface ResourceTypePresenter<R extends Resource> {

    public static interface Listener {

        /**
         * Obverser method called when a {@link Resource} is selected or a path
         * that was entered has been parsed and recognized.
         * 
         * @param presenter
         * @param resource
         */
        public void onResourceSelected(ResourceTypePresenter<?> presenter, Resource resource);

        /**
         * Obverser method called whenever the user enters a path which isn't
         * recognized as a ready-to-use {@link Resource}. Listeners may choose
         * to act on this path in order to process the path anyway.
         * 
         * @param presenter
         * @param path
         */
        public void onPathEntered(ResourceTypePresenter<?> presenter, String path);
    }

    public JComponent getWidget();

    public R getResource();

    public void setResource(R resource);

    public void addListener(Listener listener);

    public void removeListener(Listener listener);

    public void addChoosableFileFilter(FileFilter fileFilter);

    public void removeChoosableFileFilter(FileFilter fileFilter);

    public void setSelectedFileFilter(FileFilter fileFilter);
}
