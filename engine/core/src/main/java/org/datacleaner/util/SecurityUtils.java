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
package org.datacleaner.util;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.datacleaner.util.convert.EncodedStringConverter;
import org.datacleaner.util.ws.NaiveHostnameVerifier;
import org.datacleaner.util.ws.NaiveTrustManager;

/**
 * Utility methods for security concerns.
 */
public class SecurityUtils {
    static final String PREFIX = "enc:";

    private SecurityUtils() {
        // prevent instantiation
    }

    /**
     * Creates a {@link SSLConnectionSocketFactory} which is careless about SSL
     * certificate checks. Use with caution!
     * 
     * @return
     */
    public static SSLConnectionSocketFactory createUnsafeSSLConnectionSocketFactory() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(),
                    new NaiveHostnameVerifier());
            return sslsf;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Removes the certificate checks of HTTPS traffic on a HTTP client. Use
     * with caution!
     * 
     * @param httpClient
     * @throws IllegalStateException
     * 
     * @{@link Deprecated} use {@link #createUnsafeSSLConnectionSocketFactory()}
     *         in conjunction with {@link HttpClients#custom()} instead.
     */
    @Deprecated
    public static void removeSshCertificateChecks(HttpClient httpClient) throws IllegalStateException {
        try {
            // prepare a SSL context which doesn't validate certificates
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            final TrustManager trustManager = new NaiveTrustManager();
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());

            final org.apache.http.conn.ssl.SSLSocketFactory schemeSocketFactory = new org.apache.http.conn.ssl.SSLSocketFactory(
                    sslContext);
            final org.apache.http.conn.scheme.Scheme sslScheme = new org.apache.http.conn.scheme.Scheme("https", 443,
                    schemeSocketFactory);

            // try again with a new registry
            final org.apache.http.conn.scheme.SchemeRegistry registry = httpClient.getConnectionManager()
                    .getSchemeRegistry();
            registry.register(sslScheme);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Encodes/obfuscates a password. Although this does not prevent actual
     * hacking of password, it does remove the obvious threats of having
     * passwords stored as clear text.
     * 
     * @param password
     * @return a String containing the encoded password
     */
    public static String encodePassword(char[] password) {
        if (password == null) {
            return null;
        }
        final EncodedStringConverter converter = new EncodedStringConverter();
        final String encodedPassword = converter.toString(new String(password));
        return encodedPassword;
    }

    /**
     * Encodes/obfuscates a password. Although this does not prevent actual
     * hacking of password, it does remove the obvious threats of having
     * passwords stored as clear text.
     * 
     * @param password
     * @return a String containing the encoded password
     */
    public static String encodePassword(String password) {
        if (password == null) {
            return null;
        }
        return encodePassword(password.toCharArray());
    }

    /**
     * Decodes/deobfuscates an encoded password. Although this does not prevent
     * actual hacking of password, it does remove the obvious threats of having
     * passwords stored as clear text.
     * 
     * @param encodedPassword
     * @return a char array containing the password. Do not use this as a
     *         long-lived object. If the password needs to be held in memory for
     *         longer periods, the encoded version is recommended.
     */
    public static String decodePassword(String encodedPassword) {
        if (encodedPassword == null) {
            return null;
        }
        final EncodedStringConverter converter = new EncodedStringConverter();
        final String password = converter.fromString(String.class, encodedPassword);
        return password;
    }

    /**
     * It encodes/obfuscates a password and adds a human readable prefix that clarifies it.
     *
     * @param passwordInPlainText
     * @return a String containing the encoded password
     */
    public static String encodePasswordWithPrefix(String passwordInPlainText) {
        if (hasPrefix(passwordInPlainText)) {
            return passwordInPlainText;
        }

        return PREFIX + encodePassword(passwordInPlainText);
    }

    /**
     * It decodes/deobfuscates an encoded password with a human readable prefix.
     *
     * @param encodedPasswordWithPrefix
     * @return a String containing the decoded password
     */
    public static String decodePasswordWithPrefix(String encodedPasswordWithPrefix) {
        if (encodedPasswordWithPrefix == null) {
            return null;
        }
        if (hasPrefix(encodedPasswordWithPrefix)) {
            return decodePassword(encodedPasswordWithPrefix.substring(PREFIX.length()));
        } else {
            return encodedPasswordWithPrefix;
        }
    }

    public static boolean hasPrefix(String password) {
        if (password == null) {
            return false;
        }
        return password.startsWith(PREFIX);
    }
}
