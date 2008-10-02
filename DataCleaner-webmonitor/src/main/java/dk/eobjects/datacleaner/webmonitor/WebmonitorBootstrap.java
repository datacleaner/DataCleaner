/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.webmonitor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerManager;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.ValidatorManager;
import dk.eobjects.datacleaner.webmonitor.model.ProfilerJob;
import dk.eobjects.datacleaner.webmonitor.model.Trigger;

/**
 * Bootstrap class for the webmonitor. Loads the descriptors from the spring
 * beans and initializes the DataCleaner framework.
 */
public class WebmonitorBootstrap extends HibernateTemplate implements
		ApplicationContextAware {

	private ApplicationContext _applicationContext;
	private SchedulerFactory _schedulerFactory;
	private WebmonitorHelper _webmonitorHelper;
	private PersistenceHelper _persistenceHelper;

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		_applicationContext = applicationContext;
	}

	public void setWebmonitorHelper(WebmonitorHelper webmonitorHelper) {
		_webmonitorHelper = webmonitorHelper;
	}

	public void setSchedulerFactory(SchedulerFactory schedulerFactory) {
		_schedulerFactory = schedulerFactory;
	}

	public void setPersistenceHelper(PersistenceHelper persistenceHelper) {
		_persistenceHelper = persistenceHelper;
	}

	public void init() {
		initDataCleanerManagers();
		initScheduler();
	}

	@SuppressWarnings("unchecked")
	private void initScheduler() {
		try {
			final Scheduler scheduler = _schedulerFactory.getScheduler();
			scheduler.start();
			execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {

					Iterator<Trigger> it = session
							.createQuery(
									"SELECT t FROM Trigger t INNER JOIN t.profilerJobs as profilerJob")
							.iterate();
					while (it.hasNext()) {
						Trigger trigger = it.next();
						String name = trigger.getName();

						Set<ProfilerJob> profilerJobs = trigger
								.getProfilerJobs();
						try {
							org.quartz.Trigger quartzTrigger = trigger
									.toQuartzTrigger();
							for (ProfilerJob profilerJob : profilerJobs) {
								logger.info("Scheduling profiler job: "
										+ profilerJob);
								JobDetail jobDetail = profilerJob.toJobDetail(
										_webmonitorHelper, _persistenceHelper);
								scheduler.scheduleJob(jobDetail, quartzTrigger);
							}
							logger.info("Succesfully scheduled "
									+ profilerJobs.size()
									+ " profiler jobs with trigger: "
									+ trigger.toString());
						} catch (Exception e) {
							logger.error("Error setting up initial trigger '"
									+ name + "': " + e.getMessage());
							logger.debug(e);
						}
					}
					return null;
				}
			});
		} catch (SchedulerException e) {
			logger.error(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void initDataCleanerManagers() {
		Collection profileDescriptors = _applicationContext.getBeansOfType(
				IProfileDescriptor.class).values();
		ProfilerManager.setProfileDescriptors(new ArrayList<IProfileDescriptor>(
				profileDescriptors));

		Collection validationRuleDescriptors = _applicationContext
				.getBeansOfType(IValidationRuleDescriptor.class).values();
		ValidatorManager
				.setValidationRuleDescriptors(new ArrayList<IValidationRuleDescriptor>(
						validationRuleDescriptors));
	}
}