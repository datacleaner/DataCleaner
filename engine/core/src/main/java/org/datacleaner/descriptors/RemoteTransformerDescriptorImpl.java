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

import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.datacleaner.components.remote.RemoteTransformer;

/**
 @Since 9/1/15
 */
public class RemoteTransformerDescriptorImpl extends SimpleComponentDescriptor implements TransformerDescriptor {

    private String remoteDisplayName;
    private String baseUrl;
    private String componentUrl;
    private String tenant;
    private String username;
    private String password;

    public RemoteTransformerDescriptorImpl(String baseUrl, String componentUrl, String displayName, String tenant, String username, String password) {
        super(RemoteTransformer.class);
        this.componentUrl = componentUrl;
        this.remoteDisplayName = displayName;
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
        this.tenant = tenant;
        try {
            this._initializeMethods.add(new InitializeMethodDescriptorImpl(RemoteTransformer.class.getMethod("init"), this));
            this._closeMethods.add(new CloseMethodDescriptorImpl(RemoteTransformer.class.getMethod("close"), this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addPropertyDescriptor(ConfiguredPropertyDescriptor propertyDescriptor) {
        this._configuredProperties.add(propertyDescriptor);
    }

    @Override
    public String getDisplayName() {
        return remoteDisplayName + " (remote)";
    }

    @Override
    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return TransformSuperCategory.class;
    }

    @Override
    public Object newInstance() {
        RemoteTransformer t = new RemoteTransformer(baseUrl, componentUrl, remoteDisplayName, tenant, username, password);
        return t;
    }

}
