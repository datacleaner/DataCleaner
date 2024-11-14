/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.user;

import java.lang.reflect.Field;
import java.net.InetAddress;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;

import junit.framework.TestCase;

public class UserPreferencesImplTest extends TestCase {


    public void testCreateHttpClientWithoutNtCredentials() throws Exception {
        final UserPreferencesImpl up = new UserPreferencesImpl(null);
        up.setProxyHostname("host");
        up.setProxyPort(1234);
        up.setProxyUsername("bar");
        up.setProxyPassword("baz");
        up.setProxyEnabled(true);
        up.setProxyAuthenticationEnabled(true);

        final CloseableHttpClient httpClient = up.createHttpClient();

        final String computername = InetAddress.getLocalHost().getHostName();
        assertNotNull(computername);
        assertTrue(computername.length() > 1);

        AuthScope authScope;
        Credentials credentials;

        authScope = new AuthScope("host", 1234, AuthScope.ANY_REALM, "ntlm");
        credentials = getCredentialsProvider(httpClient).getCredentials(authScope);
        assertEquals("[principal: bar][workstation: " + computername.toUpperCase() + "]", credentials.toString());

        authScope = new AuthScope("host", 1234);
        credentials = getCredentialsProvider(httpClient).getCredentials(authScope);
        assertEquals("[principal: bar]", credentials.toString());

        authScope = new AuthScope("anotherhost", AuthScope.ANY_PORT);
        credentials = getCredentialsProvider(httpClient).getCredentials(authScope);
        assertNull(credentials);
    }

    public void testCreateHttpClientWithNtCredentials() throws Exception {
        final UserPreferencesImpl up = new UserPreferencesImpl(null);
        up.setProxyHostname("host");
        up.setProxyPort(1234);
        up.setProxyUsername("FOO\\bar");
        up.setProxyPassword("baz");
        up.setProxyEnabled(true);
        up.setProxyAuthenticationEnabled(true);

        final CloseableHttpClient httpClient = up.createHttpClient();

        final String computername = InetAddress.getLocalHost().getHostName();
        assertNotNull(computername);
        assertTrue(computername.length() > 1);

        AuthScope authScope;
        Credentials credentials;

        authScope = new AuthScope("host", 1234, AuthScope.ANY_REALM, "ntlm");
        credentials = getCredentialsProvider(httpClient).getCredentials(authScope);
        assertEquals("[principal: FOO/bar][workstation: " + computername.toUpperCase() + "]",
                credentials.toString().replaceAll("\\\\", "/"));

        authScope = new AuthScope("host", 1234);
        credentials = getCredentialsProvider(httpClient).getCredentials(authScope);
        assertEquals("[principal: FOO\\bar]", credentials.toString());

        authScope = new AuthScope("anotherhost", AuthScope.ANY_PORT);
        credentials = getCredentialsProvider(httpClient).getCredentials(authScope);
        assertNull(credentials);
    }

    private CredentialsProvider getCredentialsProvider(final CloseableHttpClient httpClient) {
        try {
            final Field field = httpClient.getClass().getDeclaredField("credentialsProvider");
            field.setAccessible(true);
            return (CredentialsProvider) field.get(httpClient);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
