package org.eobjects.datacleaner.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FileFilters {

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

	public static final FileFilter CSV = new ExtensionFilter("Comma-separated files (.csv)", ".csv");
	public static final FileFilter TSV = new ExtensionFilter("Tab-separated files (.tsv)", ".tsv");

}
