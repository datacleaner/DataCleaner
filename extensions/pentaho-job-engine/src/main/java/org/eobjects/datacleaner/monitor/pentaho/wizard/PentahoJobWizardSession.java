/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.pentaho.wizard;

import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.pentaho.JaxbPentahoJobTypeAdaptor;
import org.eobjects.datacleaner.monitor.pentaho.PentahoCarteClient;
import org.eobjects.datacleaner.monitor.pentaho.PentahoJobEngine;
import org.eobjects.datacleaner.monitor.pentaho.PentahoTransformation;
import org.eobjects.datacleaner.monitor.pentaho.jaxb.ObjectFactory;
import org.eobjects.datacleaner.monitor.pentaho.jaxb.PentahoJobType;
import org.eobjects.datacleaner.monitor.server.wizard.JobNameWizardPage;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.job.AbstractJobWizardSession;
import org.eobjects.datacleaner.monitor.wizard.job.JobWizardContext;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.apache.metamodel.util.Action;

/**
 * Wizard session for creating Pentaho jobs
 */
final class PentahoJobWizardSession extends AbstractJobWizardSession {

    private final PentahoJobType _pentahoJobType;
    private String _jobName;

    public PentahoJobWizardSession(JobWizardContext context) {
        super(context);
        _pentahoJobType = new PentahoJobType();
    }

    @Override
    public WizardPageController firstPageController() {
        return new PentahoCarteConfigurationPage(0) {
            @Override
            protected WizardPageController nextPageController(String hostname, int port, String username,
                    String password) {
                return jobSelectionPage(hostname, port, username, password);
            }
        };
    }

    public PentahoJobSelectionPage jobSelectionPage(String hostname, int port, String username, String password) {
        _pentahoJobType.setCarteHostname(hostname);
        _pentahoJobType.setCartePort(port);
        _pentahoJobType.setCarteUsername(username);
        _pentahoJobType.setCartePassword(password);

        final List<PentahoTransformation> availableTransformations;
        try {
            final PentahoCarteClient carteClient = new PentahoCarteClient(_pentahoJobType);
            availableTransformations = carteClient.getAvailableTransformations();
        } catch (Exception e) {
            throw new DCUserInputException(e.getMessage());
        }

        return new PentahoJobSelectionPage(1, availableTransformations) {
            @Override
            protected WizardPageController nextPageController(PentahoTransformation transformation, String groupName) {
                _pentahoJobType.setTransformationId(transformation.getId());
                _pentahoJobType.setTransformationName(transformation.getName());
                _pentahoJobType.setGroupName(groupName);

                return new JobNameWizardPage(getWizardContext(), 2, transformation.getName()) {
                    @Override
                    protected WizardPageController nextPageController(String name) {
                        _jobName = name;
                        return null;
                    }
                };
            }
        };
    }

    @Override
    public String finished() {
        final TenantContext tenantContext = getWizardContext().getTenantContext();
        final RepositoryFolder jobFolder = tenantContext.getJobFolder();

        final JAXBElement<PentahoJobType> pentahoJob = new ObjectFactory().createPentahoJob(_pentahoJobType);

        jobFolder.createFile(_jobName + PentahoJobEngine.EXTENSION, new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                JaxbPentahoJobTypeAdaptor adaptor = new JaxbPentahoJobTypeAdaptor();
                adaptor.marshal(pentahoJob, out);
            }
        });

        return _jobName;
    }

}
