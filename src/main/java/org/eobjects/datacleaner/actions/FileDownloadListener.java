package org.eobjects.datacleaner.actions;

import java.io.File;

public interface FileDownloadListener {

	public void onFilesDownloaded(File[] files);
}
