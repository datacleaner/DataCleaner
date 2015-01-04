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
package org.datacleaner.util.convert;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Convertable;
import org.datacleaner.beans.api.Converter;

/**
 * A custom {@link Converter} for encrypting sensitive strings, such as
 * passwords.
 * 
 * Apply it to your sensitive {@link Configured} properties using the
 * {@link Convertable} annotation.
 */
public class EncodedStringConverter implements Converter<String> {

    private static final String ALGORHITM = "PBEWithMD5AndDES";

    private static final byte[] DEFAULT_SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x12, (byte) 0x10, (byte) 0x33,
            (byte) 0x10, (byte) 0x12, (byte) 0xde };
    private static final char[] DEFAULT_SECRET = "cafelattebabemlobhat".toCharArray();

    private final byte[] _salt;
    private final char[] _secret;

    public EncodedStringConverter() {
        this(DEFAULT_SALT, DEFAULT_SECRET);
    }

    public EncodedStringConverter(byte[] salt, char[] secret) {
        _salt = salt;
        _secret = secret;
    }

    @Override
    public String fromString(Class<?> type, String encodedPassword) {
        if (encodedPassword == null) {
            return null;
        }
        try {
            SecretKeyFactory instance = SecretKeyFactory.getInstance(ALGORHITM);
            SecretKey key = instance.generateSecret(new PBEKeySpec(_secret));
            Cipher cipher = Cipher.getInstance(ALGORHITM);
            cipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(_salt, 20));

            byte[] bytes = encodedPassword.getBytes("UTF-8");

            bytes = cipher.doFinal(Base64.decodeBase64(bytes));
            return new String(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to decode password", e);
        }
    }

    @Override
    public String toString(String password) {
        if (password == null) {
            return null;
        }
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORHITM);
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(_secret));
            Cipher pbeCipher = Cipher.getInstance(ALGORHITM);
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(_salt, 20));

            byte[] bytes = pbeCipher.doFinal(password.getBytes());

            bytes = Base64.encodeBase64(bytes, false);
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to encode password", e);
        }
    }

    @Override
    public boolean isConvertable(Class<?> type) {
        return String.class.equals(type);
    }

}
