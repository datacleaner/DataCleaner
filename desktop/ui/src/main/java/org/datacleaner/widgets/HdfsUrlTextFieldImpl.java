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
package org.datacleaner.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.metamodel.util.HdfsResource;

public class HdfsUrlTextFieldImpl extends AbstractFilenameTextField<HdfsResource> {
    String _hdfsUri;

    public HdfsUrlTextFieldImpl(String uri, final HdfsUrlChooser.OpenType openType) {
        _hdfsUri = uri;

        getBrowseButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    HdfsUrlChooser chooser = new HdfsUrlChooser(openType);
                }
            }
        );
    }

    HdfsUrlTextFieldImpl(HdfsUrlChooser.OpenType openType) {
        this("", openType);
    }

    @Override
    public HdfsResource getResource() {
        return _hdfsUri == null ? null : new HdfsResource(_hdfsUri);
    }

    @Override
    public void setResource(HdfsResource resource) {
        if (resource == null) {
            return;
        }

        _hdfsUri = resource.getQualifiedPath();
    }

}
