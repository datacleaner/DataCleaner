import java.io.IOException;
import java.net.URL;

public class CategoriesResourceBundle extends ImageLoadingPropertyResourceBundle {
    public CategoriesResourceBundle() throws IOException {
        super(CategoriesResourceBundle.class.getClassLoader().getResource("categories.properties"));
    }
}
