package cli;

import apiClients.ApiClient;
import dataModels.FileDetails;
import dataModels.MergeRequestParameters;
import helpers.InputHelper;

import java.io.IOException;
import java.util.Map;

public class CreateMergeRequestHandler extends CommandHandler {
    private final MergeRequestParameters parameters;

    public CreateMergeRequestHandler(MergeRequestParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected boolean process(CommandContext context) {
        Map<Integer, String> repoMap = context.getRepositoryMap();

        if (repoMap == null || repoMap.isEmpty()) {
            System.err.println("No repositories available. Please list repositories first.");
            return false;
        }

        String repo = InputHelper.matchRepositoryIndexToName(repoMap);
        if (repo == null) {
            return false;
        }

        try {
            ApiClient apiClient = context.getApiClient();
            String branchName = generateUniqueBranchName(apiClient, repo, parameters.getBranchName());

            if (!createBranch(apiClient, repo, branchName)) {
                return false;
            }

            if (!commitFiles(apiClient, repo, branchName)) {
                return false;
            }

            return createPullRequest(apiClient, repo, branchName);
        } catch (IOException e) {
            System.err.println("Error during merge request process: " + e.getMessage());
            return false;
        }
    }

    private String generateUniqueBranchName(ApiClient apiClient, String repo, String baseBranchName) throws IOException {
        String branchName = baseBranchName;
        int counter = 1;

        while (apiClient.branchExists(repo, branchName)) {
            branchName = baseBranchName + "-" + counter++;
            System.out.println("Branch name already exists. Trying: " + branchName);
        }

        return branchName;
    }

    private boolean createBranch(ApiClient apiClient, String repo, String branchName) {
        System.out.println("Creating new branch: " + branchName);
        try {
            if (!apiClient.createBranch(repo, branchName)) {
                System.err.println("Failed to create branch.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error creating branch: " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean commitFiles(ApiClient apiClient, String repo, String branchName) {
        System.out.println("Adding files to branch: " + branchName);
        for (FileDetails file : parameters.getFiles()) {
            try {
                if (!apiClient.commitFile(repo, branchName, file.getFilePath(), file.getFileContent())) {
                    System.err.println("Failed to commit file: " + file.getFilePath());
                    return false;
                }
            } catch (IOException e) {
                System.err.println("Error committing file " + file.getFilePath() + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean createPullRequest(ApiClient apiClient, String repo, String branchName) {
        System.out.println("Creating pull request...");
        try {
            if (apiClient.createPullRequest(repo, branchName, parameters.getPullRequestTitle(), parameters.getPullRequestBody())) {
                System.out.println("Pull request created successfully!");
                return true;
            } else {
                System.err.println("Failed to create pull request.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error creating pull request: " + e.getMessage());
            return false;
        }
    }
}