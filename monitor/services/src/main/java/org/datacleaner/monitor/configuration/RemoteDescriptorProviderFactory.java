package org.datacleaner.monitor.configuration;

import org.datacleaner.configuration.RemoteServerDataImpl;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.descriptors.RemoteDescriptorProviderImpl;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring factory for RemoteDescriptorProviderImpl.
 */
public class RemoteDescriptorProviderFactory implements FactoryBean<RemoteDescriptorProvider> {

    String url;
    String username;
    String password;

    @Override
    public RemoteDescriptorProvider getObject() throws Exception {
        return new RemoteDescriptorProviderImpl(new RemoteServerDataImpl(url, "RemoteServer", username, password));
    }

    @Override
    public Class<?> getObjectType() {
        return RemoteDescriptorProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
