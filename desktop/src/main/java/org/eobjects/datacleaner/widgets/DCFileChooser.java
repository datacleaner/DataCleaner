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
package org.eobjects.datacleaner.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eobjects.analyzer.util.VFSUtils;
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

    private static final FileIconFactory _defaultFileIconFactory = new DefaultFileIconFactory();
    private FileIconFactory _fileIconFactory;

    public DCFileChooser() {
        this((File) null);
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

    public FileObject getSelectedFileObject() {
        File selectedFile = getSelectedFile();
        if (selectedFile == null) {
            return null;
        }
        try {
            return VFSUtils.getFileSystemManager().toFileObject(selectedFile);
        } catch (FileSystemException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Icon getIcon(File f) {
        return getFileIconFactory().getIcon(f);
    }

    public static void main(String[] args) {
        LookAndFeelManager.get().init();
        DCFileChooser fc = new DCFileChooser();
        fc.showOpenDialog(null);
    }

    public FileIconFactory getFileIconFactory() {
        if (_fileIconFactory == null) {
            return _defaultFileIconFactory;
        }
        return _fileIconFactory;
    }
}
