import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;
import java.util.Scanner;

public class GitHubMergeRequestAssistant {

    // Base API URLs
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String USER_REPOS_URL = GITHUB_API_BASE_URL + "/user/repos";
    private static final String GIT_REF_URL_TEMPLATE = GITHUB_API_BASE_URL + "/repos/%s/git/refs";
    private static final String GIT_REF_BRANCH_URL_TEMPLATE = GITHUB_API_BASE_URL + "/repos/%s/git/ref/heads/%s";
    private static final String CONTENTS_URL_TEMPLATE = GITHUB_API_BASE_URL + "/repos/%s/contents/%s";
    private static final String PULLS_URL_TEMPLATE = GITHUB_API_BASE_URL + "/repos/%s/pulls";

    // HTTP Headers
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_BEARER_TEMPLATE = "Bearer %s";

    // Constants for pull request
    private static final String DEFAULT_BRANCH = "main";
    private static final String NEW_BRANCH = "add-hello-file";
    private static final String COMMIT_MESSAGE = "Add Hello.txt";
    private static final String PULL_REQUEST_TITLE = "Add Hello.txt";
    private static final String PULL_REQUEST_BODY = "Adds a Hello.txt file with 'Hello world' content.";
    private static final String FILE_PATH = "Hello.txt";
    private static final String FILE_CONTENT = "Hello world";

    private final OkHttpClient client;
    private final String token;

    public GitHubMergeRequestAssistant(String token) {
        this.client = new OkHttpClient();
        this.token = token;
    }

    public static void main(String[] args) {
        // Load the token from the config file
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(new File("config.properties")));
        } catch (IOException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            return;
        }

        String token = config.getProperty("GITHUB_TOKEN");
        if (token == null || token.isEmpty()) {
            System.err.println("GitHub token not found in configuration file.");
            return;
        }

        GitHubMergeRequestAssistant assistant = new GitHubMergeRequestAssistant(token);

        // Fetch repositories
        try {
            JsonArray repositories = assistant.listRepositories();
            if (repositories.size() == 0) {
                System.out.println("No repositories found.");
                return;
            }

            System.out.println("Available repositories:");
            for (int i = 0; i < repositories.size(); i++) {
                System.out.println((i + 1) + ". " + repositories.get(i).getAsJsonObject().get("full_name").getAsString());
            }

            // Select repository
            System.out.print("Select a repository (1-" + repositories.size() + "): ");
            Scanner scanner = new Scanner(System.in);
            int selectedIndex = scanner.nextInt() - 1;
            if (selectedIndex < 0 || selectedIndex >= repositories.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            String repo = repositories.get(selectedIndex).getAsJsonObject().get("full_name").getAsString();

            // Create branch, commit file, and create pull request
            System.out.println("Creating branch...");
            if (assistant.createBranch(repo, NEW_BRANCH) &&
                    assistant.commitFile(repo, NEW_BRANCH, FILE_PATH, FILE_CONTENT)) {
                System.out.println("Creating pull request...");
                if (assistant.createPullRequest(repo, NEW_BRANCH, PULL_REQUEST_TITLE, PULL_REQUEST_BODY)) {
                    System.out.println("Pull request created successfully!");
                } else {
                    System.out.println("Failed to create pull request.");
                }
            } else {
                System.out.println("Failed to commit file.");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public JsonArray listRepositories() throws IOException {
        Request request = new Request.Builder()
                .url(USER_REPOS_URL)
                .header(HEADER_AUTHORIZATION, String.format(HEADER_BEARER_TEMPLATE, token))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error fetching repositories: " + response.message());
            }

            return JsonParser.parseString(response.body().string()).getAsJsonArray();
        }
    }

    public boolean createBranch(String repo, String branchName) throws IOException {
        String mainBranchSha = getBranchSha(repo, DEFAULT_BRANCH);
        if (mainBranchSha == null) return false;

        JsonObject body = new JsonObject();
        body.addProperty("ref", "refs/heads/" + branchName);
        body.addProperty("sha", mainBranchSha);

        Request request = new Request.Builder()
                .url(String.format(GIT_REF_URL_TEMPLATE, repo))
                .header(HEADER_AUTHORIZATION, String.format(HEADER_BEARER_TEMPLATE, token))
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    public boolean commitFile(String repo, String branchName, String filePath, String content) throws IOException {
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());

        JsonObject body = new JsonObject();
        body.addProperty("message", COMMIT_MESSAGE);
        body.addProperty("content", encodedContent);
        body.addProperty("branch", branchName);

        Request request = new Request.Builder()
                .url(String.format(CONTENTS_URL_TEMPLATE, repo, filePath))
                .header(HEADER_AUTHORIZATION, String.format(HEADER_BEARER_TEMPLATE, token))
                .put(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    public boolean createPullRequest(String repo, String branchName, String title, String body) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("title", title);
        requestBody.addProperty("body", body);
        requestBody.addProperty("head", branchName);
        requestBody.addProperty("base", DEFAULT_BRANCH);

        Request request = new Request.Builder()
                .url(String.format(PULLS_URL_TEMPLATE, repo))
                .header(HEADER_AUTHORIZATION, String.format(HEADER_BEARER_TEMPLATE, token))
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    private String getBranchSha(String repo, String branch) throws IOException {
        Request request = new Request.Builder()
                .url(String.format(GIT_REF_BRANCH_URL_TEMPLATE, repo, branch))
                .header(HEADER_AUTHORIZATION, String.format(HEADER_BEARER_TEMPLATE, token))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }

            return JsonParser.parseString(response.body().string())
                    .getAsJsonObject()
                    .getAsJsonObject("object")
                    .get("sha").getAsString();
        }
    }
}

