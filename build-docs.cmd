@echo off
set MAVEN_OPTS=-Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration
mvn generate-resources xml:transform