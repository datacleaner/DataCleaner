package org.eobjects.datacleaner.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter for JFileChooser's which filters on behalf of the file
 * extension
 */
public class ExtensionFilter extends FileFilter {

	private final String _desc;
	private final String _extension;

	public ExtensionFilter(String desc, String extension) {
		_desc = desc;
		_extension = extension.toLowerCase();
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String fileName = f.getAbsolutePath();
		if (fileName.length() < _extension.length()) {
			return false;
		}

		fileName = fileName.substring(fileName.length() - _extension.length());

		if (fileName.equalsIgnoreCase(_extension)) {
			return true;
		}

		return false;
	}

	@Override
	public String getDescription() {
		return _desc;
	}
}