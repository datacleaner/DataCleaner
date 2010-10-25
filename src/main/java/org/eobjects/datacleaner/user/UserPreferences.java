package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.Serializable;

public class UserPreferences implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final UserPreferences instance = new UserPreferences();

	private File openFileDir = new File(".");
	
	public static UserPreferences getInstance() {
		return instance;
	}
	
	private UserPreferences() {
		// prevent instantiation
	}

	public File getOpenFileDirectory() {
		return openFileDir;
	}
	
	public void setOpenFileDirectory(File openFileDir) {
		this.openFileDir = openFileDir;
	}
}
