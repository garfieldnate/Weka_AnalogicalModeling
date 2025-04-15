package weka.classifiers.lazy.AM;

import java.io.InputStream;
import java.util.Properties;

public class AMVersion {
    private static final String DEFAULT_VERSION = "UNKNOWN";

    public static final String VERSION = getVersion();

    private static String getVersion() {
        Properties properties = new Properties();
        try (InputStream input = AMVersion.class.getClassLoader()
            .getResourceAsStream("weka/classifiers/lazy/AM/Description.props")) {
            if (input == null) {
                System.err.println("Description.props file not found in resources.");
                return DEFAULT_VERSION;
            }
            properties.load(input);
            return properties.getProperty("Version", DEFAULT_VERSION);
        } catch (Exception e) {
            // this is called in a static block, so any uncaught errors would prevent
            // the whole system from loading! So we must catch everything.
            System.err.println("Failed to load version from Description.props: " + e.getMessage());
            e.printStackTrace();
        }
        return DEFAULT_VERSION;
    }

    public static void main(String[] args) {
        System.out.println(getVersion());
    }
}
