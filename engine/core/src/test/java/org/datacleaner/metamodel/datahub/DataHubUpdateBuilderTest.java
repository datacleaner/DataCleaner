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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.datacleaner.metamodel.datahub.update.UpdateData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataHubUpdateBuilderTest {
        
        @Rule
        public ExpectedException thrown= ExpectedException.none();
        
        @Mock
        DataHubUpdateCallback callback;
        
        @Mock
        Table table;
        
        RowUpdationBuilder rowUpdationBuilder;

        
        @Before
        public void init() {
            Mockito.when(table.getName()).thenReturn("person");
            Mockito.when(table.getColumns()).thenReturn(
                    new Column[] {
                            new MutableColumn("_trackcode", ColumnType.CHAR), 
                            new MutableColumn("name", ColumnType.CHAR), 
                            new MutableColumn("age", ColumnType.CHAR)});
            rowUpdationBuilder = new DataHubUpdateBuilder(callback, table);
        }
        
        
        @Test
        public void shouldCallExecuteGoldenRecord() {
            Column grIdColumn = new MutableColumn("gr_id", ColumnType.CHAR);
            final FilterItem grIdFilter = new FilterItem(new SelectItem(grIdColumn),
                    OperatorType.EQUALS_TO, "123");
            rowUpdationBuilder = rowUpdationBuilder.where(grIdFilter);
            Column updateColumn1 = new MutableColumn("name", ColumnType.CHAR);
            rowUpdationBuilder.value(updateColumn1, "billy");
            Column updateColumn2 = new MutableColumn("age", ColumnType.CHAR);
            rowUpdationBuilder.value(updateColumn2, "10");
            rowUpdationBuilder.execute();
            ArgumentCaptor<UpdateData> argument = ArgumentCaptor.forClass(UpdateData.class);
            verify(callback, times(1)).executeUpdate(argument.capture());
            UpdateData data = argument.getValue();
            assertEquals("123", data.getGrId());
            assertEquals(2, data.getFields().length);
            assertEquals("name", data.getFields()[0].getName());
            assertEquals("billy", data.getFields()[0].getValue());
            assertEquals("age", data.getFields()[1].getName());
            assertEquals("10", data.getFields()[1].getValue());
        }
        
        @Test
        public void shouldHandleNullValuesInUpdate() {
            Column grIdColumn = new MutableColumn("gr_id", ColumnType.CHAR);
            final FilterItem grIdFilter = new FilterItem(new SelectItem(grIdColumn),
                    OperatorType.EQUALS_TO, "123");
            rowUpdationBuilder = rowUpdationBuilder.where(grIdFilter);
            Column updateColumn1 = new MutableColumn("name", ColumnType.CHAR);
            rowUpdationBuilder.value(updateColumn1, null);
            rowUpdationBuilder.execute();
            ArgumentCaptor<UpdateData> argument = ArgumentCaptor.forClass(UpdateData.class);
            verify(callback, times(1)).executeUpdate(argument.capture());
            UpdateData data = argument.getValue();
            assertThat(data.getGrId(), is("123"));
            assertThat(data.getFields().length, is(1));
            assertThat(data.getFields()[0].getName(), is("name"));
            assertThat(data.getFields()[0].getValue(), is(nullValue(String.class)));
        }

        @Test
        public void shouldThrowForMissingWhereClause() {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Updates should have the gr_id as the sole condition value.");        
            rowUpdationBuilder.execute();
        }

        @Test
        public void shouldThrowForInvalidColumnInWhereClause() {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Updates should have the gr_id as the sole condition value.");        
            Column illegalColumn = new MutableColumn("illegal", ColumnType.CHAR);
            final FilterItem illegalFilter = new FilterItem(new SelectItem(illegalColumn),
                    OperatorType.EQUALS_TO, "nonsense");
            rowUpdationBuilder = rowUpdationBuilder.where(illegalFilter);
            rowUpdationBuilder.execute();
        }

        @Test
        public void shouldThrowForIllegalUpdateColumn() {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("Updates are not allowed on fields containing meta data, identified by the prefix \" _\".");        
            Column grIdColumn = new MutableColumn("gr_id", ColumnType.CHAR);
            final FilterItem grIdFilter = new FilterItem(new SelectItem(grIdColumn),
                    OperatorType.EQUALS_TO, "123");
            rowUpdationBuilder = rowUpdationBuilder.where(grIdFilter);
            Column illegalColumn = new MutableColumn("_trackcode", ColumnType.CHAR);
            rowUpdationBuilder.value(illegalColumn, "updated");
            rowUpdationBuilder.execute();
        }

        @Test
        public void shouldThrowForNonExistingUpdateColumn() {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("No such column in table: Column[name=nonsense,columnNumber=0,type=CHAR,nullable=null,nativeType=null,columnSize=null]");        
            Column grIdColumn = new MutableColumn("gr_id", ColumnType.CHAR);
            final FilterItem grIdFilter = new FilterItem(new SelectItem(grIdColumn),
                    OperatorType.EQUALS_TO, "123");
            rowUpdationBuilder = rowUpdationBuilder.where(grIdFilter);
            Column nonExistingColumn = new MutableColumn("nonsense", ColumnType.CHAR);
            rowUpdationBuilder.value(nonExistingColumn, "bla");
            rowUpdationBuilder.execute();
        }

}
