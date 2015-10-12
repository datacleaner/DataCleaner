package org.datacleaner.test;

import static org.junit.Assert.*;

import org.junit.Test;

public class AssertHelperTest extends AssertHelper {

    @Test
    public void testMaskFilePathsSimpleScenario() {
        final String textWithFilePath = "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://C:/Users/tomaszg/.datacleaner/4.5-RC1/CUSTOMERS_address_correction.csv\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>";
        
        final String expectedResult = "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://[MASKED FILE PATH]\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>";
        
        String maskedFilePaths = AssertHelper.maskFilePaths(textWithFilePath);
        assertEquals(expectedResult, maskedFilePaths);
    }
    
    @Test
    public void testMaskFilePathsWithSpaces() {
        final String textWithFilePath = "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://C:/Users/tomaszg/.datacleaner/4.5-RC1/CUSTOMERS address correction.csv\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>";
        
        final String expectedResult = "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://[MASKED FILE PATH]\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>";
        
        String maskedFilePaths = AssertHelper.maskFilePaths(textWithFilePath);
        assertEquals(expectedResult, maskedFilePaths);
    }
    
    @Test
    public void testMaskFilePathsMultipleOccurences() {
        final String textWithFilePath = "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://C:/Users/tomaszg/.datacleaner/4.5-RC1/CUSTOMERS address correction.csv\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>" + 
                "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://C:/Users/tomaszg/.datacleaner/4.5-RC1/CUSTOMERS address correction.csv\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>";
        
        final String expectedResult = "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://[MASKED FILE PATH]\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>" + 
                "<properties>\r\n" + 
                "                <property name=\"File\" value=\"file://[MASKED FILE PATH]\"/>\r\n" + 
                "                <property name=\"Separator char\" value=\"&amp;#44;\"/>\r\n" + 
                "                <property name=\"Quote char\" value=\"&amp;quot;\"/>\r\n" + 
                "                <property name=\"Escape char\" value=\"\\\"/>\r\n" + 
                "                <property name=\"Include header\" value=\"true\"/>\r\n" + 
                "                <property name=\"Encoding\" value=\"UTF-8\"/>\r\n" + 
                "                <property name=\"Fields\" value=\"&lt;null&gt;\"/>\r\n" + 
                "                <property name=\"Overwrite file if exists\" value=\"true\"/>\r\n" + 
                "            </properties>";
        
        String maskedFilePaths = AssertHelper.maskFilePaths(textWithFilePath);
        assertEquals(expectedResult, maskedFilePaths);
    }

}
