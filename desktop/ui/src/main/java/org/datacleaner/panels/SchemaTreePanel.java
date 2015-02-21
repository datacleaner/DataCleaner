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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.datacleaner.connection.Datastore;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.LoadingIcon;
import org.datacleaner.widgets.tree.SchemaTree;

import com.google.inject.Injector;

public class SchemaTreePanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final InjectorBuilder _injectorBuilder;
	private JComponent _updatePanel;

	@Inject
	protected SchemaTreePanel(InjectorBuilder injectorBuilder) {
		super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
		_injectorBuilder = injectorBuilder;
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setDatastore(null, false);
	}

	public void setDatastore(final Datastore datastore, final boolean expandTree) {
		removeAll();
		if (datastore == null) {
			add(new DCPanel().setPreferredSize(150, 150), BorderLayout.CENTER);
			return;
		}

		add(new LoadingIcon().setPreferredSize(150, 150), BorderLayout.CENTER);

		// load the schema tree in the background because it will retrieve
		// metadata about the datastore (might take several seconds)
		new SwingWorker<SchemaTree, Void>() {
			@Override
			protected SchemaTree doInBackground() throws Exception {
				Injector injector = _injectorBuilder.with(Datastore.class, datastore).createInjector();
				SchemaTree tree = injector.getInstance(SchemaTree.class);
				return tree;
			}

			protected void done() {
				try {
					SchemaTree schemaTree = get();
					final JScrollPane schemaTreeScroll = WidgetUtils.scrolleable(schemaTree);
					schemaTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					schemaTree.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							updateParentPanel();
						}

					});
					removeAll();
					add(schemaTreeScroll, BorderLayout.CENTER);
					updateParentPanel();
					if (expandTree) {
					    schemaTree.expandSelectedData();
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		}.execute();
	}

	private void updateParentPanel() {
		if (_updatePanel != null) {
			_updatePanel.updateUI();
		}
	}

	public void setUpdatePanel(JComponent updatePanel) {
		_updatePanel = updatePanel;
	}
}
