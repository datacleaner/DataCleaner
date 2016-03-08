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
package org.datacleaner.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.apache.metamodel.util.FileHelper;
import org.junit.rules.TemporaryFolder;

public class MockHadoopConfigHelper {

    // Prepare "environment"
    private final File confFolder;
    private final File resourcesFolder = new File("src/test/resources");

    private final String path = resourcesFolder.toURI().toString();

    public MockHadoopConfigHelper(TemporaryFolder temporaryFolder) throws IOException {
         confFolder = temporaryFolder.newFolder();
    }

    public void generateCoreFile() throws IOException {
        final File coreSiteFile = new File(confFolder, "conf-site.xml");

        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream("core-site-template.xml")) {
            final BufferedReader reader = FileHelper.getBufferedReader(inputStream, FileHelper.UTF_8_ENCODING);
            try (final Writer writer = FileHelper.getWriter(coreSiteFile)) {
                String line = reader.readLine();
                while (line != null) {
                    line = line.replace("${PATH}", path);
                    writer.write(line);
                    line = reader.readLine();
                }
                writer.flush();
            }
        }
    }

    public File getConfFolder() {
        return confFolder;
    }

    public String getPath() {
        return path;
    }



}
