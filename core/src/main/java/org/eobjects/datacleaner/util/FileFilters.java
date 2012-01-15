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
package org.eobjects.datacleaner.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Contains commonly used file filters.
 * 
 * @author Kasper Sørensen
 */
public final class FileFilters {

	private FileFilters() {
		// prevent instantiation
	}

	public static final FileFilter ALL = new FileFilter() {

		@Override
		public boolean accept(File f) {
			return true;
		}

		@Override
		public String getDescription() {
			return "All files";
		}
	};

	public static FileFilter combined(final String description, final FileFilter... fileFilters) {
		return new FileFilter() {
			@Override
			public boolean accept(File f) {
				for (FileFilter fileFilter : fileFilters) {
					if (fileFilter.accept(f)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
	}

	public static final ExtensionFilter CSV = new ExtensionFilter("Comma-separated files (.csv)", ".csv");
	public static final ExtensionFilter TSV = new ExtensionFilter("Tab-separated files (.tsv)", ".tsv");
	public static final ExtensionFilter DAT = new ExtensionFilter("Data files (.dat)", ".dat");
	public static final ExtensionFilter TXT = new ExtensionFilter("Text files (.txt)", ".txt");
	public static final ExtensionFilter XLS = new ExtensionFilter("Excel 97-2003 spreadsheet (.xls)", ".xls");
	public static final ExtensionFilter XLSX = new ExtensionFilter("Excel spreadsheet (.xlsx)", ".xlsx");
	public static final ExtensionFilter MDB = new ExtensionFilter("Access 97-2003 database (.mdb)", ".mdb");
	public static final ExtensionFilter ACCDB = new ExtensionFilter("Access database (.accdb)", ".accdb");
	public static final ExtensionFilter DBF = new ExtensionFilter("Dbase database (.dbf)", ".dbf");
	public static final ExtensionFilter ODB = new ExtensionFilter("OpenOffice.org database (.odb)", ".odb");
	public static final ExtensionFilter XML = new ExtensionFilter("Extensible Markup Language (.xml)", ".xml");

	public static final ExtensionFilter ANALYSIS_XML = new ExtensionFilter("Analysis job (.analysis.xml)", ".analysis.xml");
	public static final ExtensionFilter ANALYSIS_RESULT_SER = new ExtensionFilter("Analysis result (.analysis.result.dat)", ".analysis.result.dat");

}
