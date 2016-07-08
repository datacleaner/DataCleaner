package org.datacleaner.monitor.server;

import org.datacleaner.monitor.referencedata.ReferenceDataService;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("RefereceDataService")
public class ReferenceDataServiceImpl implements ReferenceDataService, ApplicationContextAware{

    private static Logger logger = LoggerFactory.getLogger(ReferenceDataServiceImpl.class); 
    private ApplicationContext _applicationContext;
    
    @Override
    public String getDictionaries(TenantIdentifier tenant) {
        // TODO uto-generated method stub
        return "I am a list of dictionaries"; 
        
    }

    @Override
    public void getSynonymsCatalog(TenantIdentifier tenant) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getStringPatterns(TenantIdentifier tenant) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.info("The application context has been initialized" + applicationContext.getDisplayName()); 
        _applicationContext = applicationContext; 
    }

}
