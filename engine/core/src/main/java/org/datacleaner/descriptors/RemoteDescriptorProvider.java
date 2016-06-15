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
package org.datacleaner.descriptors;

import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.configuration.RemoteServerState;

/**
 * Descriptor provider for remote components.
 */
public interface RemoteDescriptorProvider extends DescriptorProvider {

    String DATACLEANER_BASE_URL = "https://datacleaner.org";
    String DATACLOUD_URL = "https://services.datacleaner.org";
    String DATACLOUD_TERMS_PURE_URL = DATACLEANER_BASE_URL + "/datacloud_terms_pure";
    String DATACLOUD_TERMS_URL = DATACLEANER_BASE_URL + "/datacloud_terms";
    String DATACLOUD_TERMS_ACCEPT_URL = DATACLEANER_BASE_URL + "/ws/datacloud_accept_terms";
    String DATACLOUD_NEWS_CHANNEL_URL = DATACLEANER_BASE_URL + "/ws/lastnews";
    String DATACLOUD_SERVER_NAME = "DataCloud";

    public RemoteServerData getServerData();

    public RemoteServerState getServerState();
}
