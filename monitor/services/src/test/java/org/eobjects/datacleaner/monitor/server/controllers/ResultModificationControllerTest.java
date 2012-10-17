/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactoryImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.file.FileRepository;

import junit.framework.TestCase;

public class ResultModificationControllerTest extends TestCase {

    ResultModificationController controller;

    protected void setUp() throws Exception {
        controller = new ResultModificationController();

        File targetDir = new File("target/repo_result_modification");
        FileUtils.deleteDirectory(targetDir);
        FileUtils.copyDirectory(new File("src/test/resources/example_repo"), targetDir);
        Repository repository = new FileRepository(targetDir);

        controller._contextFactory = new TenantContextFactoryImpl(repository, new InjectionManagerFactoryImpl());
    }

    public void testModifyJob() throws Exception {
        ResultModificationPayload input = new ResultModificationPayload();
        input.setJob("email_standardizer");

        Map<String, String> response = controller.modifyResult("tenant1", "product_profiling-3", input);
        assertEquals("{new_result_name=email_standardizer-1338990580902.analysis.result.dat, "
                + "old_result_name=product_profiling-3.analysis.result.dat, "
                + "repository_url=/repository/tenant1/results/email_standardizer-1338990580902.analysis.result.dat}",
                response.toString());
    }

    public void testModifyDate() throws Exception {
        ResultModificationPayload input = new ResultModificationPayload();
        input.setDate("2012-12-17");

        // reproduce the date, to make unittest locale-independent
        Date date = ConvertToDateTransformer.getInternalInstance().transformValue("2012-12-17");

        Map<String, String> response = controller.modifyResult("tenant1", "product_profiling-3", input);
        assertEquals("{new_result_name=product_profiling-" + date.getTime() + ".analysis.result.dat, "
                + "old_result_name=product_profiling-3.analysis.result.dat, "
                + "repository_url=/repository/tenant1/results/product_profiling-1355698800000.analysis.result.dat}",
                response.toString());
    }

    public void testModifyBothDateAndJob() throws Exception {
        ResultModificationPayload input = new ResultModificationPayload();
        input.setJob("email_standardizer");
        input.setDate("1355698800000");

        Map<String, String> response = controller.modifyResult("tenant1", "product_profiling-3", input);
        assertEquals("{new_result_name=email_standardizer-1355698800000.analysis.result.dat, "
                + "old_result_name=product_profiling-3.analysis.result.dat, "
                + "repository_url=/repository/tenant1/results/email_standardizer-1355698800000.analysis.result.dat}",
                response.toString());
    }
}
