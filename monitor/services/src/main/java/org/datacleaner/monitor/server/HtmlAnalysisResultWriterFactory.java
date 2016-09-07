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

import java.util.Map.Entry;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.html.BaseHeadElement;
import org.datacleaner.result.html.HeadElement;
import org.datacleaner.result.html.HtmlAnalysisResultWriter;
import org.apache.metamodel.util.Predicate;
import org.springframework.stereotype.Component;

/**
 * Factory for {@link HtmlAnalysisResultWriter} objects.
 */
@Component
public class HtmlAnalysisResultWriterFactory {

    private String resourcesDirectory;

    public String getResourcesDirectory() {
        return resourcesDirectory;
    }

    public void setResourcesDirectory(String resourcesDirectory) {
        this.resourcesDirectory = resourcesDirectory;
    }

    public HtmlAnalysisResultWriter create(
            boolean tabs,
            Predicate<Entry<ComponentJob, AnalyzerResult>> jobInclusionPredicate,
            boolean headers) {
        
        return new HtmlAnalysisResultWriter(tabs, jobInclusionPredicate,
                headers) {
            @Override
            protected HeadElement createBaseHeadElement() {
                if (resourcesDirectory == null) {
                    return super.createBaseHeadElement();
                } else {
                    return new BaseHeadElement(resourcesDirectory);
                }
            }
        };
    }
}
