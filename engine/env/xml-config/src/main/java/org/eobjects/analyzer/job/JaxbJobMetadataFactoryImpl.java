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
package org.eobjects.analyzer.job;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeFactory;

import org.eobjects.analyzer.job.jaxb.JobMetadataType;
import org.eobjects.analyzer.job.jaxb.MetadataProperties;
import org.eobjects.analyzer.job.jaxb.MetadataProperties.Property;

/**
 * Default {@link JaxbJobMetadataFactory} implementation
 */
public class JaxbJobMetadataFactoryImpl implements JaxbJobMetadataFactory {

    private final DatatypeFactory _datatypeFactory;
    private final String _author;
    private final String _jobName;
    private final String _jobDescription;
    private final String _jobVersion;

    public JaxbJobMetadataFactoryImpl() {
        this(null, null, null, null);
    }

    public JaxbJobMetadataFactoryImpl(String author, String jobName, String jobDescription, String jobVersion) {
        _author = author;
        _jobName = jobName;
        _jobDescription = jobDescription;
        _jobVersion = jobVersion;
        try {
            _datatypeFactory = DatatypeFactory.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final JobMetadataType create(AnalysisJob analysisJob) throws Exception {
        final JobMetadataType jobMetadata = new JobMetadataType();

        buildMainSection(jobMetadata, analysisJob);

        buildProperties(jobMetadata, analysisJob);

        return jobMetadata;
    }

    protected void buildProperties(JobMetadataType jobMetadata, AnalysisJob analysisJob) throws Exception {
        final AnalysisJobMetadata metadata = getMetadata(analysisJob);
        if (metadata != null) {
            final Map<String, String> properties = metadata.getProperties();
            if (properties != null && !properties.isEmpty()) {
                final MetadataProperties propertiesType = new MetadataProperties();
                final List<Property> propertyList = propertiesType.getProperty();

                for (Entry<String, String> entry : properties.entrySet()) {
                    final Property property = new Property();
                    property.setName(entry.getKey());
                    property.setValue(entry.getValue());
                    propertyList.add(property);
                }

                jobMetadata.setMetadataProperties(propertiesType);
            }
        }
    }

    protected void buildMainSection(JobMetadataType jobMetadata, AnalysisJob analysisJob) throws Exception {
        final AnalysisJobMetadata metadata = getMetadata(analysisJob);

        final Date createdDate = metadata.getCreatedDate();
        if (createdDate != null) {
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(createdDate);
            jobMetadata.setCreatedDate(_datatypeFactory.newXMLGregorianCalendar(c));
        }
        jobMetadata.setUpdatedDate(_datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar()));
        jobMetadata.setAuthor(_author == null ? metadata.getAuthor() : _author);
        jobMetadata.setJobName(_jobName == null ? metadata.getJobName() : _jobName);
        jobMetadata.setJobDescription(_jobDescription == null ? metadata.getJobDescription() : _jobDescription);
        jobMetadata.setJobVersion(_jobVersion == null ? metadata.getJobVersion() : _jobVersion);
    }

    protected AnalysisJobMetadata getMetadata(AnalysisJob analysisJob) {
        final AnalysisJobMetadata metadata = analysisJob.getMetadata();
        if (metadata == null) {
            return AnalysisJobMetadata.EMPTY_METADATA;
        }
        return metadata;
    }
}
