package cli;

import apiClients.ApiClient;

import java.util.Map;

public class CommandContext {
    private final ApiClient apiClient;
    private Map<Integer, String> repositoryMap; // Map of number-to-repo

    public CommandContext(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public Map<Integer, String> getRepositoryMap() {
        return repositoryMap;
    }

    public void setRepositoryMap(Map<Integer, String> repositoryMap) {
        this.repositoryMap = repositoryMap;
    }
}


