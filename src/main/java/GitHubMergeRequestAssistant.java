import apiClients.ApiClient;
import apiClients.GitHubApiClient;
import cli.CommandHandler;
import cli.CreateMergeRequestHandler;
import cli.ListRepositoriesHandler;
import cli.CommandContext;
import dataModels.FileDetails;
import dataModels.MergeRequestParameters;
import helpers.ConfigHelper;

import java.util.Collections;

public class GitHubMergeRequestAssistant {
    private final ApiClient apiClient;
    private final CommandHandler cliHandler;

    // Constants for branch name, file details, and pull request information
    private static final String BRANCH_NAME = "add-hello-file";
    private static final String FILE_PATH = "Hello.txt";
    private static final String FILE_CONTENT = "Hello world";
    private static final String PULL_REQUEST_TITLE = "Add Hello.txt";
    private static final String PULL_REQUEST_BODY = "Adds a single Hello.txt file with 'Hello world' content.";
    private static final String CONFIG_FILE = "config.properties";
    private static final String TOKEN_ERROR_MESSAGE = "GitHub token not found in configuration file.";
    private static final String WELCOME_MESSAGE = "Welcome to GitHub Merge Request Assistant!";

    public GitHubMergeRequestAssistant(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.cliHandler = initializeCommandHandlers();
    }

    public static void main(String[] args) {
        // Load the token
        String token = ConfigHelper.getToken(CONFIG_FILE);
        if (token == null || token.isEmpty()) {
            System.err.println(TOKEN_ERROR_MESSAGE);
            return;
        }

        // Use GitHubApiClient (can be swapped for GitLabApiClient, etc.)
        ApiClient apiClient = new GitHubApiClient(token);
        GitHubMergeRequestAssistant assistant = new GitHubMergeRequestAssistant(apiClient);

        assistant.run();
    }

    public void run() {
        System.out.println(WELCOME_MESSAGE);

        // Start CLI interaction
        cliHandler.handle(new CommandContext(apiClient));
    }

    private CommandHandler initializeCommandHandlers() {
        CommandHandler listReposHandler = new ListRepositoriesHandler();
        CommandHandler createMRHandler = createMergeRequestHandler();

        listReposHandler.setNext(createMRHandler);
        return listReposHandler;
    }

    private CommandHandler createMergeRequestHandler() {
        // Create parameters for the single file to be added in the merge request
        MergeRequestParameters parameters = new MergeRequestParameters(
                BRANCH_NAME, // Branch name
                Collections.singletonList( // Single file
                        new FileDetails(FILE_PATH, FILE_CONTENT) // File path and content
                ),
                PULL_REQUEST_TITLE, // Pull request title
                PULL_REQUEST_BODY // Pull request body
        );

        return new CreateMergeRequestHandler(parameters);
    }
}
