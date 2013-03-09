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
package org.eobjects.datacleaner.monitor.shared.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A popup for a datastore wizard.
 */
public class DatastoreWizardPopupPanel extends AbstractWizardPopupPanel {

	public DatastoreWizardPopupPanel(WizardServiceAsync service,
			TenantIdentifier tenant) {
		super("New datastore", service, tenant);
		addStyleName("DatastoreWizardPopupPanel");

		service.getDatastoreWizardIdentifiers(tenant,
				new DCAsyncCallback<List<WizardIdentifier>>() {
					@Override
					public void onSuccess(List<WizardIdentifier> wizards) {
						showWizardSelection(wizards);
					}
				});
	}

	@Override
	protected int getStepsBeforeWizardPages() {
		return 1;
	}

	protected void showWizardSelection(final List<WizardIdentifier> wizards) {
		final FlowPanel panel = new FlowPanel();

		final TextBox nameTextBox = new TextBox();

		panel.add(new Label("Please select the datastore type:"));

		final List<RadioButton> radios = new ArrayList<RadioButton>(
				wizards.size());

		for (final WizardIdentifier wizard : wizards) {
			final RadioButton radio = new RadioButton("wizardIdentifier",
					wizard.getDisplayName());
			radio.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setSteps(wizard.getExpectedPageCount() + getStepsBeforeWizardPages());
					setProgress(0);
				}
			});
			panel.add(radio);
			radios.add(radio);
		}

		panel.add(new Label(
				"Please name the datastore you are about to create:"));
		panel.add(nameTextBox);

		setNextClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String datastoreName = nameTextBox.getText();
				if (datastoreName == null || datastoreName.trim().isEmpty()) {
					throw new DCUserInputException(
							"Please enter a valid datastore name");
				}
				for (int i = 0; i < radios.size(); i++) {
					final RadioButton radio = radios.get(i);
					if (radio.getValue().booleanValue()) {
						final WizardIdentifier wizard = wizards.get(i);
						setLoading();
						setHeader("New datastore: " + datastoreName);
						_service.startDatastoreWizard(_tenant, wizard,
								datastoreName, createNextPageCallback());
						return;
					}
				}
			}
		});

		setContent(panel);
		center();
	}

	@Override
	protected void wizardFinished() {
		final Button button = new Button("Close");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// full page refresh.
				Window.Location.reload();
			}
		});

		setContent(new Label("Datastore created! Wizard finished."));
		getButtonPanel().clear();
		addButton(button);
		center();
	}
}
