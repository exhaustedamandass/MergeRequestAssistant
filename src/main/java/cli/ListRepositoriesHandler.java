package cli;

import apiClients.ApiClient;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ListRepositoriesHandler extends CommandHandler {
    @Override
    protected boolean process(CommandContext context) {
        System.out.println("Fetching repositories...");
        ApiClient apiClient = context.getApiClient();
        Map<Integer, String> repoMap = new HashMap<>(); // Map to store number-to-repo mapping

        try {
            JsonArray repos = apiClient.listRepositories();
            if (repos.size() == 0) {
                System.out.println("No repositories found.");
                return false;
            }

            // Print repositories and populate the map
            for (int i = 0; i < repos.size(); i++) {
                String repoName = repos.get(i).getAsJsonObject().get("full_name").getAsString();
                repoMap.put(i + 1, repoName); // Map numbers (1-based index) to repository names
                System.out.println((i + 1) + ". " + repoName);
            }

            // Store the map in the context for use by other handlers
            context.setRepositoryMap(repoMap);

        } catch (IOException e) {
            System.err.println("Error listing repositories: " + e.getMessage());
            return false;
        }

        return false; // Allow the next handler in the chain to execute
    }
}

