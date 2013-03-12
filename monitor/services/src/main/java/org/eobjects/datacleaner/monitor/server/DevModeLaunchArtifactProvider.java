/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * {@link LaunchArtifactProvider} implementation for development mode.
 * 
 * This implementation assumes that a signed version of all DataCleaner jar
 * files are available in the packaging module's target. To produce this, run
 * the maven build with the 'jnlp' profile:
 * 
 * <pre>
 * mvn install -P jnlp
 * </pre>
 */
public class DevModeLaunchArtifactProvider implements LaunchArtifactProvider {

    private final LaunchArtifactProvider _delegate;

    public DevModeLaunchArtifactProvider() {
        // if being run from the parent project's folder
        final File candidate1 = new File("packaging/jnlp/target/jnlp");

        // if being run from any of the parent modules folders
        final File candidate2 = new File("../packaging/jnlp/target/jnlp");

        // if being run from any of the module folders
        final File candidate3 = new File("../../packaging/jnlp/target/jnlp");
        
       // if being run from self-extracting war in target folder
        final File candidate4 = new File("../../../packaging/jnlp/target/jnlp");

        if (candidate1.exists()) {
            _delegate = new FileFolderLaunchArtifactProvider(candidate1);
        } else if (candidate2.exists()) {
            _delegate = new FileFolderLaunchArtifactProvider(candidate2);
        } else if (candidate3.exists()) {
            _delegate = new FileFolderLaunchArtifactProvider(candidate3);
        } else if (candidate4.exists()) {
            _delegate = new FileFolderLaunchArtifactProvider(candidate4);
        } else {
            _delegate = new JnlpUrlLaunchArtifactProvider();
        }
    }

    @Override
    public boolean isAvailable() {
        return _delegate != null;
    }

    @Override
    public List<String> getJarFilenames() {
        if (_delegate == null) {
            return Collections.emptyList();
        }
        return _delegate.getJarFilenames();
    }

    @Override
    public InputStream readJarFile(String filename) {
        return _delegate.readJarFile(filename);
    }

}
