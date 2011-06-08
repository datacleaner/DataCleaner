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

/**
 * Defines the remote interface, allowing for RMI clients to request the
 * installation of an extension on the ExtensionSwap.
 * 
 * @author Kasper Sørensen
 * 
 */
public interface ExtensionSwapInstallationService extends Remote {
	
	public static final int DEFAULT_PORT = 1099;

	public void initiateTransfer(String extensionId) throws RemoteException;
}
