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
package org.datacleaner.repository.vfs;

import org.apache.commons.vfs2.FileObject;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.util.VFSUtils;

/**
 * Repository implementation based of commons VFS.
 * 
 * TODO: For now this is a really simple implementation that will only work on
 * local file based {@link FileObject}s. A proper implementation would be much
 * better but this is sufficient for current initial needs.
 */
public class VfsRepository extends FileRepository {

    private static final long serialVersionUID = 1L;

    public VfsRepository(FileObject rootFolder) {
        super(VFSUtils.toFile(rootFolder));
    }
}
