import java.io.IOException;

public class ComponentInstanceResourceBundle extends ImageLoadingPropertyResourceBundle {
    public ComponentInstanceResourceBundle() throws IOException {
        super(ComponentInstanceResourceBundle.class.getClassLoader().getResource("components.properties"));
    }
}
