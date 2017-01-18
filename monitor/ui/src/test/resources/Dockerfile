FROM tomcat:8-jre8

CMD ["/bin/sh", "-c", "catalina.sh run > /usr/local/tomcat/logs/catalina.out 2>&1"]

# The docker-maven-plugin adds the war to the maven folder because of the result of the <assembly> element within
# <build> is added to the created docker image. Files and artifacts contained in the assembly are by default copied
# under the "/maven" directory.
COPY maven/${project.build.finalName}.war /usr/local/tomcat/webapps/${project.build.finalName}.war
RUN mkdir /tmp/hot_folder
COPY maven/generate-hot-folder-input.sh /tmp/generate-hot-folder-input.sh
COPY maven/get-results-count.sh /tmp/get-results-count.sh
