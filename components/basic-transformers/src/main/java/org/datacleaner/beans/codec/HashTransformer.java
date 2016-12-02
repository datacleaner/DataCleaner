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
package org.datacleaner.beans.codec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringJoiner;
import javax.xml.bind.DatatypeConverter;
import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.EncodingCategory;

@Named("Hash value")
@Description("It creates a hash from specified input. ")
@Categorized(EncodingCategory.class)
public class HashTransformer implements Transformer {
    @Configured
    InputColumn<?>[] _columns;

    @Configured
    Algorithm _algorithm;

    enum Algorithm {
        MD2,
        MD5,
        SHA,
        SHA_224,
        SHA_256,
        SHA_384,
        SHA_512;

        public String toString() {
            return name().replace('_', '-');
        }
    }

    public HashTransformer() {
        _algorithm = Algorithm.SHA_512;
    }

    HashTransformer(final InputColumn<?>[] columns, final Algorithm algorithm) {
        _columns = columns;
        _algorithm = algorithm;
    }

    @Override
    public OutputColumns getOutputColumns() {
        final StringJoiner joiner = new StringJoiner(", ");

        for (InputColumn<?> column : _columns) {
            joiner.add(column.getName());
        }

        return new OutputColumns(String.class, "Hash of " + joiner.toString(), "Input length");
    }

    @Override
    public String[] transform(final InputRow inputRow) {
        final StringBuilder builder = new StringBuilder();
        int length = 0;

        for (final InputColumn<?> column : _columns) {
            final Object value = inputRow.getValue(column);

            if (value != null && !value.toString().isEmpty()) {
                final String stringValue = value.toString();
                length += stringValue.length();
                builder.append(stringValue);
            }
        }

        return new String[] { hash(builder.toString().getBytes()), String.valueOf(length) };
    }

    private String hash(final byte[] input) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(_algorithm.toString());
            messageDigest.update(input);
            final byte[] hash = messageDigest.digest();

            return DatatypeConverter.printHexBinary(hash);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm used for hashing was not recognized. " + e.getMessage());
        }
    }
}
