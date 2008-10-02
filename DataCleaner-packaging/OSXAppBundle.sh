mvn clean package osxappbundle:bundle  -Dbundlename="DataCleaner"  -DiconFile="src/main/resources/images/datacleaner.icns" -DmainClass="dk.eobjects.datacleaner.gui.DataCleanerGui" -DjvmVersion="1.5+" -DvmOptions="-Xms512m -Xmx1024m -XX:MaxPermSize=256m -XX:SurvivorRatio=16" 
rm -rf datacleaner-gui.app
mv target/datacleaner-gui.app ./