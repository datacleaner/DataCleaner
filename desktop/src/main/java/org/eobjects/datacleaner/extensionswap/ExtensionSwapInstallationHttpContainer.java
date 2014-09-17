/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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

import java.awt.Component;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.util.InvalidHttpResponseException;
import org.apache.metamodel.util.FileHelper;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP container used for the ExtensionSwap to signal that the user wants to
 * install an extension.
 */
public class ExtensionSwapInstallationHttpContainer implements Container {

    private static final int PORT_NUMBER = 31389;

    private static final Logger logger = LoggerFactory.getLogger(ExtensionSwapInstallationHttpContainer.class);

    private final ExtensionSwapClient _client;

    public ExtensionSwapInstallationHttpContainer(ExtensionSwapClient extensionSwapClient, UsageLogger usageLogger) {
        _client = extensionSwapClient;
    }

    @Override
    public void handle(Request req, Response resp) {
        PrintStream out = null;
        String callback = null;
        try {
            out = resp.getPrintStream();
            callback = req.getParameter("callback");

            final String extensionId = req.getParameter("extensionId");
            if (extensionId == null) {
                throw new IllegalArgumentException("extensionId cannot be null");
            }

            final String username = req.getParameter("username");

            logger.info("Initiating transfer of extension: {}", extensionId);

            final ExtensionSwapPackage extensionSwapPackage = _client.getExtensionSwapPackage(extensionId);
            logger.info("Fetched ExtensionSwap package: {}", extensionSwapPackage);

            if (_client.isInstalled(extensionSwapPackage)) {
                // reject the extension because it is already installed.
                if (callback != null && out != null) {
                    out.print(callback
                            + "({\"success\":false,\"errorMessage\":\"This extension is already installed\"})");
                }
                resp.setCode(500);
            } else {
                // install the extension
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        displayInstallationOptions(extensionSwapPackage, username);
                    }
                });

                if (callback != null) {
                    out.print(callback + "({\"success\":true})");
                }
                resp.setCode(200);
            }

        } catch (InvalidHttpResponseException e) {
            if (callback != null && out != null) {
                out.print(callback + "({\"success\":false,\"errorMessage\":\"Could not retrieve extension details\"})");
            }
            resp.setCode(500);
        } catch (IOException e) {
            logger.error("IOException occurred while processing HTTP request", e);
            resp.setCode(400);
        } finally {
            FileHelper.safeClose(out);
        }
    }

    private void displayInstallationOptions(final ExtensionSwapPackage extensionSwapPackage, final String username) {

        final String title = "Install DataCleaner extension?";
        final String message = "Do you want to download and install the extension '" + extensionSwapPackage.getName()
                + "'";

        final Component window = (Component) DCWindowContext.getAnyWindow();
        final int confirmation = JOptionPane.showConfirmDialog(window, message, title, JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            _client.registerExtensionPackage(extensionSwapPackage, username);
        }
    }

    /**
     * Initializes the extension swap installation HTTP service
     * 
     * @param client
     *            the client to use
     * @return a closable that can be invoked in case the service is to be shut
     *         down
     */
    public Closeable initialize() {
        try {
            Connection connection = new SocketConnection(this);
            SocketAddress address = new InetSocketAddress(PORT_NUMBER);
            connection.connect(address);
            logger.info("HTTP service for ExtensionSwap installation running on port {}", PORT_NUMBER);
            return connection;
        } catch (IOException e) {
            logger.warn("Could not host HTTP service for ExtensionSwap installation on port " + PORT_NUMBER
                    + ". Automatic installations of extensions will not be available.", e);
            return null;
        }
    }

}
