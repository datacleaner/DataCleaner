import java.io.IOException;

public class DatastoreTypeResourceBundle extends ImageLoadingPropertyResourceBundle {
    public DatastoreTypeResourceBundle() throws IOException {
        super(DatastoreTypeResourceBundle.class.getClassLoader().getResource("datastore-types.properties"));
    }
}
