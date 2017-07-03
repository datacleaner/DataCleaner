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
package org.datacleaner.windows;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import org.datacleaner.tenantloader.WorkspaceManager;
import org.datacleaner.util.WidgetFactory;

/**
 * Workspace dialog. User can select workspace for start of DataCleaner
 */
public class WorkspaceConfigDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final WorkspaceManager _workspaceManager;

    private JList<String> _workspaceList;
    private JButton _buttonRemove;
    private JButton _buttonSelectFolder;
    private JButton _buttonStart;
    private JCheckBox _checkBoxDontShowIt;

    public WorkspaceConfigDialog(JFrame owner, final WorkspaceManager workspaceManager) {
        super(owner);
        _workspaceManager = workspaceManager;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        createLayout();
        loadData();
        pack();
        setVisible(true);
    }

    private void createLayout() {
        createButtons();
        setLayout(new FlowLayout());
        add(new JScrollPane(createList()));
        add(_buttonSelectFolder);
        add(_buttonRemove);
        add(_buttonStart);
        _checkBoxDontShowIt = new JCheckBox("Don't show it again.");
        add(_checkBoxDontShowIt);

    }

    private void loadData() {
        final List<String> workspacePaths = _workspaceManager.getWorkspacePaths();
        final Vector<String> vector = new Vector<>();
        vector.addAll(workspacePaths);
        _workspaceList.setListData(vector);
        if (workspacePaths.isEmpty()) {
            _buttonRemove.setEnabled(false);
            _buttonStart.setEnabled(false);
        } else {
            _buttonRemove.setEnabled(true);
            _buttonStart.setEnabled(true);
            _workspaceList.setSelectedIndex(0);
        }
        _workspaceList.repaint();
    }

    private JList<String> createList() {
        _workspaceList = new JList<>();
        _workspaceList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _workspaceList.setFixedCellHeight(80);
        _workspaceList.setFixedCellWidth(500);
        _workspaceList.setVisibleRowCount(10);
        return _workspaceList;
    }

    private void createButtons() {
        _buttonRemove = WidgetFactory.createDefaultButton("remove");
        _buttonRemove.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String selectedValue = _workspaceList.getSelectedValue();
                _workspaceManager.removeWorkspace(selectedValue);
                loadData();
            }
        });

        _buttonSelectFolder = WidgetFactory.createDefaultButton("Select new workspace");
        _buttonSelectFolder.addActionListener(new FileChooserAction("Workspace selector", this));

        _buttonStart = WidgetFactory.createDefaultButton("Start");
        _buttonStart.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String selectedValue = _workspaceList.getSelectedValue();
                _workspaceManager.setWorkspaceToRun(selectedValue);
                if (_checkBoxDontShowIt.isSelected()) {
                    _workspaceManager.setDefaultWorkspace(selectedValue);
                    _workspaceManager.setShowDialog(false);
                }
                dispose();
            }
        });
    }

    private static class FileChooserAction extends AbstractAction {
        private final WorkspaceConfigDialog _parent;

        public FileChooserAction(final String name, final WorkspaceConfigDialog parent) {
            super(name);
            _parent = parent;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new java.io.File("."));
            fileChooser.setDialogTitle("Select workspace");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(_parent) == JFileChooser.APPROVE_OPTION) {
                _parent._workspaceManager.addWorkspacePath(fileChooser.getSelectedFile().getAbsolutePath());
                _parent.loadData();
            }
        }
    }
}
