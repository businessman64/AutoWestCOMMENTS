package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "config/config.properties";
    private static final Properties properties = loadProperties();

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
