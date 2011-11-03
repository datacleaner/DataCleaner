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
package org.eobjects.datacleaner.testtools;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.storage.RowAnnotation;
import org.eobjects.analyzer.storage.RowAnnotationFactory;

public abstract class TestToolAnalyzer implements
		Analyzer<TestToolAnalyzerResult> {

	@Provided
	RowAnnotation errornousRowAnnotation;

	@Provided
	RowAnnotationFactory annotationFactory;

	@Configured(required = false)
	EmailConfiguration emailConfiguration;

	private final AtomicInteger successCounter = new AtomicInteger(0);

	@Override
	public final void run(InputRow row, int distinctCount) {
		boolean valid = isValid(row);
		if (valid) {
			for (int i = 0; i < distinctCount; i++) {
				successCounter.incrementAndGet();
			}
		} else {
			annotationFactory.annotate(row, distinctCount,
					errornousRowAnnotation);
		}
	}

	@Override
	public final TestToolAnalyzerResult getResult() {
		if (emailConfiguration != null) {
			boolean success = errornousRowAnnotation.getRowCount() == 0;
			if (!success || emailConfiguration.isSendEmailOnSuccess()) {
				MailUtils.sendMail(emailConfiguration,
						createEmailSubject(success), createEmailBody());
			}
		}

		return new TestToolAnalyzerResult(successCounter.get(),
				annotationFactory, errornousRowAnnotation,
				getColumnsOfInterest());
	}

	private String createEmailSubject(boolean success) {
		return "DC test tools notification: "
				+ getClass().getAnnotation(AnalyzerBean.class).value() + ": "
				+ (success ? "SUCCESS" : "FAILURE");
	}

	private String createEmailBody() {
		return "TODO: This is the email body!";
	}

	protected abstract boolean isValid(InputRow row);

	protected abstract InputColumn<?>[] getColumnsOfInterest();
}
