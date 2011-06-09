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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.eobjects.datacleaner.DefaultExitActionListener;
import org.eobjects.datacleaner.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExtensionSwapInstallationServiceImpl implements ExtensionSwapInstallationService {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ExtensionSwapInstallationServiceImpl.class);

	private final WindowManager _windowManager;

	public ExtensionSwapInstallationServiceImpl(WindowManager windowManager) {
		_windowManager = windowManager;
	}

	public static void main(String[] args) throws Throwable {
		JFrame frame = new JFrame();
		frame.setVisible(true);
		JOptionPane.setRootFrame(frame);
		ExtensionSwapInstallationServiceImpl service = new ExtensionSwapInstallationServiceImpl(new WindowManager(
				new DefaultExitActionListener()));
		service.startListener();
	}

	public void startListener() throws RemoteException {
		ExtensionSwapInstallationService service = new ExtensionSwapInstallationServiceImpl(_windowManager);
		Remote stub = UnicastRemoteObject.exportObject(service, 0);

		Registry registry = LocateRegistry.createRegistry(DEFAULT_PORT);
		registry.rebind(ExtensionSwapInstallationService.class.getSimpleName(), stub);
	}

	@Override
	public void initiateTransfer(String extensionId) {
		logger.info("Initiating transfer of extension: {}", extensionId);

		final ExtensionSwapClient client = new ExtensionSwapClient(_windowManager);
		final ExtensionSwapPackage extensionSwapPackage = client.getExtensionSwapPackage(extensionId);
		logger.info("Fetched ExtensionSwap package: {}", extensionSwapPackage);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int confirmation = JOptionPane.showConfirmDialog(null, "Do you want to download and install the extension '"
						+ extensionSwapPackage.getName() + "'");

				if (confirmation == JOptionPane.YES_OPTION) {
					client.registerExtensionPackage(extensionSwapPackage);
				}
			}
		});
	}

}
