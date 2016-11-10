package org.datacleaner.wizard;

import java.util.List;

public interface WizardProvider {
    String getName();

    List<JobWizard> getWizards();
}
