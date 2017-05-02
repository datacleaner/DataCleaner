import java.io.IOException;

public class ComponentTypeResourceBundle extends ImageLoadingPropertyResourceBundle {
    public ComponentTypeResourceBundle() throws IOException {
        super(ComponentTypeResourceBundle.class.getClassLoader().getResource("component-types.properties"));
    }
}
