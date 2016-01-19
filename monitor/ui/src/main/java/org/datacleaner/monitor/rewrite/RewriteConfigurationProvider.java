package org.datacleaner.monitor.rewrite;

import javax.servlet.ServletContext;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

/**
 * Defines URL rewriting rules for the website
 */
@RewriteConfiguration
public class RewriteConfigurationProvider extends HttpConfigurationProvider {

    @Override
    public Configuration getConfiguration(ServletContext arg0) {
        ConfigurationBuilder builder = ConfigurationBuilder.begin();

        builder = addJoinRule(builder, "/", "/index.html");
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
