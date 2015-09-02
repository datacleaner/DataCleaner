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

import java.util.Arrays;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.datacleaner.components.remote.RemoteTransformer;

/**
 @Since 9/1/15
 */
public class RemoteTransformerDescriptorImpl extends SimpleComponentDescriptor implements TransformerDescriptor {
    private String displayName;

    public RemoteTransformerDescriptorImpl(String displayName, RemoteConfiguredPropertyDescriptorImpl[] configuredProperties) {
        super(RemoteTransformer.class);
        this._configuredProperties.addAll(Arrays.asList(configuredProperties));
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    protected Class<? extends ComponentSuperCategory> getDefaultComponentSuperCategoryClass() {
        return TransformSuperCategory.class;
    }

}
