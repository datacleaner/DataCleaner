package org.eobjects.datacleaner.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

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

	public static final FileFilter CSV = new ExtensionFilter("Comma-separated files (.csv)", ".csv");
	public static final FileFilter TSV = new ExtensionFilter("Tab-separated files (.tsv)", ".tsv");
	public static final FileFilter DAT = new ExtensionFilter("Data files (.dat)", ".dat");
	public static final FileFilter TXT = new ExtensionFilter("Text files (.csv)", ".txt");
	public static final FileFilter XLS = new ExtensionFilter("Excel 97-2003 spreadsheet (.xls)", ".xls");
	public static final FileFilter XLSX = new ExtensionFilter("Excel spreadsheet (.xlsx)", ".xlsx");

}
