package org.eobjects.datacleaner.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eobjects.datacleaner.windows.AbstractWindow;
import org.eobjects.datacleaner.windows.MainWindow;

public final class WindowManager {

	private static WindowManager instance = new WindowManager();

	private List<AbstractWindow> windows = new ArrayList<AbstractWindow>();

	public static WindowManager getInstance() {
		return instance;
	}

	private WindowManager() {
	}

	public List<AbstractWindow> getWindows() {
		return Collections.unmodifiableList(windows);
	}

	public MainWindow getMainWindow() {
		for (AbstractWindow window : windows) {
			if (window instanceof MainWindow) {
				return (MainWindow) window;
			}
		}
		throw new IllegalStateException("The main window appears to be missing!");
	}

	public void onDispose(AbstractWindow window) {
		windows.remove(window);
	}

	public void onShow(AbstractWindow window) {
		windows.add(window);
	}
}
