@echo off
set MAVEN_OPTS=-Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration
cd \dev\eclipse-analyzerbeans\workspace\DataCleaner
mvn generate-resources xml:transform