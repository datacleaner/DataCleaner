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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.junit.Test;

public class FilenameTextFieldTest {

    @Test
    public void testAvoidListenerMutations() throws Exception {
        final FilenameTextField filenameTextField = new FilenameTextField(new File("."), true);

        // create some silly listeners that re-set the value of the widget.
        filenameTextField.addFileSelectionListener(new FileSelectionListener() {
            @Override
            public void onSelected(FilenameTextField filenameTextField, File file) {
                filenameTextField.setFile(file);
            }
        });
        filenameTextField.addListener(new ResourceTypePresenter.Listener() {

            @Override
            public void onResourceSelected(ResourceTypePresenter<?> presenter, Resource resource) {
                filenameTextField.setResource((FileResource) resource);
            }

            @Override
            public void onPathEntered(ResourceTypePresenter<?> presenter, String path) {
            }
        });

        filenameTextField.setFilename("src/foo.txt");
        assertEquals("src/foo.txt", filenameTextField.getFile().getPath().replace('\\', '/'));
    }

    @Test
    public void testDifferentListenersAllGetsNotification() throws Exception {
        final FilenameTextField filenameTextField = new FilenameTextField(new File("."), true);

        final AtomicBoolean fileSelectionListenerInvoked = new AtomicBoolean(false);
        final AtomicBoolean resourceListenerInvoked = new AtomicBoolean(false);

        // create some silly listeners that re-set the value of the widget.
        filenameTextField.addFileSelectionListener(new FileSelectionListener() {
            @Override
            public void onSelected(FilenameTextField filenameTextField, File file) {
                fileSelectionListenerInvoked.set(true);
            }
        });
        filenameTextField.addListener(new ResourceTypePresenter.Listener() {

            @Override
            public void onResourceSelected(ResourceTypePresenter<?> presenter, Resource resource) {
                resourceListenerInvoked.set(true);
            }

            @Override
            public void onPathEntered(ResourceTypePresenter<?> presenter, String path) {
            }
        });

        filenameTextField.setFilename("src/foo.txt");

        assertTrue(fileSelectionListenerInvoked.get());
        assertTrue(resourceListenerInvoked.get());
    }
}
