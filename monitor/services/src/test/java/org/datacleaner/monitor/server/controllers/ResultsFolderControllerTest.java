package org.datacleaner.monitor.server.controllers;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.datacleaner.monitor.server.job.DefaultJobEngineManager;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ResultsFolderControllerTest {
    private Repository repository;
    private TenantContextFactoryImpl tenantContextFactory;
    private ResultsFolderController resultsFolderController;

    @Before
    public void setUp() throws Exception {
        final ApplicationContext applicationContext =
                new ClassPathXmlApplicationContext("context/application-context.xml");
        repository = applicationContext.getBean(FileRepository.class);

        tenantContextFactory = new TenantContextFactoryImpl(repository, new DataCleanerEnvironmentImpl(),
                new DefaultJobEngineManager(applicationContext));
        resultsFolderController = new ResultsFolderController();
        resultsFolderController._tenantContextFactory = tenantContextFactory; 
    }
 
    
    @Test
    public void resultsFolderJson() throws Exception {
        final List<Map<String, String>> resultsFolderJson = resultsFolderController.resultsFolderJson("tenant1");
        assertEquals(6, resultsFolderJson.size()); 
    }
    
    @Test
    public void resultsFolderJsonAfterTimestamp() throws Exception {
        final List<Map<String, String>> resultsFolderJson = resultsFolderController.resultsFolderJsonAfterTimestamp("tenant1", 1);
        assertEquals(5, resultsFolderJson.size()); 
    }
    
    
}
