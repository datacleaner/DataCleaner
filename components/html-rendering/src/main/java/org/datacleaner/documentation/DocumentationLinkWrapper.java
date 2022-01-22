/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.documentation;

import org.datacleaner.api.ExternalDocumentation;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;

/**
 * A wrapper around the {@link ExternalDocumentation.DocumentationLink} object to make it easier for Freemarker
 * templates to read its values.
 */
public class DocumentationLinkWrapper {

    private final DocumentationLink _link;

    public DocumentationLinkWrapper(DocumentationLink link) {
        _link = link;
    }

    public String getTitle() {
        return _link.title();
    }

    public String getUrl() {
        return _link.url();
    }

    public String getType() {
        if (_link.type() == null) {
            return null;
        }
        return _link.type().toString();
    }

    public String getVersion() {
        return _link.version();
    }

    
}
