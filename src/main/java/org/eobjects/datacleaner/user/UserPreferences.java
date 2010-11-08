package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.Serializable;

public class UserPreferences implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final UserPreferences instance = new UserPreferences();

	private File openDatastoreFileDirectory = new File(".");
	private File openPropertyFileDirectory = new File(".");
	private File saveFileDirectory = new File(".");

	public static UserPreferences getInstance() {
		return instance;
	}

	private UserPreferences() {
		// prevent instantiation
	}

	public File getOpenDatastoreFileDirectory() {
		return openDatastoreFileDirectory;
	}

	public void setOpenDatastoreFileDirectory(File openFileDir) {
		this.openDatastoreFileDirectory = openFileDir;
	}

	public File getOpenPropertyFileDirectory() {
		return openPropertyFileDirectory;
	}

	public void setOpenPropertyFileDirectory(File openPropertyFileDirectory) {
		this.openPropertyFileDirectory = openPropertyFileDirectory;
	}
	
	public File getSaveFileDirectory() {
		return saveFileDirectory;
	}
	
	public void setSaveFileDirectory(File saveFileDirectory) {
		this.saveFileDirectory = saveFileDirectory;
	}
}
