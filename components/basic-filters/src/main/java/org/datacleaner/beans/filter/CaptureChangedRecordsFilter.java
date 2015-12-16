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
package org.datacleaner.beans.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import javax.inject.Named;

import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.Resource;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Optimizeable;
import org.datacleaner.api.QueryOptimizedFilter;
import org.datacleaner.components.categories.DateAndTimeCategory;
import org.datacleaner.components.categories.FilterCategory;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Filter for archieving a "change data capture" mechanism based on a
 * "last modified" field. After each execution, the greatest timestamp is
 * recorded and picked up successively by the next run.
 */
@Named("Capture changed records")
@Description("Include only records that have changed since the last time you ran the job. This filter assumes a field containing the timestamp or a number field of the latest change for each record, and stores the greatest encountered value in order to update the filter's future state.")
@Distributed(false)
@Categorized({ FilterCategory.class, DateAndTimeCategory.class })
@Optimizeable(removeableUponOptimization = false)
public class CaptureChangedRecordsFilter implements QueryOptimizedFilter<ValidationCategory> {

    private static final Logger logger = LoggerFactory.getLogger(CaptureChangedRecordsFilter.class);

    @Configured
    @Description("Column containing the last modification timestamp or date or number.")
    InputColumn<Object> lastModifiedColumn;

    @Configured
    @Description("A file used to persist and load the latest state of this data capture component.")
    @FileProperty(extension = "properties", accessMode = FileAccessMode.SAVE)
    Resource captureStateFile;

    @Configured(required = false)
    @Description("A custom identifier for this captured state. If omitted, the name of the 'Last modified column' will be used.")
    String captureStateIdentifier;

    private long _lastModifiedThresholdMillis = -1l;
    private int _lastModifiedThresholdNanos = 0;
    private long _greatestEncounteredMillis = -1l;
    private int _greatestEncounteredNanos = 0;

    @Initialize
    public void initialize() throws IOException {
        final Properties properties = loadProperties();
        final String key = getPropertyKey();
        final String lastModifiedStr = properties.getProperty(key);
        if (!Strings.isNullOrEmpty(lastModifiedStr)) {
            final int indexOfDot = lastModifiedStr.indexOf('.');
            if (indexOfDot == -1) {
                _lastModifiedThresholdMillis = convertToNumber(lastModifiedStr).longValue();
            } else {
                final String str1 = lastModifiedStr.substring(0, indexOfDot);
                final String str2 = lastModifiedStr.substring(indexOfDot + 1);

                _lastModifiedThresholdMillis = convertToNumber(str1).longValue();
                _lastModifiedThresholdNanos = convertToNumber(str2).intValue();
            }
        }
    }

    @Override
    public boolean isOptimizable(final ValidationCategory category) {
        // only the valid category is optimizeable currently
        return category == ValidationCategory.VALID;
    }

    @Override
    public Query optimizeQuery(final Query q, final ValidationCategory category) {
        assert category == ValidationCategory.VALID;

        if (_lastModifiedThresholdMillis != -1l) {
            final Column column = lastModifiedColumn.getPhysicalColumn();
            if (column.getType().isTimeBased()) {
                q.where(column, OperatorType.GREATER_THAN, createQueryOperand());
            } else {
                q.where(column, OperatorType.GREATER_THAN, _lastModifiedThresholdMillis);
            }

        }
        return q;
    }

    private Date createQueryOperand() {
        if (_lastModifiedThresholdNanos == 0) {
            return new Date(_lastModifiedThresholdMillis);
        }
        final Timestamp ts = new Timestamp(_lastModifiedThresholdMillis);
        ts.setNanos(_lastModifiedThresholdNanos);
        return ts;
    }

