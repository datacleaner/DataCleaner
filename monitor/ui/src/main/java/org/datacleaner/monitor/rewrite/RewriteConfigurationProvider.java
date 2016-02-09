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
package org.datacleaner.monitor.rewrite;

import javax.servlet.ServletContext;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Redirect;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

/**
 * Defines URL rewriting rules for the DataCleaner monitor
 */
@RewriteConfiguration
public class RewriteConfigurationProvider extends HttpConfigurationProvider {

    @Override
    public Configuration getConfiguration(ServletContext servletContext) {
        ConfigurationBuilder builder = ConfigurationBuilder.begin();

        builder.addRule().when(Path.matches("/").or(Path.matches(""))).perform(Redirect.temporary(servletContext
                .getContextPath() + "/login"));

        builder = addJoinRule(builder, "/login", "/login.jsf");
        builder = addJoinRule(builder, "/dashboard", "/dashboard.jsf");
        builder = addJoinRule(builder, "/scheduling", "/scheduling.jsf");
        builder = addJoinRule(builder, "/repository", "/repository.jsf");
        builder = addJoinRule(builder, "/datastores", "/datastores.jsf");
        builder = addJoinRule(builder, "/query", "/query.jsf");

        return builder;
    }

    private ConfigurationBuilder addJoinRule(ConfigurationBuilder builder, String fromPath, String toPath) {
        return builder.addRule(Join.path(fromPath).to(toPath));
    }

    @Override
    public int priority() {
        return 0;
    }

}
