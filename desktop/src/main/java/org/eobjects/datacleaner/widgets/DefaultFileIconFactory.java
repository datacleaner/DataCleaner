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

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

/**
 * A {@link FileIconFactory} implementation that returns DataCleaner's icons for
 * file system elements.
 * 
 * @author Kasper Sørensen
 */
public class DefaultFileIconFactory implements FileIconFactory {

	private static final ImageManager imageManager = ImageManager.get();

	private static final ImageIcon ICON_FILE_CSV = imageManager.getImageIcon(IconUtils.CSV_IMAGEPATH,
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_ACCESS = imageManager.getImageIcon(IconUtils.ACCESS_IMAGEPATH,
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_DBASE = imageManager.getImageIcon(IconUtils.DBASE_IMAGEPATH,
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_EXCEL = imageManager.getImageIcon(IconUtils.EXCEL_IMAGEPATH,
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_ODB = imageManager.getImageIcon(IconUtils.ODB_IMAGEPATH,
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_ANALYSIS_JOB = imageManager.getImageIcon("images/filetypes/analysis_job.png",
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_ANALYSIS_RESULT = imageManager.getImageIcon("images/filetypes/analysis_result.png",
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_XML = imageManager.getImageIcon(IconUtils.XML_IMAGEPATH,
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE = imageManager.getImageIcon("images/filetypes/file.png",
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FILE_ARCHIVE = imageManager.getImageIcon("images/filetypes/archive.png",
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FOLDER = imageManager.getImageIcon("images/filetypes/folder.png",
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FOLDER_HIDDEN = imageManager.getImageIcon("images/filetypes/hidden-folder.png",
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FOLDER_DESKTOP = imageManager.getImageIcon("images/filetypes/desktop-folder.png",
			IconUtils.ICON_SIZE_MEDIUM);
	private static final ImageIcon ICON_FOLDER_HOME = imageManager.getImageIcon("images/filetypes/home-folder.png",
			IconUtils.ICON_SIZE_MEDIUM);

	private static final String USER_HOME_PATH = System.getProperty("user.home");
	private static final String DESKTOP_PATH = USER_HOME_PATH + File.separatorChar + "Desktop";

	@Override
	public Icon getIcon(File f) {
		final String name = f.getName().toLowerCase();
		if (f.isDirectory()) {
			final String absolutePath = f.getAbsolutePath();
			if (USER_HOME_PATH.equals(absolutePath)) {
				return ICON_FOLDER_HOME;
			}
			if (DESKTOP_PATH.equals(absolutePath)) {
				return ICON_FOLDER_DESKTOP;
			}
			if (name.startsWith(".") || f.isHidden()) {
				return ICON_FOLDER_HIDDEN;
			}
			return ICON_FOLDER;
		}
		if (name.endsWith(FileFilters.ANALYSIS_XML.getExtension())) {
			return ICON_FILE_ANALYSIS_JOB;
		}
		if (name.endsWith(FileFilters.ANALYSIS_RESULT_SER.getExtension())) {
			return ICON_FILE_ANALYSIS_RESULT;
		}
		if (name.endsWith(FileFilters.ODB.getExtension())) {
			return ICON_FILE_ODB;
		}
		if (name.endsWith(FileFilters.XML.getExtension())) {
			return ICON_FILE_XML;
		}
		if (name.endsWith(FileFilters.DBF.getExtension())) {
			return ICON_FILE_DBASE;
		}
		if (name.endsWith(FileFilters.CSV.getExtension()) || name.endsWith(FileFilters.TSV.getExtension())
				|| name.endsWith(FileFilters.DAT.getExtension()) || name.endsWith(FileFilters.TXT.getExtension())) {
			return ICON_FILE_CSV;
		}
		if (name.endsWith(FileFilters.MDB.getExtension()) || name.endsWith(FileFilters.ACCDB.getExtension())) {
			return ICON_FILE_ACCESS;
		}
		if (name.endsWith(FileFilters.XLS.getExtension()) || name.endsWith(FileFilters.XLSX.getExtension())) {
			return ICON_FILE_EXCEL;
		}
		if (name.endsWith(".zip") || name.endsWith(".tar") || name.endsWith(".gz") || name.endsWith(".jar")
				|| name.endsWith(".war") || name.endsWith(".ear")) {
			return ICON_FILE_ARCHIVE;
		}
		return ICON_FILE;
	}

}
