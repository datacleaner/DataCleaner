package org.eobjects.datacleaner.database;

import java.io.Serializable;

/**
 * An object that provides information about a JDBC database, which will aid the
 * user in selecting correct driver classes, filling out the connection URL etc.
 * 
 * @author Kasper Sørensen
 */
public interface DatabaseDriverDescriptor extends Serializable, Comparable<DatabaseDriverDescriptor> {

	public String getDisplayName();

	public String getIconImagePath();

	public String getDriverClassName();

	/**
	 * @return an array of URLs for the files needed to download to use this
	 *         driver. Typically this will just be a single file (a driver JAR),
	 *         but in some cases the driver has additional dependencies, which
	 *         also needs to be downloaded.
	 */
	public String[] getDownloadUrls();

	public String[] getConnectionUrlTemplates();
}