    @Close(onFailure = false)
    public void close() throws IOException {
        if (_greatestEncounteredMillis != -1) {
            final Properties properties = loadProperties();
            final String key = getPropertyKey();
            final String value;
            if (_greatestEncounteredNanos == 0) {
                value = "" + _greatestEncounteredMillis;
            } else {
                value = "" + _greatestEncounteredMillis + '.' + String.format("%09d", _greatestEncounteredNanos);
            }
            properties.setProperty(key, value);

            captureStateFile.write(new Action<OutputStream>() {
                @Override
                public void run(OutputStream out) throws Exception {
                    properties.store(out, null);
                }
            });
        }
    }

    /**
     * Gets the key to use in the capture state file. If there is not a
     * captureStateIdentifier available, we want to avoid using a hardcoded key,
     * since the same file may be used for multiple purposes, even multiple
     * filters of the same type. Of course this is not desired configuration,
     * but may be more convenient for lazy users!
     * 
     * @return
     */
    private String getPropertyKey() {
        if (StringUtils.isNullOrEmpty(captureStateIdentifier)) {
            if (lastModifiedColumn.isPhysicalColumn()) {
                Table table = lastModifiedColumn.getPhysicalColumn().getTable();
                if (table != null && !StringUtils.isNullOrEmpty(table.getName())) {
                    return table.getName() + "." + lastModifiedColumn.getName() + ".GreatestLastModifiedValue";
                }
            }
            return lastModifiedColumn.getName() + ".GreatestLastModifiedValue";
        }
        return captureStateIdentifier.trim() + ".GreatestLastModifiedValue";
    }

    private Properties loadProperties() throws IOException {
        final Properties properties = new Properties();
        if (!captureStateFile.isExists()) {
            logger.info("Capture state file does not exist: {}", captureStateFile);
            return properties;
        }

        captureStateFile.read(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                properties.load(in);
            }
        });
        return properties;
    }

    @Override
    public ValidationCategory categorize(InputRow inputRow) {
        final Object lastModified = inputRow.getValue(lastModifiedColumn);
        final long rowMillis;
        final int rowNanos;

        if (lastModified == null) {
            rowMillis = -1l;
            rowNanos = 0;
        } else if (lastModified instanceof Timestamp) {
            final Timestamp ts = (Timestamp) lastModified;
            rowMillis = ts.getTime();
            rowNanos = ts.getNanos();
        } else if (lastModified instanceof String) {
            final Date date = ConvertToDateTransformer.getInternalInstance().transformValue(lastModified);
            if (date == null) {
                rowMillis = -1l;
            } else {
                rowMillis = date.getTime();
            }
            rowNanos = 0;
        } else {
            final Number lastModifiedAsNumber = convertToNumber(lastModified);
            if (lastModifiedAsNumber == null) {
                rowMillis = -1l;
            } else {
                rowMillis = lastModifiedAsNumber.longValue();
            }
            rowNanos = 0;
        }

        if (rowMillis != -1l) {
            synchronized (this) {
                if (_greatestEncounteredMillis == -1l || _greatestEncounteredMillis < rowMillis) {
                    _greatestEncounteredMillis = rowMillis;
                    _greatestEncounteredNanos = rowNanos;
                } else if (_greatestEncounteredMillis == rowMillis && _greatestEncounteredNanos < rowNanos) {
                    _greatestEncounteredMillis = rowMillis;
                    _greatestEncounteredNanos = rowNanos;
                }
            }
        }

        if (_lastModifiedThresholdMillis == -1l) {
            return ValidationCategory.VALID;
        }

        if (rowMillis == -1l) {
            logger.info("Value of {} was not comparable, returning INVALID category: {}", lastModifiedColumn.getName(),
                    inputRow);
            return ValidationCategory.INVALID;
        }

        if (_lastModifiedThresholdMillis < rowMillis) {
            return ValidationCategory.VALID;
        }
        return ValidationCategory.INVALID;
    }

    private Number convertToNumber(final Object lastModified) {
        return ConvertToNumberTransformer.transformValue(lastModified);
    }

}
