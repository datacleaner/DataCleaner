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

public class CombinationFilter extends FileFilter {

	private FileFilter[] _filters;

	public CombinationFilter(FileFilter... filters) {
		_filters = filters;
	}

	@Override
	public boolean accept(File f) {
		for (int i = 0; i < _filters.length; i++) {
			if (_filters[i].accept(f)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "All supported files";
	}

}