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
package org.datacleaner.configuration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DataCleanerConfigurationUpdaterTest {
    private String originalPassword = "superSecretPassword";
    private String newPassword = "newPassword";
    private String tagName = "password";
    private String nodePathString = "descriptor-providers:remote-components:server:" + tagName;
    private String configurationFileName = "conf.xml";
    private String configurationFileClasspath = "/" + configurationFileName;

    @Test
    public void testUpdate() throws Exception {
        Assert.assertTrue(isValuePresent(originalPassword));
        Assert.assertFalse(isValuePresent(newPassword));

        changePassword(newPassword);

        Assert.assertFalse(isValuePresent(originalPassword));
        Assert.assertTrue(isValuePresent(newPassword));

        changePassword(originalPassword);

        Assert.assertTrue(isValuePresent(originalPassword));
        Assert.assertFalse(isValuePresent(newPassword));
    }

    private boolean isValuePresent(String value) {
        try {
            String path = getClass().getResource(configurationFileClasspath).getPath();
            List<String> allLines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);

            for (String line : allLines) {
                if (line.contains(value)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        return false;
    }

    private void changePassword(String newValue) {
        try {
            URL url = getClass().getResource(configurationFileClasspath).toURI().toURL();
            DataCleanerConfigurationUpdater configurationUpdater = new DataCleanerConfigurationUpdater(url);
            String[] nodePath = nodePathString.split(":");
            configurationUpdater.update(nodePath, newValue);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}