Packaging DataCleaner can sometimes be tricky as we rely on an
external maven repository to generate the .exe file of the
win32 distribution.

To create the win32 .exe file run maven like this:

 mvn install launch4j:launch4j
 
Sometimes this will cause an error because the third party
repository is not that stable :-( You can also download launch4j
and create the .exe manually.