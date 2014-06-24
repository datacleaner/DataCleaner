/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.SchemaComparator;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.NamedStructure;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

public class SchemaStructureComboBoxListRenderer extends DCListCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final ImageManager imageManager = ImageManager.get();

	private boolean indentEnabled;
	private String nullText = "- none -";

	public SchemaStructureComboBoxListRenderer() {
		this(true);
	}
	
	public SchemaStructureComboBoxListRenderer(boolean indentsEnabled) {
		indentEnabled = indentsEnabled;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		int indent = 0;
		Icon icon = null;

		if (value == null) {
			result.setText(getNullText());
		} else if (value instanceof NamedStructure) {
			result.setText(((NamedStructure) value).getName());
			if (value instanceof Schema) {
				icon = imageManager.getImageIcon("images/model/schema.png", IconUtils.ICON_SIZE_SMALL);
				if (SchemaComparator.isInformationSchema((Schema) value)) {
					icon = imageManager.getImageIcon("images/model/schema_information.png", IconUtils.ICON_SIZE_SMALL);
				} else {
					icon = imageManager.getImageIcon("images/model/schema.png", IconUtils.ICON_SIZE_SMALL);
				}
			} else if (value instanceof Table) {
				icon = imageManager.getImageIcon("images/model/table.png", IconUtils.ICON_SIZE_SMALL);
				indent = 10;
			} else if (value instanceof Column) {
				icon = imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
				indent = 20;
			}
		} else if (value instanceof Datastore) {
			Datastore datastore = (Datastore) value;
			icon = IconUtils.getDatastoreIcon(datastore, IconUtils.ICON_SIZE_SMALL);
			result.setText(datastore.getName());
		} else if (value instanceof InputColumn<?>) {
			result.setText(((InputColumn<?>) value).getName());
			icon = imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
		}

		if (icon != null) {
			result.setIcon(icon);
		}
		if (isIndentEnabled()) {
			result.setBorder(new EmptyBorder(0, indent, 0, 0));
		}

		return result;
	}

	public void setIndentEnabled(boolean indent) {
		this.indentEnabled = indent;
	}

	public boolean isIndentEnabled() {
		return indentEnabled;
	}

	public void setNullText(String nullText) {
		this.nullText = nullText;
	}

	public String getNullText() {
		return nullText;
	}
}
