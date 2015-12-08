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

/**
 * A {@link TransformerDescriptor} for remote transformers.
 */
public interface RemoteTransformerDescriptor extends TransformerDescriptor {

    public void addPropertyDescriptor(ConfiguredPropertyDescriptor propertyDescriptor);

    public void setRemoteDescriptorProvider(RemoteDescriptorProvider remoteDescriptorProvider);

    public RemoteDescriptorProvider getRemoteDescriptorProvider();

    public void setServerPriority(Integer serverPriority);

    public Integer getServerPriority();

    public String getDisplayName();

    public void setServerName(String serverName);

    public String getServerName();

    public byte[] getIconData();
}
