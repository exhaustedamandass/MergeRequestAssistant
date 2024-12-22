package apiClients;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dataModels.ApiResponse;
import okhttp3.*;

import java.io.IOException;

public abstract class ApiClient {
    protected final OkHttpClient client;
    protected final String token;

    public ApiClient(String token) {
        this.client = new OkHttpClient();
        this.token = token;
    }

    /**
     * Constructs the base URL for the API.
     * @return Base URL as a String.
     */
    protected abstract String getBaseUrl();

    /**
     * Creates the Authorization header value.
     * @return Authorization header value.
     */
    protected String getAuthorizationHeader() {
        return "Bearer " + token;
    }

    /**
     * Sends a GET request to the specified URL.
     * @param url The endpoint URL.
     * @return Parsed JSON response.
     * @throws IOException If the request fails.
     */
    protected ApiResponse get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(getBaseUrl() + url)
                .header("Authorization", getAuthorizationHeader())
                .build();

        try (Response response = client.newCall(request).execute()) {
            int statusCode = response.code();

            if (response.isSuccessful()) {
                JsonElement jsonElement = JsonParser.parseString(response.body().string());
                return new ApiResponse(jsonElement, statusCode);
            } else {
                return new ApiResponse(null, statusCode); // Return status code for error handling
            }
        }
    }

    /**
     * Sends a POST request to the specified URL.
     * @param url The endpoint URL.
     * @param body The JSON body as a String.
     * @return Parsed JSON response.
     * @throws IOException If the request fails.
     */
    protected JsonElement post(String url, String body) throws IOException {
        Request request = new Request.Builder()
                .url(getBaseUrl() + url)
                .header("Authorization", getAuthorizationHeader())
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP POST failed: " + response.message());
            }
            return JsonParser.parseString(response.body().string());
        }
    }

    /**
     * Sends a PUT request to the specified URL.
     * @param url The endpoint URL.
     * @param body The JSON body as a String.
     * @return Parsed JSON response.
     * @throws IOException If the request fails.
     */
    protected JsonElement put(String url, String body) throws IOException {
        Request request = new Request.Builder()
                .url(getBaseUrl() + url)
                .header("Authorization", getAuthorizationHeader())
                .put(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP PUT failed: " + response.message());
            }
            return JsonParser.parseString(response.body().string());
        }
    }

    /**
     * Fetches a list of repositories accessible by the authenticated user.
     *
     * @return A JsonArray containing repository information.
     * @throws IOException If the request fails or returns an error response.
     */
    public abstract JsonArray listRepositories() throws IOException;

    /**
     * Creates a new branch in the specified repository.
     *
     * @param repo       The repository name in the format "owner/repo".
     * @param branchName The name of the new branch to create.
     * @return true if the branch was created successfully, false otherwise.
     * @throws IOException If the request fails or returns an error response.
     */
    public abstract boolean createBranch(String repo, String branchName) throws IOException;

    /**
     * Commits a file to the specified branch in the repository.
     *
     * @param repo       The repository name in the format "owner/repo".
     * @param branchName The name of the branch to commit to.
     * @param filePath   The path of the file to commit (e.g., "path/to/file.txt").
     * @param content    The content of the file to commit.
     * @return true if the file was committed successfully, false otherwise.
     * @throws IOException If the request fails or returns an error response.
     */
    public abstract boolean commitFile(String repo, String branchName, String filePath, String content)
            throws IOException;

    /**
     * Creates a pull request in the specified repository.
     *
     * @param repo       The repository name in the format "owner/repo".
     * @param branchName The branch containing the changes to merge.
     * @param title      The title of the pull request.
     * @param body       The description or body of the pull request.
     * @return true if the pull request was created successfully, false otherwise.
     * @throws IOException If the request fails or returns an error response.
     */
    public abstract boolean createPullRequest(String repo, String branchName, String title, String body)
            throws IOException;

    /**
     * Checks if a branch with given name already exists in current repository.
     *
     * @param repo       The repository name in the format "owner/repo".
     * @param branchName The branch containing the changes to merge.
     * @return true if branch with given name already exists, false otherwise.
     * @throws IOException If the request fails or returns an error response.
     */
    public abstract boolean branchExists(String repo, String branchName) throws IOException;
}
