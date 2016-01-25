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
package org.datacleaner.widgets.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.sql.Clob;

import javax.sql.rowset.serial.SerialClob;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.datacleaner.widgets.DCLabel;
import org.junit.Test;

public class DCTableCellRendererTest {

    @Test
    public void testRenderClob() throws Exception {
        final Clob clob = new SerialClob("foo bar baz".toCharArray());

        // assert that the toString() method is not "foo bar baz" because that
        // could be a potential implementation detail that would disturb the
        // validity of the below assertions.
        assertFalse("foo bar baz".equals(clob.toString()));

        final DCTableCellRenderer renderer = new DCTableCellRenderer(null);
        final Component component = renderer.getTableCellRendererComponent(new JTable(), clob, false, false, 1, 1);
        assertTrue(component instanceof JLabel);

        final JLabel label = (JLabel) component;
        assertEquals("foo bar baz", label.getText());
    }

    @Test
    public void testRenderIcon() throws Exception {
        final Image image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        final Icon icon = new ImageIcon(image);

        final DCTableCellRenderer renderer = new DCTableCellRenderer(null);
        final Component component = renderer.getTableCellRendererComponent(new JTable(), icon, false, false, 1, 1);
        assertTrue(component instanceof JLabel);

        final JLabel label = (JLabel) component;
        assertEquals(icon, label.getIcon());
    }

    @Test
    public void testRenderJComponent() throws Exception {
        final JComponent inputComponent = DCLabel.dark("hello world");

        final DCTableCellRenderer renderer = new DCTableCellRenderer(null);
        final Component component = renderer.getTableCellRendererComponent(new JTable(), inputComponent, false, false,
                1, 1);
        assertTrue(component instanceof JLabel);

        assertSame(inputComponent, component);
    }
}
