package org.eobjects.datacleaner.widgets;

import java.io.File;

public interface FileSelectionListener {

	void onSelected(FilenameTextField filenameTextField, File file);
}
