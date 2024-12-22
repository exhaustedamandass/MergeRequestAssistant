package helpers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigHelperTest {

    private static final String VALID_CONFIG_CONTENT = "GITHUB_TOKEN=test_token";
    private static final String INVALID_CONFIG_CONTENT = "INVALID_KEY=test_value";
    private Path tempConfigFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary file to simulate the configuration file
        tempConfigFile = Files.createTempFile("test-config", ".properties");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Delete the temporary file after each test
        Files.deleteIfExists(tempConfigFile);
    }

    @Test
    void testGetToken_ValidConfig() throws IOException {
        // Write valid content to the temp file
        Files.writeString(tempConfigFile, VALID_CONFIG_CONTENT);

        // Test ConfigHelper
        String token = ConfigHelper.getToken(tempConfigFile.toString());
        assertEquals("test_token", token, "The token should match the value in the config file.");
    }

    @Test
    void testGetToken_MissingKey() throws IOException {
        // Write invalid content to the temp file
        Files.writeString(tempConfigFile, INVALID_CONFIG_CONTENT);

        // Test ConfigHelper
        String token = ConfigHelper.getToken(tempConfigFile.toString());
        assertNull(token, "The token should be null if the GITHUB_TOKEN key is missing.");
    }

    @Test
    void testGetToken_FileNotFound() {
        // Test ConfigHelper with a non-existent file
        String token = ConfigHelper.getToken("nonexistent-file.properties");
        assertNull(token, "The token should be null if the file does not exist.");
    }

    @Test
    void testGetToken_FileReadError() {
        // Simulate a file that cannot be read
        File unreadableFile = new File(tempConfigFile.toString());
        unreadableFile.setReadable(false);

        try {
            String token = ConfigHelper.getToken(tempConfigFile.toString());
            assertNull(token, "The token should be null if the file cannot be read.");
        } finally {
            unreadableFile.setReadable(true); // Restore file readability
        }
    }
}
