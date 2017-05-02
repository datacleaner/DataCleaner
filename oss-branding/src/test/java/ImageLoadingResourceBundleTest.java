import static org.junit.Assert.*;

import java.awt.Image;
import java.util.ResourceBundle;

import org.junit.Test;

public class ImageLoadingResourceBundleTest {

    @Test
    public void testCategoriesResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = CategoriesResourceBundle.getBundle("CategoriesResourceBundle");
        final Object object = resourceBundle.getObject("component.category.DateAndTimeCategory");
        assertTrue(object instanceof Image);
    }

    @Test
    public void testComponentTypeResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = ComponentTypeResourceBundle.getBundle("ComponentTypeResourceBundle");
        final Object object = resourceBundle.getObject("component.type.category");
        assertTrue(object instanceof Image);
    }

    @Test
    public void testComponentInstanceResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = ComponentInstanceResourceBundle.getBundle("ComponentInstanceResourceBundle");
        final Object object = resourceBundle.getObject("component.instance.ConvertToBooleanTransformer");
        assertTrue(object instanceof Image);
    }

    @Test
    public void testDatastoreTypeResourceBundleHasImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = DatastoreTypeResourceBundle.getBundle("DatastoreTypeResourceBundle");
        final Object object = resourceBundle.getObject("datastore.type.couchdb");
        assertTrue(object instanceof Image);
    }

    @Test
    public void testDatastoreTypeResourceBundleHasDataBaseImage() {
        // Only using concrete class directly so static analysis knows that it is being used.
        final ResourceBundle resourceBundle = DatastoreTypeResourceBundle.getBundle("DatastoreTypeResourceBundle");
        final Object object = resourceBundle.getObject("datastore.type.database.kettle");
        assertTrue(object instanceof Image);
    }
}