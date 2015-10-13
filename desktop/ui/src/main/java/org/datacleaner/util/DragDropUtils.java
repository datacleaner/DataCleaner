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
package org.datacleaner.util;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;

public class DragDropUtils {

    public static final DataFlavor MODEL_DATA_FLAVOR;

    static {
        try {
            MODEL_DATA_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
                    + Object.class.getName() + "\"");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class ModelDataTransferable implements Transferable {

        private final Object _modelObject;

        public ModelDataTransferable(Object modelObject) {
            _modelObject = modelObject;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == MODEL_DATA_FLAVOR;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { MODEL_DATA_FLAVOR };
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor != MODEL_DATA_FLAVOR) {
                return null;
            }
            return _modelObject;
        }
    };

    public static TransferHandler createSourceTransferHandler() {
        return new TransferHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                final Object modelObject = getModelObject(c);
                if (modelObject == null) {
                    return null;
                }

                final Transferable transferable = new ModelDataTransferable(modelObject);

                if (modelObject instanceof Column) {
                    final Image image = ImageManager.get().getImage(IconUtils.MODEL_COLUMN);
                    setDragImage(image);
                } else if (modelObject instanceof Table) {
                    final Image image = ImageManager.get().getImage(IconUtils.MODEL_TABLE);
                    setDragImage(image);
                }

                return transferable;
            }
        };
    }

    protected static Object getModelObject(JComponent c) {
        if (c instanceof JTree) {
            final TreePath path = ((JTree) c).getSelectionPath();
            if (path != null) {
                final Object pathComponent = path.getLastPathComponent();
                if (pathComponent instanceof DefaultMutableTreeNode) {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathComponent;
                    final Object userObject = node.getUserObject();
                    return userObject;
                }
            }
        }
        return null;
    }
}
