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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ResourceBundle;

import org.junit.Test;

public class ColorsResourceBundleTest {
    @Test
    public void testColorsResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = ColorsResourceBundle.getBundle("ColorsResourceBundle");

        assertTrue(resourceBundle.getObject("color.orange.dark") instanceof Color);
        assertTrue(resourceBundle.getObject("color.background.alternative") instanceof Color);

        assertEquals(Color.WHITE, resourceBundle.getObject("color.brightest"));
    }
}
