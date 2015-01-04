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
package org.eobjects.analyzer.test;

import java.io.File;

import org.eobjects.analyzer.beans.api.Alias;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@FilterBean("Mock filter")
public class MockFilter implements Filter<MockFilter.Category> {

    public static enum Category {
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
    public MockFilter.Category categorize(InputRow inputRow) {
        return someEnum;
    }

    public MockFilter.Category getSomeEnum() {
        return someEnum;
    }

    public File getSomeFile() {
        return someFile;
    }

    public void setInput(InputColumn<?> input) {
        this.input = input;
    }

    public void setSomeEnum(Category someEnum) {
        this.someEnum = someEnum;
    }

    public void setSomeFile(File someFile) {
        this.someFile = someFile;
    }

    public InputColumn<?> getInput() {
        return input;
    }
}
