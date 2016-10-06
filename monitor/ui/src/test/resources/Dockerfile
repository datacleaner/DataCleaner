FROM tomcat:8-jre8

# The docker-maven-plugin adds the war to the maven folder because of the <descriptorRef> element in the <assembly> 
# element within <build> contains the value "artifact", which indicates the "artifact" produced by the maven module 
# is added to the created docker image. Files and artifacts contained in the assembly are by default copied under the
# "/maven" directory.
COPY maven/${project.build.finalName}.war /usr/local/tomcat/webapps
