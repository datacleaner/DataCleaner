/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

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
