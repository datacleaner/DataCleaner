@echo off
set MAVEN_OPTS=-Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XIncludeParserConfiguration
del target\generated-resources\xml\xslt\*.html
mvn generate-resources xml:transform