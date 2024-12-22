package dataModels;

import java.util.List;

public class MergeRequestParameters {
    private final String branchName;
    private final List<FileDetails> files;
    private final String pullRequestTitle;
    private final String pullRequestBody;

    public MergeRequestParameters(String branchName, List<FileDetails> files, String pullRequestTitle, String pullRequestBody) {
        this.branchName = branchName;
        this.files = files;
        this.pullRequestTitle = pullRequestTitle;
        this.pullRequestBody = pullRequestBody;
    }

    public String getBranchName() {
        return branchName;
    }

    public List<FileDetails> getFiles() {
        return files;
    }

    public String getPullRequestTitle() {
        return pullRequestTitle;
    }

    public String getPullRequestBody() {
        return pullRequestBody;
    }
}

