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
package org.datacleaner.test;

import java.io.File;

import javax.inject.Named;

import org.datacleaner.api.Alias;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

@Named("Mock filter")
public class MockFilter implements Filter<MockFilter.Category> {

    public enum Category {
        VALID, INVALID
    }

    @Configured
    @Alias("a file")
    File someFile;

    @Configured
    Category someEnum;

    @Configured
    InputColumn<?> input;

    @Override
    public MockFilter.Category categorize(final InputRow inputRow) {
        return someEnum;
    }

    public MockFilter.Category getSomeEnum() {
        return someEnum;
    }

    public void setSomeEnum(final Category someEnum) {
        this.someEnum = someEnum;
    }

    public File getSomeFile() {
        return someFile;
    }

    public void setSomeFile(final File someFile) {
        this.someFile = someFile;
    }

    public InputColumn<?> getInput() {
        return input;
    }

    public void setInput(final InputColumn<?> input) {
        this.input = input;
    }
}
