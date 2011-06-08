#!/bin/sh
export MAVEN_OPTS=-Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration
rm target/generated-resources/xml/xslt/*.html
mvn generate-resources xml:transform
