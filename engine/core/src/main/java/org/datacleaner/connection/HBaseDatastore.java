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
package org.datacleaner.connection;

import java.util.List;

import org.apache.metamodel.hbase.HBaseConfiguration;
import org.apache.metamodel.hbase.HBaseDataContext;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.SimpleTableDef;

/**
 * Datastore implementation for HBase.
 */
public class HBaseDatastore extends UsageAwareDatastore<HBaseDataContext> {

    private static final long serialVersionUID = 1L;

    private final int _zookeeperPort;
    private final String _zookeeperHostname;
    private final SimpleTableDef[] _tableDefs;

    public HBaseDatastore(final String name, final String zookeeperHostname, final int zookeeperPort) {
        this(name, zookeeperHostname, zookeeperPort, null);
    }

    public HBaseDatastore(final String name, final String zookeeperHostname, final int zookeeperPort,
            final SimpleTableDef[] tableDefs) {
        super(name);
        _zookeeperHostname = zookeeperHostname;
        _zookeeperPort = zookeeperPort;
        _tableDefs = tableDefs;
    }

    @Override
    public DatastoreConnection openConnection() {
        return (DatastoreConnection) super.openConnection();
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<HBaseDataContext> createDatastoreConnection() {
        final HBaseConfiguration hBaseConfiguration =
                new HBaseConfiguration("HBase", _zookeeperHostname, _zookeeperPort, _tableDefs, ColumnType.STRING);
        final HBaseDataContext hBaseDataContext = new HBaseDataContext(hBaseConfiguration);
        return new DatastoreConnectionImpl<>(hBaseDataContext, this);
    }

    public String getZookeeperHostname() {
        return _zookeeperHostname;
    }

    public int getZookeeperPort() {
        return _zookeeperPort;
    }

    public SimpleTableDef[] getTableDefs() {
        return _tableDefs;
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_zookeeperHostname);
        identifiers.add(_zookeeperPort);
        identifiers.add(_tableDefs);
    }
}
