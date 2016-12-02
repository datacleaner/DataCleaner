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
package org.datacleaner.metamodel.datahub;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Table;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataHubDeleteBuilderTest {

    private static final String DATASOURCE_NAME = "datasource_name";

    private static final String DATASOURCE_RECORD_ID = "datasource_record_id";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    DataHubUpdateCallback callback;

    @Mock
    Table table;

    RowDeletionBuilder sut;


    @Before
    public void init() {
        Mockito.when(table.getName()).thenReturn("person");
        sut = new DataHubDeleteBuilder(callback, table);
    }


    @Test
    public void shouldCallExecuteGoldenRecord() {
        final Column grIdColumn = new MutableColumn("gr_id", ColumnType.CHAR);
        final FilterItem grIdFilter = new FilterItem(new SelectItem(grIdColumn), OperatorType.EQUALS_TO, "123");
        sut = sut.where(grIdFilter);
        sut.execute();
        verify(callback, times(1)).executeDeleteGoldenRecord("123");
    }

    @Test
    public void shouldCallExecuteSourceRecordForPerson() {
        final Column sourceIdColumn = new MutableColumn(DATASOURCE_RECORD_ID, ColumnType.CHAR);
        final FilterItem sourceIdFilter = new FilterItem(new SelectItem(sourceIdColumn), OperatorType.EQUALS_TO, "456");
        sut = sut.where(sourceIdFilter);

        final Column sourceNameColumn = new MutableColumn(DATASOURCE_NAME, ColumnType.CHAR);
        final FilterItem sourceNameFilter =
                new FilterItem(new SelectItem(sourceNameColumn), OperatorType.EQUALS_TO, "testSource");
        sut = sut.where(sourceNameFilter);
        sut.execute();
        verify(callback, times(1)).executeDeleteSourceRecord("testSource", "456", "person");
    }

    @Test
    public void shouldCallExecuteSourceRecordForOrganization() {
        Mockito.when(table.getName()).thenReturn("organization");
        final Column sourceIdColumn = new MutableColumn(DATASOURCE_RECORD_ID, ColumnType.CHAR);
        final FilterItem sourceIdFilter = new FilterItem(new SelectItem(sourceIdColumn), OperatorType.EQUALS_TO, "456");
        sut = sut.where(sourceIdFilter);

        final Column sourceNameColumn = new MutableColumn(DATASOURCE_NAME, ColumnType.CHAR);
        final FilterItem sourceNameFilter =
                new FilterItem(new SelectItem(sourceNameColumn), OperatorType.EQUALS_TO, "testSource");
        sut = sut.where(sourceNameFilter);
        sut.execute();
        verify(callback, times(1)).executeDeleteSourceRecord("testSource", "456", "organization");
    }

    @Test
    public void shouldThrowForMissingWhereClause() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Delete requires a condition");
        sut.execute();
    }

    @Test
    public void shouldThrowForInvalidColumnInWhereClause() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Delete condition is not valid");
        final Column illegalColumn = new MutableColumn("illegal", ColumnType.CHAR);
        final FilterItem illegalFilter =
                new FilterItem(new SelectItem(illegalColumn), OperatorType.EQUALS_TO, "nonsense");
        sut = sut.where(illegalFilter);
        sut.execute();
    }

    @Test
    public void shouldThrowForTooManyColumnsInGoldenRecordWhereClause() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Delete requires the gr_id as the sole condition value.");
        final Column grIdColumn = new MutableColumn("gr_id", ColumnType.CHAR);
        final FilterItem grIdFilter = new FilterItem(new SelectItem(grIdColumn), OperatorType.EQUALS_TO, "123");
        sut = sut.where(grIdFilter);
        final Column illegalColumn = new MutableColumn("illegal", ColumnType.CHAR);
        final FilterItem illegalFilter =
                new FilterItem(new SelectItem(illegalColumn), OperatorType.EQUALS_TO, "nonsense");
        sut = sut.where(illegalFilter);
        sut.execute();
    }

    @Test
    public void shouldThrowForInvalidColumnsInSourceRecordWhereClause() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Delete must be executed on a SourceRecordsGoldenRecordFormat table using "
                + "datasource_record_id and datasource_name as condition values.");

        final Column sourceIdColumn = new MutableColumn(DATASOURCE_RECORD_ID, ColumnType.CHAR);
        final FilterItem sourceIdFilter = new FilterItem(new SelectItem(sourceIdColumn), OperatorType.EQUALS_TO, "456");
        sut = sut.where(sourceIdFilter);

        final Column illegalColumn = new MutableColumn("illegal", ColumnType.CHAR);
        final FilterItem illegalFilter =
                new FilterItem(new SelectItem(illegalColumn), OperatorType.EQUALS_TO, "nonsense");
        sut = sut.where(illegalFilter);
        sut.execute();
    }

    @Test
    public void shouldThrowForTooManyColumnsInSourceRecordWhereClause() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Delete must be executed on a SourceRecordsGoldenRecordFormat table using "
                + "datasource_record_id and datasource_name as condition values.");

        final Column sourceIdColumn = new MutableColumn(DATASOURCE_RECORD_ID, ColumnType.CHAR);
        final FilterItem sourceIdFilter = new FilterItem(new SelectItem(sourceIdColumn), OperatorType.EQUALS_TO, "456");
        sut = sut.where(sourceIdFilter);

        final Column sourceNameColumn = new MutableColumn(DATASOURCE_NAME, ColumnType.CHAR);
        final FilterItem sourceNameFilter =
                new FilterItem(new SelectItem(sourceNameColumn), OperatorType.EQUALS_TO, "testSource");
        sut = sut.where(sourceNameFilter);

        final Column illegalColumn = new MutableColumn("illegal", ColumnType.CHAR);
        final FilterItem illegalFilter =
                new FilterItem(new SelectItem(illegalColumn), OperatorType.EQUALS_TO, "nonsense");
        sut = sut.where(illegalFilter);
        sut.execute();
    }

}
