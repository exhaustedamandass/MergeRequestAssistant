package helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigHelper {
    public static String getToken(String filePath) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filePath));
            return properties.getProperty("GITHUB_TOKEN");
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            return null;
        }
    }
}

