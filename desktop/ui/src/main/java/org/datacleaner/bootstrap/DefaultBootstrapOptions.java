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
package org.datacleaner.bootstrap;

import java.awt.Image;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.metamodel.DataContext;
import org.datacleaner.cli.CliArguments;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.ResourceManager;
import org.datacleaner.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default bootstrap options for DataCleaner.
 */
public class DefaultBootstrapOptions implements BootstrapOptions {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBootstrapOptions.class);

    private final String[] _args;
    private final CliArguments _arguments;
    private final boolean _commandLineMode;
    private final String _embeddedClientName;

    public DefaultBootstrapOptions(final String[] args) {
        _args = args;
        _arguments = CliArguments.parse(_args);

        // command line mode determination
        boolean commandLineMode = _arguments.isSet();
        if (commandLineMode) {
            if ("true".equalsIgnoreCase(System.getProperty(SystemProperties.UI_VISIBLE, "false"))) {
                commandLineMode = false;
            }
        }
        _commandLineMode = commandLineMode;

        _embeddedClientName = System.getProperty(SystemProperties.EMBED_CLIENT);
    }

    @Override
    public boolean isCommandLineMode() {
        return _commandLineMode;
    }

    @Override
    public CliArguments getCommandLineArguments() {
        return _arguments;
    }

    @Override
    public ExitActionListener getExitActionListener() {
        return new DCExitActionListener();
    }

    @Override
    public boolean isSingleDatastoreMode() {
        return _arguments.getDatastoreName() != null;
    }

    @Override
    public Datastore getSingleDatastore(final DatastoreCatalog datastoreCatalog) {
        final String ds = _arguments.getDatastoreName();
        return datastoreCatalog.getDatastore(ds);
    }

    @Override
    public void initializeSingleDatastoreJob(final AnalysisJobBuilder analysisJobBuilder, final DataContext dataContext,
            final InjectorBuilder injectorBuilder) {
        // do nothing
    }

    @Override
    public Image getWelcomeImage() {
        if (_arguments.getJobFile() != null) {
            if ("Kettle".equals(_embeddedClientName)) {
                try {
                    final URL url = ResourceManager.get().getUrl("images/pdi_dc_banner.png");
                    return ImageIO.read(url);
                } catch (final Exception e) {
                    logger.warn("Could not load PDI DC banner", e);
                }
            }
        }
        return null;
    }
}
