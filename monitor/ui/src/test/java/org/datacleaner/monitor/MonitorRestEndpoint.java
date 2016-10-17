package org.datacleaner.monitor;

import java.net.URI;
import java.util.Properties;

import org.junit.rules.ExternalResource;

import io.restassured.RestAssured;

public class MonitorRestEndpoint extends ExternalResource {
    @Override
    protected void before() throws Exception {
        final Properties dockerProperties = new Properties();
        dockerProperties.load(getClass().getClassLoader().getResourceAsStream("docker.properties"));

        RestAssured.baseURI = "http://" + (new URI(System.getenv("DOCKER_HOST"))).getHost() + ":" + dockerProperties
                .getProperty("monitor.portnumber") + "/" + dockerProperties.getProperty("monitor.contextpath");
        RestAssured.basePath = "/repository/demo";
    }
}
