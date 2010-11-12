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

	public static final ExtensionFilter CSV = new ExtensionFilter("Comma-separated files (.csv)", ".csv");
	public static final ExtensionFilter TSV = new ExtensionFilter("Tab-separated files (.tsv)", ".tsv");
	public static final ExtensionFilter DAT = new ExtensionFilter("Data files (.dat)", ".dat");
	public static final ExtensionFilter TXT = new ExtensionFilter("Text files (.csv)", ".txt");
	public static final ExtensionFilter XLS = new ExtensionFilter("Excel 97-2003 spreadsheet (.xls)", ".xls");
	public static final ExtensionFilter XLSX = new ExtensionFilter("Excel spreadsheet (.xlsx)", ".xlsx");
	public static final ExtensionFilter MDB = new ExtensionFilter("Access database (.mdb)", ".mdb");

	public static final ExtensionFilter ANALYSIS_XML = new ExtensionFilter("Analysis job (.analysis.xml)", ".analysis.xml");

}
