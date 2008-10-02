/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.model;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter for JFileChooser's which filters on behalf of the file
 * extension
 */
public class ExtensionFilter extends FileFilter {

	private String _desc;
	private String _extension;

	public ExtensionFilter(String desc, String extension) {
		_desc = desc;
		_extension = extension.toLowerCase();
	}

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String fileExtension = getExtention(f);

		if (fileExtension != null) {
			if (fileExtension.equals(_extension)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getDescription() {
		return _desc;
	}

	public static String getExtention(File file) {
		if (file != null) {
			String temp = file.getName();
			int i = temp.lastIndexOf('.');
			if (i != -1) {
				return (temp.substring(i + 1, temp.length()));
			}
		}
		return null;
	}
}