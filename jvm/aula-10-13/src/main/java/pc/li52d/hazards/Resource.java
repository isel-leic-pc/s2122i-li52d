package pc.li52d.hazards;

public class Resource {

    private static Resource instance = new Resource();

    public static Resource getInstance() {
        return instance;
    }

    private Resource() {
        //...
    }
}
