package dataModels;

public class FileDetails {
    private final String filePath;
    private final String fileContent;

    public FileDetails(String filePath, String fileContent) {
        this.filePath = filePath;
        this.fileContent = fileContent;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileContent() {
        return fileContent;
    }
}

