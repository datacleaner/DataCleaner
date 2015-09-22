package org.datacleaner.monitor.configuration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Class RemoteComponentsConfigurationHandler
 *
 * @author k.houzvicka
 * @since 18.9.15
 */
public class RemoteComponentsConfHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("published-components", new RemoteComponentsConfBeanDefinitionParser());
    }
}
