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
package org.eobjects.datacleaner.util;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.eobjects.analyzer.util.convert.EncodedStringConverter;

/**
 * Utility methods for security concerns.
 */
public class SecurityUtils {

    protected static final char[] SECRET = "cafelattebabemlobhat".toCharArray();
    protected static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x12, (byte) 0x10, (byte) 0x33,
            (byte) 0x10, (byte) 0x12, (byte) 0xde };

    private SecurityUtils() {
        // prevent instantiation
    }

    /**
     * Removes the certificate checks of HTTPS traffic on a HTTP client. Use
     * with caution!
     * 
     * @param httpClient
     * @throws IllegalStateException
     */
    public static void removeSshCertificateChecks(HttpClient httpClient) throws IllegalStateException {
        try {
            // prepare a SSL context which doesn't validate certificates
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            final TrustManager trustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }
            };
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
            final SSLSocketFactory schemeSocketFactory = new SSLSocketFactory(sslContext);
            final Scheme sslScheme = new Scheme("https", 443, schemeSocketFactory);

            // try again with a new registry
            final SchemeRegistry registry = httpClient.getConnectionManager().getSchemeRegistry();
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
        EncodedStringConverter converter = new EncodedStringConverter();
        String encodedPassword = converter.toString(new String(password));
        return encodedPassword;
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
        EncodedStringConverter converter = new EncodedStringConverter();
        String password = converter.fromString(String.class, encodedPassword);
        return password;
    }

}
