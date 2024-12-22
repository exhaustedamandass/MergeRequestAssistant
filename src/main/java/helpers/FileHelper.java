package helpers;

import java.util.Base64;

public class FileHelper {
    public static String encodeContent(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }
}

