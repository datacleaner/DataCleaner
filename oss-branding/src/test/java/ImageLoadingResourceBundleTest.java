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

import static org.junit.Assert.assertTrue;

import java.awt.Image;
import java.util.ResourceBundle;

import org.junit.Test;

public class ImageLoadingResourceBundleTest {

    @Test
    public void testCategoriesResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = CategoriesResourceBundle.getBundle("CategoriesResourceBundle");
        assertTrue(resourceBundle.getObject("component.category.DateAndTimeCategory") instanceof Image);
    }

    @Test
    public void testComponentTypeResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = ComponentTypeResourceBundle.getBundle("ComponentTypeResourceBundle");
        assertTrue(resourceBundle.getObject("component.type.category") instanceof Image);
    }

    @Test
    public void testComponentInstanceResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle =
                ComponentInstanceResourceBundle.getBundle("ComponentInstanceResourceBundle");
        assertTrue(resourceBundle.getObject("component.instance.ConvertToBooleanTransformer") instanceof Image);
    }

    @Test
    public void testDatastoreTypeResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = DatastoreTypeResourceBundle.getBundle("DatastoreTypeResourceBundle");

        assertTrue(resourceBundle.getObject("datastore.type.couchdb") instanceof Image);
        assertTrue(resourceBundle.getObject("datastore.type.database.kettle") instanceof Image);
    }

    @Test
    public void testDesktopUiResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = DesktopUiResourceBundle.getBundle("DesktopUiResourceBundle");

        assertTrue(resourceBundle.getObject("desktop.actions.close_bright") instanceof Image);
        assertTrue(resourceBundle.getObject("desktop.chart-types.scatter") instanceof Image);
        assertTrue(resourceBundle.getObject("desktop.widgets.output_column_visibility_visible") instanceof Image);
        assertTrue(resourceBundle.getObject("desktop.remote-icon-overlay-small") instanceof Image);
    }

    @Test
    public void testFileTypeResourceBundleBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = FileTypeResourceBundle.getBundle("FileTypeResourceBundle");
        assertTrue(resourceBundle.getObject("filetypes.new-folder") instanceof Image);
    }

    @Test
    public void testModelResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = ModelResourceBundle.getBundle("ModelResourceBundle");
        assertTrue(resourceBundle.getObject("model.progress_information") instanceof Image);
    }

    @Test
    public void testMonitorResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = MonitorResourceBundle.getBundle("MonitorResourceBundle");

        assertTrue(resourceBundle.getObject("monitor.extensions.jdbc-wizard-background") instanceof Image);
        assertTrue(resourceBundle.getObject("monitor.services.launch-datacleaner-app-icon") instanceof Image);
        assertTrue(resourceBundle.getObject("monitor.ui.images.infographic_part_evolution") instanceof Image);
        assertTrue(resourceBundle.getObject("monitor.ui.resources.copy") instanceof Image);
        assertTrue(resourceBundle.getObject("monitor.widgets.column") instanceof Image);
    }
}
