/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.result.renderer;

import java.util.Arrays;

import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.CrosstabResult;

import junit.framework.TestCase;

public class CrosstabTextRendererTest extends TestCase {
    
    public void testEmptyCrosstab() throws Exception {
        CrosstabDimension genderDimension = new CrosstabDimension("Gender");
        genderDimension.addCategory("Male");
        genderDimension.addCategory("Female");
        CrosstabDimension regionDimension = new CrosstabDimension("Region");

        Crosstab<Integer> c = new Crosstab<Integer>(Integer.class, genderDimension, regionDimension);
        
        String s = new CrosstabTextRenderer().render(new CrosstabResult(c));
        assertEquals("   Male Female \n", s);
    }

    public void testSimpleCrosstab() throws Exception {
        Crosstab<Integer> c = new Crosstab<Integer>(Integer.class, "Gender", "Region");
        c.where("Gender", "Male").where("Region", "EU").put(1, true);
        c.where("Gender", "Male").where("Region", "USA").put(2, true);
        c.where("Gender", "Female").where("Region", "EU").put(3, true);
        c.where("Gender", "Female").where("Region", "USA").put(4, true);

        String s = new CrosstabTextRenderer().render(new CrosstabResult(c));
        assertEquals("      Male Female \nEU       1      3 \nUSA      2      4 \n", s);
    }

    public void testOneDimension() throws Exception {
        Crosstab<Integer> c = new Crosstab<Integer>(Integer.class, "Region");
        c.where("Region", "EU").put(1, true);
        c.where("Region", "USA").put(2, true);
        c.where("Region", "Asia").put(3, true);

        CrosstabTextRenderer crosstabRenderer = new CrosstabTextRenderer();
        String result = crosstabRenderer.render(c);
        assertEquals("    EU    USA   Asia \n" + "     1      2      3 \n", result.replaceAll("\"", "'"));
    }

    public void testMultipleDimensions() throws Exception {
        // creates a crosstab of some metric (simply iterated for simplicity)
        // based on person characteristica, examplified with Region (EU and
        // USA), Age-group (children, teenagers and adult)
        // and Gender (male and female)

        Crosstab<Integer> c = new Crosstab<Integer>(Integer.class, "Region", "Age-group", "Gender", "Native");
        String[] genderValues = { "Male", "Female" };
        String[] regionValues = { "EU", "USA" };
        String[] ageGroupValues = { "Child", "Teenager", "Adult" };
        String[] nativeValues = { "Yes", "No, immigrant", "No, second-generation" };

        int i = 0;
        for (String gender : genderValues) {
            for (String region : regionValues) {
                for (String ageGroup : ageGroupValues) {
                    for (String nativeValue : nativeValues) {
                        c.where("Region", region).where("Age-group", ageGroup).where("Gender", gender)
                                .where("Native", nativeValue).put(i, true);
                        i++;
                    }
                }
            }
        }

        String[] dimensionNames = c.getDimensionNames();
        assertEquals("[Region, Age-group, Gender, Native]", Arrays.toString(dimensionNames));

        CrosstabTextRenderer crosstabRenderer = new CrosstabTextRenderer();

        // auto-assigned axises
        assertEquals("                                                   EU                      USA \n"
                + "                                Child Teenager    Adult    Child Teenager    Adult \n"
                + "Male   Yes                          0        3        6        9       12       15 \n"
                + "No, immigrant                1        4        7       10       13       16 \n"
                + "No, second-generation        2        5        8       11       14       17 \n"
                + "Female Yes                         18       21       24       27       30       33 \n"
                + "No, immigrant               19       22       25       28       31       34 \n"
                + "No, second-generation       20       23       26       29       32       35 \n", crosstabRenderer
                .render(c).replaceAll("\"", "'"));
    }
}
