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

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.analyzer.reference.DatastoreSynonymCatalog;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.ReferenceData;
import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.reference.TextFileDictionary;
import org.eobjects.analyzer.reference.TextFileSynonymCatalog;
import org.eobjects.datacleaner.regexswap.RegexSwapStringPattern;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

public class ReferenceDataComboBoxListRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;
	private static final ImageManager imageManager = ImageManager.getInstance();

	private String nullText = "- none -";

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value == null) {
			result.setText(getNullText());
		} else if (value instanceof ReferenceData) {
			result.setText(((ReferenceData) value).getName());

			String iconPath = null;
			if (value instanceof Dictionary) {
				iconPath = IconUtils.DICTIONARY_IMAGEPATH;
				if (value instanceof SimpleDictionary) {
					iconPath = IconUtils.DICTIONARY_SIMPLE_IMAGEPATH;
				} else if (value instanceof TextFileDictionary) {
					iconPath = IconUtils.DICTIONARY_TEXTFILE_IMAGEPATH;
				} else if (value instanceof DatastoreDictionary) {
					iconPath = IconUtils.DICTIONARY_DATASTORE_IMAGEPATH;
				}
			} else if (value instanceof SynonymCatalog) {
				iconPath = IconUtils.SYNONYM_CATALOG_IMAGEPATH;
				if (value instanceof TextFileSynonymCatalog) {
					iconPath = IconUtils.SYNONYM_CATALOG_TEXTFILE_IMAGEPATH;
				} else if (value instanceof DatastoreSynonymCatalog) {
					iconPath = IconUtils.SYNONYM_CATALOG_DATASTORE_IMAGEPATH;
				}
			} else if (value instanceof StringPattern) {
				iconPath = IconUtils.STRING_PATTERN_IMAGEPATH;
				if (value instanceof SimpleStringPattern) {
					iconPath = IconUtils.STRING_PATTERN_SIMPLE_IMAGEPATH;
				} else if (value instanceof RegexStringPattern) {
					iconPath = IconUtils.STRING_PATTERN_REGEX_IMAGEPATH;
				} else if (value instanceof RegexSwapStringPattern) {
					iconPath = IconUtils.STRING_PATTERN_REGEXSWAP_IMAGEPATH;
				}
			}

			if (iconPath != null) {
				result.setIcon(imageManager.getImageIcon(iconPath, IconUtils.ICON_SIZE_SMALL));
			}
		}

		return result;
	}

	public void setNullText(String nullText) {
		this.nullText = nullText;
	}

	public String getNullText() {
		return nullText;
	}
}
