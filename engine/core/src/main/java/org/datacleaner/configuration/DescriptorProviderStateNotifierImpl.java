/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 * <p/>
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.configuration;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.DescriptorProviderState;
import org.datacleaner.job.builder.ComponentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DescriptorProviderStateNotifierImpl implements DescriptorProviderStateNotifier {
    private static final long SERVER_CHECK_INTERVAL = 2 * 60 * 1000; //[ms] - 2min
    private static final Logger logger = LoggerFactory.getLogger(DescriptorProviderStateNotifierImpl.class);
    private final DescriptorProvider _descriptorProvider;
    private Set<DescriptorProviderStateListener> _listenerSet = Collections.synchronizedSet(new HashSet<DescriptorProviderStateListener>());

    private Map<DescriptorProvider, DescriptorProviderState> _stateMap = new HashMap<>();
    private ServerChecker _serverChecker;

    public DescriptorProviderStateNotifierImpl(DescriptorProvider descriptorProvider) {
        this._descriptorProvider = descriptorProvider;
    }

    @Override
    public void addListener(DescriptorProviderStateListener listener) {
        synchronized (_listenerSet) {
            if (_listenerSet.isEmpty()) {
                startThread();
            }
            _listenerSet.add(listener);
        }
    }

    @Override
    public void removeListener(DescriptorProviderStateListener listener) {
        if (!_listenerSet.contains(listener)) {
            return;
        }
        synchronized (_listenerSet) {
            _listenerSet.remove(listener);
            if (_listenerSet.isEmpty()) {
                if (_serverChecker != null) {
                    synchronized (_serverChecker) {
                        _serverChecker.stop();
                        _serverChecker.notifyAll();
                    }
                    _serverChecker = null;
                }
            }
        }
    }

    private void notifyAllListeners() {
        for (DescriptorProviderStateListener descriptorProviderStateListener : _listenerSet) {
            descriptorProviderStateListener.notify(_stateMap);
        }
    }

    private synchronized void startThread() {
        if (_serverChecker == null) {
            _serverChecker = new ServerChecker();
            Thread serverCheckingThread = new Thread(_serverChecker);
            serverCheckingThread.start();
        }
    }

    private class ServerChecker implements Runnable {

        private boolean running = true;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                synchronized (this) {
                    if (running) {
                        try {
                            wait(SERVER_CHECK_INTERVAL);
                        } catch (InterruptedException e) {
                            running = false;
                            logger.error("Waiting on checking thread was interrupted : " + e.getMessage());
                        }
                    }
                }

                Map<DescriptorProvider, DescriptorProviderState> providerStatesMap = _descriptorProvider.getProviderStatesMap();
                boolean equals = equals(providerStatesMap, _stateMap);
                if (!equals) {
                    _stateMap = providerStatesMap;
                    notifyAllListeners();
                }
            }
        }

        private boolean equals(Map<DescriptorProvider, DescriptorProviderState> map1, Map<DescriptorProvider, DescriptorProviderState> map2) {
            if (map1 == null || map2 == null) {
                if (map1 == null && map2 == null) {
                    return true;
                }
                return false;
            }

            if (map1.size() != map2.size()) {
                return false;
            }

            for (Map.Entry<DescriptorProvider, DescriptorProviderState> map1Entry : map1.entrySet()) {
                if (!map2.get(map1Entry.getKey()).equals(map1Entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
    }
}
