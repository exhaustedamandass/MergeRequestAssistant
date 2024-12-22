package apiClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dataModels.ApiResponse;
import helpers.FileHelper;

import java.io.IOException;

public class GitHubApiClient extends ApiClient {

    public GitHubApiClient(String token) {
        super(token);
    }

    @Override
    protected String getBaseUrl() {
        return "https://api.github.com";
    }

    @Override
    public JsonArray listRepositories() throws IOException {
        ApiResponse response = get("/user/repos");

        if (response.getStatusCode() >= 400) {
            throw new IOException("Failed to list repositories. HTTP status: " + response.getStatusCode());
        }

        return response.getJsonElement().getAsJsonArray();
    }

    @Override
    public boolean createBranch(String repo, String branchName) throws IOException {
        String url = String.format("/repos/%s/git/refs", repo);
        String mainBranchSha = getBranchSha(repo, "main");
        if (mainBranchSha == null) return false;

        JsonObject body = new JsonObject();
        body.addProperty("ref", "refs/heads/" + branchName);
        body.addProperty("sha", mainBranchSha);

        ApiResponse response = post(url, body.toString());
        if(!response.isSuccessful()){
            throw new IOException("Failed to create a branch. HTTP status: " + response.getStatusCode());
        }
        return true;
    }

    @Override
    public boolean commitFile(String repo, String branchName, String filePath, String content) throws IOException {
        String url = String.format("/repos/%s/contents/%s", repo, filePath);
        String encodedContent = FileHelper.encodeContent(content);

        JsonObject body = new JsonObject();
        body.addProperty("message", "Add " + filePath);
        body.addProperty("content", encodedContent);
        body.addProperty("branch", branchName);

        // Check if the file already exists
        String sha = getFileSha(repo, filePath, branchName);
        if (sha != null) {
            body.addProperty("sha", sha); // Add the SHA to update the file
        }

        ApiResponse response = put(url, body.toString());
        if(!response.isSuccessful()){
            throw new IOException("Failed to commit a file. HTTP status: " + response.getStatusCode());
        }

        return true;
    }

    @Override
    public boolean createPullRequest(String repo, String branchName, String title, String body) throws IOException {
        String url = String.format("/repos/%s/pulls", repo);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("title", title);
        requestBody.addProperty("body", body);
        requestBody.addProperty("head", branchName);
        requestBody.addProperty("base", "main");

        ApiResponse response = post(url, requestBody.toString());
        if(!response.isSuccessful()){
            throw new IOException("Failed to create a pull request. HTTP status: " + response.getStatusCode());
        }

        return true;
    }

    @Override
    public boolean branchExists(String repo, String branchName) throws IOException {
        String url = String.format("/repos/%s/git/ref/heads/%s", repo, branchName);

        ApiResponse response = get(url); // Call the updated `get` method

        if (response.getStatusCode() == 404) {
            return false; // Branch does not exist
        }

        if (response.getStatusCode() >= 400) {
            throw new IOException("Failed to check branch existence. HTTP status: " + response.getStatusCode());
        }

        return true; // Branch exists
    }

    private String getBranchSha(String repo, String branch) throws IOException {
        String url = String.format("/repos/%s/git/ref/heads/%s", repo, branch);
        ApiResponse response = get(url);

        if (response.getStatusCode() == 404) {
            throw new IOException("Branch not found: " + branch);
        }

        if (response.getStatusCode() >= 400) {
            throw new IOException("Failed to retrieve branch SHA. HTTP status: " + response.getStatusCode());
        }

        return response.getJsonElement()
                .getAsJsonObject()
                .getAsJsonObject("object")
                .get("sha")
                .getAsString();
    }

    private String getFileSha(String repo, String filePath, String branchName) throws IOException {
        String url = String.format("/repos/%s/contents/%s?ref=%s", repo, filePath, branchName);

        try {
            ApiResponse response = get(url); // Use the `get` method to fetch file info
            if (response.getStatusCode() == 404) {
                return null; // File does not exist
            }

            if (response.getStatusCode() >= 400) {
                throw new IOException("Failed to fetch file SHA. HTTP status: " + response.getStatusCode());
            }

            JsonObject json = response.getJsonElement().getAsJsonObject();
            return json.get("sha").getAsString();
        } catch (IOException e) {
            if (e.getMessage().contains("404")) {
                return null; // File does not exist
            }
            throw e;
        }
    }
}