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
package org.datacleaner.monitor.configuration;

import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.configuration.RemoteServerDataImpl;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring factory for RemoteServerDataImpl.
 */
public class RemoteServerDataFactory implements FactoryBean<RemoteServerData> {

    String url;
    String username;
    String password;

    @Override
    public RemoteServerData getObject() throws Exception {
        return new RemoteServerDataImpl(url, "RemoteServer", username, password);
    }

    @Override
    public Class<?> getObjectType() {
        return RemoteDescriptorProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
