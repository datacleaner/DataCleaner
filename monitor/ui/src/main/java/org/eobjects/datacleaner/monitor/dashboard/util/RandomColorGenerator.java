/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.monitor.dashboard.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomColorGenerator {

    public static String getRandomColor() {

        String hex1 = getRandomHex();
        String hex2 = getRandomHex();
        String hex3 = getRandomHex();
        String hex4 = getRandomHex();
        String hex5 = getRandomHex();
        String hex6 = getRandomHex();

        String color = "#" + hex1 + hex2 + hex3 + hex4 + hex5 + hex6;

        return color;
    }

    public static List<String> getRandomColors(int numberOfColors) {
        List<String> randomColorsList = new ArrayList<String>();
        for (int i = 0; i < numberOfColors; i++) {
            randomColorsList.add(getRandomColor());
        }
        return randomColorsList;
    }

    private static String getRandomHex() {
        String[] hex = new String[] { "0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "A", "B", "C", "D", "E", "F" };
        Random random = new Random();
        int randomNum = random.nextInt(hex.length);
        String sHex = hex[randomNum];
        return sHex;
    }

}