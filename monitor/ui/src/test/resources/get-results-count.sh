datacleaner_directory=/usr/local/tomcat/webapps/DataCleaner-monitor-ui-*-SNAPSHOT
results_directory=$datacleaner_directory/repository/demo/results
job_prefix=simple_numbers_distribution
ls $results_directory | grep $job_prefix | wc -l
