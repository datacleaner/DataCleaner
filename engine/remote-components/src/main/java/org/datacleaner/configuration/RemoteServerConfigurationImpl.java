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
package org.datacleaner.configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.job.concurrent.ScheduledTaskRunner;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.restclient.ComponentRESTClient;
import org.datacleaner.restclient.DataCloudUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link RemoteServerConfiguration}.
 */
public class RemoteServerConfigurationImpl implements RemoteServerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RemoteServerConfigurationImpl.class);
    private static final int TEST_CONNECTION_TIMEOUT = 15 * 1000; // [ms]
    private static final long DELAY_MIN = 5;
    private final Map<String, RemoteServerState> actualStateMap = Collections.synchronizedMap(new HashMap<>());
    private ServerStatusTask serverStatusTask;
    private ScheduledTaskRunner scheduledTaskRunner;
    private List<RemoteServerStateListener> listeners = Collections.synchronizedList(new ArrayList());
    protected final List<RemoteServerData> remoteServerDataList;

    public RemoteServerConfigurationImpl(List<RemoteServerData> serverData, TaskRunner taskRunner) {
        remoteServerDataList = new ArrayList<>(serverData);
        for (RemoteServerData remoteServerData : serverData) {
            actualStateMap.put(remoteServerData.getServerName(),
                    new RemoteServerState(RemoteServerState.State.UNKNOWN, remoteServerData.getUsername(), null));
        }

        if (taskRunner == null || !(taskRunner instanceof ScheduledTaskRunner)) {
            logger.error("Task runner isn't ScheduledTaskRunner. Remote server status task won't be scheduled.");
        } else {
            scheduledTaskRunner = (ScheduledTaskRunner) taskRunner;
        }
    }

    @Override
    public List<RemoteServerData> getServerList() {
        return Collections.unmodifiableList(remoteServerDataList);
    }

    @Override
    public RemoteServerData getServerConfig(String serverName) {
        if (serverName == null) {
            return null;
        }

        for (RemoteServerData remoteServerData : remoteServerDataList) {
            String configServerName = remoteServerData.getServerName();
            if (configServerName == null) {
                continue;
            }
            if (configServerName.toLowerCase().equals(serverName.toLowerCase())) {
                return remoteServerData;
            }
        }
        return null;
    }

    @Override
    public RemoteServerState getActualState(String remoteServerName) {
        if (serverStatusTask == null) {
            scheduledTask();
        }
        return actualStateMap.get(remoteServerName);
    }

    @Override
    public void addListener(RemoteServerStateListener listener) {
        if (serverStatusTask == null) {
            scheduledTask();
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(RemoteServerStateListener listener) {
        listeners.remove(listener);
    }

    private synchronized void scheduledTask() {
        if (scheduledTaskRunner != null && serverStatusTask == null) {
            serverStatusTask = new ServerStatusTask();
            ServerStatusListener serverStatusListener = new ServerStatusListener();
            scheduledTaskRunner.runScheduled(serverStatusTask, serverStatusListener, 0, DELAY_MIN, TimeUnit.MINUTES);
        }
    }

    private RemoteServerState checkServerAvailability(RemoteServerData remoteServerData) {
        if (remoteServerData.getServerName().equals(RemoteDescriptorProvider.DATACLOUD_SERVER_NAME)) {
            return checkDataCloudServerAvailability(remoteServerData);
        } else {
            return checkOtherServerAvailability(remoteServerData);
        }
    }

    private RemoteServerState checkDataCloudServerAvailability(RemoteServerData remoteServerData) {
        DataCloudUser dataCloudUserInfo = null;
        try {
            ComponentRESTClient restClient =
                    new ComponentRESTClient(remoteServerData.getUrl(), remoteServerData.getUsername(),
                            remoteServerData.getPassword());
            dataCloudUserInfo = restClient.getDataCloudUserInfo();
        } catch (Exception e) {
            logger.warn("DataCloud server connection problem: " + e.getMessage());
            return new RemoteServerState(RemoteServerState.State.ERROR, remoteServerData.getUsername(), e.getMessage());
        }
        RemoteServerState.State state;
        if (dataCloudUserInfo.getCredit() != null && dataCloudUserInfo.getCredit() > 0) {
            state = RemoteServerState.State.OK;
        } else {
            state = RemoteServerState.State.NO_CREDIT;
        }
        return new RemoteServerState(state, dataCloudUserInfo.getEmail(), dataCloudUserInfo.getRealName(),
                dataCloudUserInfo.getCredit(), dataCloudUserInfo.isEmailConfirmed());
    }

    private RemoteServerState checkOtherServerAvailability(RemoteServerData remoteServerData) {
        try (Socket socket = new Socket()) {
            URL siteURL = new URL(remoteServerData.getUrl());
            int port = siteURL.getPort();
            if (port <= 0) {
                port = siteURL.getDefaultPort();
            }
            InetSocketAddress endpoint = new InetSocketAddress(siteURL.getHost(), port);
            socket.connect(endpoint, TEST_CONNECTION_TIMEOUT);
            final boolean connectionCheckResult = socket.isConnected();
            if (connectionCheckResult) {
                return new RemoteServerState(RemoteServerState.State.OK, remoteServerData.getUsername(), null);
            } else {
                return new RemoteServerState(RemoteServerState.State.ERROR, remoteServerData.getUsername(), null);
            }
        } catch (IOException e) {
            logger.warn(
                    "Server '" + remoteServerData.getServerName() + "(" + remoteServerData.getUrl() + ")' is down: "
                            + e.getMessage());
            return new RemoteServerState(RemoteServerState.State.ERROR, remoteServerData.getUsername(), e.getMessage());
        }
    }

    private class ServerStatusTask implements Task {

        private List<String> stateChanged;

        @Override
        public void execute() throws Exception {
            stateChanged = new ArrayList<>();
            for (RemoteServerData remoteServerData : remoteServerDataList) {
                String serverName = remoteServerData.getServerName();
                RemoteServerState state = checkServerAvailability(remoteServerData);
                RemoteServerState oldState = actualStateMap.get(serverName);
                if (!state.equals(oldState)) { //old state can be null - new remote server.
                    actualStateMap.put(serverName, state);
                    stateChanged.add(serverName);
                }
            }
        }

        public List<String> getStateChanged() {
            return stateChanged;
        }
    }

    private class ServerStatusListener implements TaskListener {

        @Override
        public void onBegin(final Task task) {

        }

        @Override
        public void onComplete(final Task task) {
            ServerStatusTask serverStatusTask = (ServerStatusTask) task;
            for (String changeServerName : serverStatusTask.getStateChanged()) {
                for (RemoteServerStateListener listener : listeners) {
                    RemoteServerState remoteServerState = actualStateMap.get(changeServerName);
                    logger.info("Remote server {} has new state {}", changeServerName, remoteServerState);
                    listener.onRemoteServerStateChange(changeServerName, remoteServerState);
                }
            }
        }

        @Override
        public void onError(final Task task, final Throwable throwable) {
            logger.error("Error in Remote server status task.", throwable);
        }
    }
}
