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
package org.eobjects.datacleaner.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyRunner {

    public static void main(String[] args) throws Exception {
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(8888);
        server.addConnector(connector);

        WebAppContext webApp = new WebAppContext();
        webApp.setContextPath("/");
        webApp.setWar("src/main/webapp");
        server.setHandler(webApp);
        server.start();

        System.out.println("Jetty running now! Type 'exit' to try graceful shutdown.");
        createExitListenerThread(server);

        server.join();

        System.out.println("Now we expect the program to exit, or else there is a thread leak.");

        Set<Entry<Thread, StackTraceElement[]>> threadEntries = Thread.getAllStackTraces().entrySet();
        for (Entry<Thread, StackTraceElement[]> entry : threadEntries) {
            Thread thread = entry.getKey();
            if (!thread.isDaemon()) {
                if (Thread.currentThread() != thread) {
                    System.out.println("Still alive: " + thread);
                }
            }
        }
    }

    private static void createExitListenerThread(final Server server) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                try {
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        if ("exit".equals(line)) {
                            System.out.println("Signal 'stop' -> server!");
                            try {
                                server.stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            System.out.println("Signal 'destroy' -> server!");
                            server.destroy();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Graceful shutdown thread failed.");
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }
}
