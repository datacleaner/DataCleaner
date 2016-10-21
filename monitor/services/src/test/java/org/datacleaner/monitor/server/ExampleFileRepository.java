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
package org.datacleaner.monitor.server;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.datacleaner.repository.file.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class ExampleFileRepository extends FileRepository {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ExampleFileRepository.class);

    public ExampleFileRepository() {
        super(generateTestFolder());
    }

    private static File generateTestFolder() {
        final File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();
        try {
            FileUtils.copyDirectory(new File("src/test/resources/example_repo"), tempDir);
        } catch (IOException e) {
            logger.error("Could not generate test folder", e);
        }

        return tempDir.getAbsoluteFile();
    }
}
