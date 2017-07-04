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

import org.apache.commons.lang3.StringUtils;
import org.datacleaner.tenantloader.WorkspaceManager;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

/**
 * Workspace dialog. User can select workspace for start of DataCleaner
 */
public class WorkspaceConfigDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final WorkspaceManager _workspaceManager;

    private JList<String> workspaceList;
    private JButton buttonRemove;
    private JButton buttonSelectFolder;
    private JButton buttonStart;
    private JCheckBox checkBoxDontShowIt;
    private boolean shouldStart;
    private JXLabel label;

    public WorkspaceConfigDialog(JFrame owner, final WorkspaceManager workspaceManager) {
        super(owner, "Select Workspace", true);
        _workspaceManager = workspaceManager;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        createComponents();
        createLayout();
        loadData();
        createListeners();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createListeners() {
        buttonRemove.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String selectedValue = workspaceList.getSelectedValue();
                _workspaceManager.removeWorkspace(selectedValue);
                loadData();
            }
        });
        buttonStart.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                start();
            }
        });
        buttonSelectFolder.addActionListener(new FileChooserAction("Workspace selector", this));
        workspaceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() >= 2) {
                    start();
                }
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                "EscapeAction");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "EnterAction");
        getRootPane().getActionMap().put(
                "EscapeAction",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        dispose();
                    }
                });
        getRootPane().getActionMap().put(
                "EnterAction",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        start();
                    }
                });
    }

    private void start() {
        final String selectedValue = workspaceList.getSelectedValue();
        _workspaceManager.setWorkspaceToRun(selectedValue);
        _workspaceManager.setDefaultWorkspace(selectedValue);
        _workspaceManager.setShowDialog(!checkBoxDontShowIt.isSelected());
        shouldStart = true;
        dispose();
    }

    private void createComponents() {
        checkBoxDontShowIt = new JCheckBox("Don't show again");
        buttonRemove = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);
        buttonSelectFolder = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        buttonStart = WidgetFactory.createDefaultButton("Start", IconUtils.WEBSITE);
        workspaceList = new JXList();
        workspaceList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        workspaceList.setVisibleRowCount(7);
        label = new JXLabel("Previously selected workspaces");

        checkBoxDontShowIt.setFont(WidgetUtils.FONT_NORMAL);
        checkBoxDontShowIt.setOpaque(false);
        label.setFont(WidgetUtils.FONT_NORMAL);
        workspaceList.setFont(WidgetUtils.FONT_NORMAL);
        buttonStart.setFont(WidgetUtils.FONT_BUTTON);
    }

    private void createLayout() {
        JComponent panel = new JPanel();
        getRootPane().setLayout(new BorderLayout());
        getRootPane().add(panel);
        GroupLayout l = new GroupLayout(panel);
        panel.setLayout(l);
        panel.setBackground(Color.WHITE);
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane workspaceScroll = new JScrollPane(workspaceList);
        l.setHorizontalGroup(l.createParallelGroup()
                .addComponent(label)
                .addGroup(l.createSequentialGroup()
                        .addComponent(workspaceScroll, GroupLayout.DEFAULT_SIZE, 450, GroupLayout.DEFAULT_SIZE)
                        .addGap(4)
                        .addGroup(l.createParallelGroup()
                                .addComponent(buttonSelectFolder)
                                .addComponent(buttonRemove)
                        )
                )
                .addComponent(checkBoxDontShowIt)
                .addComponent(buttonStart, GroupLayout.Alignment.CENTER)

        );
        l.setVerticalGroup(l.createSequentialGroup()
                .addComponent(label)
                .addGap(4)
                .addGroup(l.createParallelGroup()
                        .addComponent(workspaceScroll)
                        .addGroup(l.createSequentialGroup()
                                .addComponent(buttonSelectFolder)
                                .addGap(3)
                                .addComponent(buttonRemove)
                        )
                )
                .addGap(5)
                .addComponent(checkBoxDontShowIt)
                .addGap(10)
                .addComponent(buttonStart)
        );

    }

    private void loadData() {
        final List<String> workspacePaths = _workspaceManager.getWorkspacePaths();
        final Vector<String> vector = new Vector<>();
        vector.addAll(workspacePaths);
        workspaceList.setListData(vector);
        if (workspacePaths.isEmpty()) {
            buttonRemove.setEnabled(false);
            buttonStart.setEnabled(false);
        } else {
            buttonRemove.setEnabled(true);
            buttonStart.setEnabled(true);
            String valueToSelect = _workspaceManager.getDefaultWorkspace();
            if(StringUtils.isNotBlank(valueToSelect)) {
                workspaceList.setSelectedValue(valueToSelect, true);
            }
            if(workspaceList.getSelectedIndex() < 0) {
                workspaceList.setSelectedIndex(0);
            }
        }
        checkBoxDontShowIt.setSelected(!_workspaceManager.showDialog());
    }

    public boolean isShouldStart() {
        return shouldStart;
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
