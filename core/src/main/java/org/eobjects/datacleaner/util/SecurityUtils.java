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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;

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
        if (password == null) {
            return null;
        }
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SECRET));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));

            byte[] bytes = pbeCipher.doFinal(new String(password).getBytes());

            bytes = Base64.encodeBase64(bytes, false);
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to encode password", e);
        }
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
        try {
            SecretKeyFactory instance = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = instance.generateSecret(new PBEKeySpec(SECRET));
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));

            byte[] bytes = encodedPassword.getBytes("UTF-8");

            bytes = cipher.doFinal(Base64.decodeBase64(bytes));
            return new String(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decode password", e);
        }
    }

}
