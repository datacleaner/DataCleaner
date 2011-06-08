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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DummyClient {

	public static void main(String[] args) throws Throwable {
		Registry registry = LocateRegistry.getRegistry(ExtensionSwapInstallationService.DEFAULT_PORT);
		
		ExtensionSwapInstallationService service = (ExtensionSwapInstallationService) registry.lookup(ExtensionSwapInstallationService.class.getSimpleName());
		
		service.initiateTransfer("HIquality-DataCleaner");
	}
}
