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
package org.eobjects.analyzer.beans.composition;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.FileProperty;
import org.eobjects.analyzer.beans.api.FileProperty.FileAccessMode;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.categories.CompositionCategory;
import org.eobjects.analyzer.beans.transform.AbstractWrappedAnalysisJobTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.Resource;

@TransformerBean("Invoke child Analysis job")
@Description("Wraps another (external) Analysis job's transformations and invokes them as an integrated part of the current job. Using this transformation you can compose parent and child jobs for more coarse or more fine granularity of transformations.")
@Categorized(CompositionCategory.class)
public class InvokeChildAnalysisJobTransformer extends AbstractWrappedAnalysisJobTransformer {

    @Configured
    InputColumn<?>[] input;

    @Configured("Analysis job")
    @FileProperty(accessMode = FileAccessMode.OPEN, extension = ".analysis.xml")
    Resource analysisJobResource;

    @Override
    protected AnalysisJob createWrappedAnalysisJob() {
        AnalysisJob job = analysisJobResource.read(new Func<InputStream, AnalysisJob>() {
            @Override
            public AnalysisJob eval(InputStream in) {
                JaxbJobReader reader = new JaxbJobReader(getAnalyzerBeansConfiguration());
                AnalysisJobBuilder jobBuilder = reader.create(in);
                AnalysisJob job = jobBuilder.toAnalysisJob(false);
                return job;
            }
        });
        return job;
    }

    @Override
    protected Map<InputColumn<?>, InputColumn<?>> getInputColumnConversion(AnalysisJob wrappedAnalysisJob) {
        Collection<InputColumn<?>> sourceColumns = wrappedAnalysisJob.getSourceColumns();
        if (input.length < sourceColumns.size()) {
            throw new IllegalStateException("Wrapped job defines " + sourceColumns.size()
                    + " columns, but transformer input only defines " + input.length);
        }

        Map<InputColumn<?>, InputColumn<?>> result = new LinkedHashMap<InputColumn<?>, InputColumn<?>>();
        int i = 0;
        Iterator<InputColumn<?>> it = sourceColumns.iterator();
        while (it.hasNext()) {
            InputColumn<?> parentColumn = input[i];
            InputColumn<?> childColumn = it.next();
            result.put(parentColumn, childColumn);
            i++;
        }

        return result;
    }

}
