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
package dk.eobjects.datacleaner.webmonitor.model;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.ProfileRunner;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfileConfiguration;
import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.datacleaner.webmonitor.PersistenceHelper;
import dk.eobjects.datacleaner.webmonitor.WebmonitorHelper;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class ProfilerJob implements Serializable, Job {

	private static final Log _log = LogFactory.getLog(ProfilerJob.class);

	private static final long serialVersionUID = 2266406226450576308L;
	private long _id;
	private String _name;
	private Set<Trigger> _triggers = new HashSet<Trigger>();
	private Set<ProfilerResult> _results = new HashSet<ProfilerResult>();
	private String _filename;

	public Set<Trigger> getTriggers() {
		return _triggers;
	}

	public void setTriggers(Set<Trigger> triggers) {
		_triggers = triggers;
	}

	public void setResults(Set<ProfilerResult> results) {
		_results = results;
	}

	public Set<ProfilerResult> getResults() {
		return _results;
	}

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		_id = id;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getFilename() {
		return _filename;
	}

	public void setFilename(String filename) {
		_filename = filename;
	}

	public JobDetail toJobDetail(WebmonitorHelper webmonitorHelper,
			PersistenceHelper persistenceHelper) {
		JobDetail jobDetail = new JobDetail();
		jobDetail.setJobClass(ProfilerJob.class);
		jobDetail.setName(_name);
		JobDataMap map = new JobDataMap();
		File file = webmonitorHelper.getFile(_filename);
		map.put("file", file);
		map.put("id", _id);
		map.put("persistenceHelper", persistenceHelper);
		jobDetail.setJobDataMap(map);
		return jobDetail;
	}

	public void execute(JobExecutionContext executionContext)
			throws JobExecutionException {
		// Recreate the state of the ProfilerJob (it will be instantiated by
		// Quartz before execute is called)
		JobDataMap jobDataMap = executionContext.getJobDetail().getJobDataMap();
		_id = jobDataMap.getLong("id");
		_name = executionContext.getJobDetail().getName();
		File file = (File) jobDataMap.get("file");

		PersistenceHelper persistenceHelper = (PersistenceHelper) jobDataMap
				.get("persistenceHelper");
		if (file == null) {
			_log.error("File is null");
			throw new JobExecutionException("File cannot be null");
		}
		_log.info("Executing profiler job '"
				+ executionContext.getJobDetail().getName() + "'");
		try {
			Document document = DomHelper.getDocumentBuilder().parse(file);
			Element node = document.getDocumentElement();
			// TODO: Get "profiler" literal from some core constant
			if ("profiler".equals(node.getNodeName())) {
				Node dataContextSelectionNode = DomHelper.getChildNodesByName(
						node, DataContextSelection.NODE_NAME).get(0);
				DataContextSelection dataContextSelection = DataContextSelection
						.deserialize(dataContextSelectionNode);

				DataContext dc = dataContextSelection.getDataContext();
				List<Node> configurationNodes = DomHelper.getChildNodesByName(
						node, ProfileConfiguration.NODE_NAME);
				Set<Column> columns = new HashSet<Column>();
				ProfileRunner runner = new ProfileRunner();
				for (Node configurationNode : configurationNodes) {
					ProfileConfiguration configuration = ProfileConfiguration
							.deserialize(configurationNode, dc);
					columns.addAll(Arrays.asList(configuration.getColumns()));
					runner.addConfiguration(configuration);
				}
				runner.execute(dataContextSelection.getDataContext());
				dataContextSelection.selectNothing();

				Table[] resultTables = runner.getResultTables();
				for (Table table : resultTables) {
					List<IProfileResult> results = runner
							.getResultsForTable(table);
					persistenceHelper.saveResults(this, table, results);
				}
			} else {
				throw new JobExecutionException(
						"Could not deserialize node with name '"
								+ node.getNodeName() + "'");
			}
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(e);
			if (e instanceof JobExecutionException) {
				throw (JobExecutionException) e;
			} else {
				throw new JobExecutionException(e);
			}
		}
	}

	@Override
	public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this,
				ToStringStyle.SHORT_PREFIX_STYLE).append("id", _id).append(
				"name", _name).append("filename", _filename);
		return tsb.toString();
	}
}