/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.extensionswap;

import java.io.File;

import org.apache.http.client.HttpClient;
import org.eobjects.datacleaner.actions.DownloadFilesActionListener;
import org.eobjects.datacleaner.actions.FileDownloadListener;
import org.eobjects.datacleaner.bootstrap.WindowManager;
import org.eobjects.datacleaner.user.ExtensionPackage;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.HttpXmlUtils;
import org.w3c.dom.Element;

public final class ExtensionSwapClient {

	private static final String DEFAULT_WEBSITE_HOSTNAME = "datacleaner.eobjects.org";

	private final HttpClient _httpClient;
	private final WindowManager _windowManager;
	private final String _baseUrl;

	public ExtensionSwapClient(WindowManager windowManager) {
		this(DEFAULT_WEBSITE_HOSTNAME, windowManager);
	}

	public ExtensionSwapClient(String websiteHostname, WindowManager windowManager) {
		this(HttpXmlUtils.getHttpClient(), websiteHostname, windowManager);
	}

	public ExtensionSwapClient(HttpClient httpClient, String websiteHostname, WindowManager windowManager) {
		_httpClient = httpClient;
		_windowManager = windowManager;
		_baseUrl = "http://" + websiteHostname + "/ws/extension/";
	}

	public ExtensionPackage registerExtensionPackage(ExtensionSwapPackage extensionSwapPackage, File jarFile) {
		String packageName = ExtensionPackage.autoDetectPackageName(jarFile);
		ExtensionPackage extensionPackage = new ExtensionPackage(extensionSwapPackage.getName(), packageName, true,
				new File[] { jarFile });
		UserPreferences.getInstance().getExtensionPackages().add(extensionPackage);
		return extensionPackage;
	}

	public ExtensionSwapPackage getExtensionSwapPackage(String id) {
		final Element rootNode = HttpXmlUtils.getRootNode(_httpClient, _baseUrl + id);
		final String name = HttpXmlUtils.getChildNodeText(rootNode, "name");
		return new ExtensionSwapPackage(id, name);
	}

	public void registerExtensionPackage(final ExtensionSwapPackage extensionSwapPackage) {
		downloadJarFile(extensionSwapPackage, new FileDownloadListener() {
			@Override
			public void onFilesDownloaded(File[] files) {
				File jarFile = files[0];
				registerExtensionPackage(extensionSwapPackage, jarFile);
			}
		});
	}

	private void downloadJarFile(ExtensionSwapPackage extensionSwapPackage, FileDownloadListener listener) {
		String url = _baseUrl + extensionSwapPackage.getId() + "/jarfile";
		String filename = extensionSwapPackage.getId() + ".jar";
		DownloadFilesActionListener actionListener = new DownloadFilesActionListener(new String[] { url },
				new String[] { filename }, listener, _windowManager);
		actionListener.actionPerformed(null);
	}
}
