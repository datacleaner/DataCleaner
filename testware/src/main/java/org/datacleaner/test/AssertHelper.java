package org.datacleaner.test;

/**
 * A utility class containing methods useful during unit testing.
 *
 */
public class AssertHelper {

    public static final String FILE_PATH_REPLACEMENT = "[MASKED FILE PATH]";

    /**
     * Replaces every occurence of a file path starting with file:// prefix with
     * [MASKED FILE PATH]. Useful for testing DataCleaner job XML files against benchmarks
     * 
     * Example: Replaces 
     * <property name="File" value="file://C:/CUSTOMERS_address_correction.csv"/> 
     * with <property name="File" value="file://[MASKED FILE PATH]"/>
     * 
     * @param fileString
     *            The text to replace file paths in
     * @return
     */
    public static String maskFilePaths(final String fileString) {
        final String filePrefix = "\"file://";
        return fileString.replaceAll(filePrefix + "(.*?)\"", filePrefix + FILE_PATH_REPLACEMENT + "\"");
    }

}
