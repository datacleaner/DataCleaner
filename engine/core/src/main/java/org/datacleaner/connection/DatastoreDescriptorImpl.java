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

import java.util.ArrayList;
import java.util.List;

public class DatastoreDescriptorImpl implements DatastoreDescriptor {

    private final String _name;
    private final String _description;
    private final Class<? extends Datastore> _datastoreClass;
    private final List<String> _tags;
    
    public DatastoreDescriptorImpl(final String name, String description, final Class<? extends Datastore> datastoreClass, List<String> tags) {
        if (name == null) {
            throw new IllegalArgumentException("The name of the datastore cannot be null");
        }
        
        if (datastoreClass == null) {
            throw new IllegalArgumentException("The class representing the datastore cannot be null");
        }
        
        _name = name;
        _description = description;
        _datastoreClass = datastoreClass;
        _tags = tags;
    }

    public DatastoreDescriptorImpl(final String name, String description, final Class<? extends Datastore> datastoreClass) {
        this(name, description, datastoreClass, new ArrayList<String>());
    }
    
    @Override
    public String getName() {
        return _name;
    }
    
    @Override
    public String getDescription() {
        return _description;
    }
    
    @Override
    public Class<? extends Datastore> getDatastoreClass() {
        return _datastoreClass;
    }
    
    @Override
    public boolean isUpdatable() {
        Class<?>[] interfaces = _datastoreClass.getInterfaces();
        for (Class<?> implementedInterface : interfaces) {
            if (implementedInterface.equals(UpdateableDatastore.class)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public List<String> getTags() {
        return _tags;
    }
    
    @Override
    public boolean equals(Object that) {
        if (that != null) {
            if (that instanceof DatastoreDescriptor) {
                DatastoreDescriptor thatDescriptor = (DatastoreDescriptor) that;
                boolean nameEquals = this.getName().equals(thatDescriptor.getName());
                boolean datastoreClassEquals = this.getDatastoreClass().equals(thatDescriptor.getDatastoreClass());
                
                if (nameEquals && datastoreClassEquals) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this._name.hashCode();
        hash = 89 * hash + this._datastoreClass.hashCode();
        return hash;
    }
    
}
