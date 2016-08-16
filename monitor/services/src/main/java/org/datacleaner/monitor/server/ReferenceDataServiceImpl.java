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
package org.datacleaner.monitor.server;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.monitor.referencedata.ReferenceDataItem;
import org.datacleaner.monitor.referencedata.ReferenceDataService;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("RefereceDataService")
public class ReferenceDataServiceImpl implements ReferenceDataService, ApplicationContextAware {

    private ApplicationContext _applicationContext;
    
    @Override
    public List<ReferenceDataItem> getDictionaries(TenantIdentifier tenant) {
        return getDummyData("dictionary");
    }

    @Override
    public List<ReferenceDataItem> getSynonymCatalogs(TenantIdentifier tenant) {
        return getDummyData("synonym-catalog");
    }

    @Override
    public List<ReferenceDataItem> getStringPatterns(TenantIdentifier tenant) {
        return getDummyData("string-pattern");
    }
    
    private List<ReferenceDataItem> getDummyData(String prefix) {
        final List<ReferenceDataItem> list = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            list.add(new ReferenceDataItem(prefix + i, prefix + "-ABC" + i));
        }
        
        return list;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
     System.out.println("The application context has been initialized: " + applicationContext.getDisplayName()); 
        _applicationContext = applicationContext; 
    }
}
