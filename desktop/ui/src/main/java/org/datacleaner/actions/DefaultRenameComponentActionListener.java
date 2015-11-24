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
package org.datacleaner.actions;

import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.widgets.visualization.JobGraphContext;

/**
 * Default implementation of {@link RenameComponentActionListener} which simply
 * refreshes the graph when a name has changed.
 */
public final class DefaultRenameComponentActionListener extends RenameComponentActionListener {

    private final JobGraphContext _graphContext;

    public DefaultRenameComponentActionListener(ComponentBuilder componentBuilder, JobGraphContext graphContext) {
        super(componentBuilder);
        _graphContext = graphContext;
    }

    @Override
    protected void onNameChanged() {
        _graphContext.getJobGraph().refresh();
    }

}
