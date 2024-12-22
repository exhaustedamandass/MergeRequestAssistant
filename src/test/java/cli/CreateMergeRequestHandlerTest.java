package cli;

import apiClients.ApiClient;
import dataModels.FileDetails;
import dataModels.MergeRequestParameters;
import interfaces.RepositorySelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CreateMergeRequestHandlerTest {

    private CommandContext mockContext;
    private ApiClient mockApiClient;
    private MergeRequestParameters mockParameters;
    private CreateMergeRequestHandler handler;
    private RepositorySelector mockRepositorySelector;

    @BeforeEach
    void setUp() {
        mockContext = mock(CommandContext.class);
        mockApiClient = mock(ApiClient.class);
        mockParameters = mock(MergeRequestParameters.class);
        mockRepositorySelector = mock(RepositorySelector.class);

        when(mockContext.getApiClient()).thenReturn(mockApiClient);

        handler = new CreateMergeRequestHandler(mockParameters, mockRepositorySelector);
    }

    @Test
    void process_NoRepositories_ReturnsFalse() {
        when(mockContext.getRepositoryMap()).thenReturn(null);

        boolean result = handler.process(mockContext);

        assertFalse(result);
        verify(mockContext, never()).getApiClient();
    }

    @Test
    void process_RepositorySelectionFails_ReturnsFalse() {
        Map<Integer, String> repoMap = new HashMap<>();
        repoMap.put(1, "repo1");
        when(mockContext.getRepositoryMap()).thenReturn(repoMap);

        when(mockRepositorySelector.matchRepositoryIndexToName(repoMap)).thenReturn(null);

        boolean result = handler.process(mockContext);

        assertFalse(result);
    }

    @Test
    void process_CreateBranchFails_ReturnsFalse() throws IOException {
        Map<Integer, String> repoMap = new HashMap<>();
        repoMap.put(1, "repo1");
        String repo = "repo1";

        when(mockContext.getRepositoryMap()).thenReturn(repoMap);
        when(mockRepositorySelector.matchRepositoryIndexToName(repoMap)).thenReturn(repo);

        when(mockParameters.getBranchName()).thenReturn("feature-branch");
        when(mockApiClient.branchExists(repo, "feature-branch")).thenReturn(false);
        when(mockApiClient.createBranch(repo, "feature-branch")).thenReturn(false);

        boolean result = handler.process(mockContext);

        assertFalse(result);
        verify(mockApiClient).createBranch(repo, "feature-branch");
    }

    @Test
    void process_CommitFilesFails_ReturnsFalse() throws IOException {
        Map<Integer, String> repoMap = new HashMap<>();
        repoMap.put(1, "repo1");
        String repo = "repo1";

        when(mockContext.getRepositoryMap()).thenReturn(repoMap);
        when(mockRepositorySelector.matchRepositoryIndexToName(repoMap)).thenReturn(repo);

        when(mockParameters.getBranchName()).thenReturn("feature-branch");
        when(mockApiClient.branchExists(repo, "feature-branch")).thenReturn(false);
        when(mockApiClient.createBranch(repo, "feature-branch")).thenReturn(true);

        FileDetails mockFile = mock(FileDetails.class);
        when(mockParameters.getFiles()).thenReturn(List.of(mockFile));
        when(mockFile.getFilePath()).thenReturn("/path/to/file.txt");
        when(mockApiClient.commitFile(repo, "feature-branch", "/path/to/file.txt", mockFile.getFileContent())).thenReturn(false);

        boolean result = handler.process(mockContext);

        assertFalse(result);
        verify(mockApiClient).commitFile(repo, "feature-branch", "/path/to/file.txt", mockFile.getFileContent());
    }

    @Test
    void process_CreatePullRequestFails_ReturnsFalse() throws IOException {
        Map<Integer, String> repoMap = new HashMap<>();
        repoMap.put(1, "repo1");
        String repo = "repo1";

        when(mockContext.getRepositoryMap()).thenReturn(repoMap);
        when(mockRepositorySelector.matchRepositoryIndexToName(repoMap)).thenReturn(repo);

        when(mockParameters.getBranchName()).thenReturn("feature-branch");
        when(mockApiClient.branchExists(repo, "feature-branch")).thenReturn(false);
        when(mockApiClient.createBranch(repo, "feature-branch")).thenReturn(true);

        FileDetails mockFile = mock(FileDetails.class);
        when(mockParameters.getFiles()).thenReturn(List.of(mockFile));
        when(mockFile.getFilePath()).thenReturn("/path/to/file.txt");
        when(mockApiClient.commitFile(repo, "feature-branch", "/path/to/file.txt", mockFile.getFileContent())).thenReturn(true);

        when(mockParameters.getPullRequestTitle()).thenReturn("Add feature");
        when(mockParameters.getPullRequestBody()).thenReturn("This is a test pull request.");
        when(mockApiClient.createPullRequest(repo, "feature-branch", "Add feature", "This is a test pull request.")).thenReturn(false);

        boolean result = handler.process(mockContext);

        assertFalse(result);
        verify(mockApiClient).createPullRequest(repo, "feature-branch", "Add feature", "This is a test pull request.");
    }

    @Test
    void process_Success_ReturnsTrue() throws IOException {
        Map<Integer, String> repoMap = new HashMap<>();
        repoMap.put(1, "repo1");
        String repo = "repo1";

        when(mockContext.getRepositoryMap()).thenReturn(repoMap);
        when(mockRepositorySelector.matchRepositoryIndexToName(repoMap)).thenReturn(repo);

        when(mockParameters.getBranchName()).thenReturn("feature-branch");
        when(mockApiClient.branchExists(repo, "feature-branch")).thenReturn(false);
        when(mockApiClient.createBranch(repo, "feature-branch")).thenReturn(true);

        FileDetails mockFile = mock(FileDetails.class);
        when(mockParameters.getFiles()).thenReturn(List.of(mockFile));
        when(mockFile.getFilePath()).thenReturn("/path/to/file.txt");
        when(mockApiClient.commitFile(repo, "feature-branch", "/path/to/file.txt", mockFile.getFileContent())).thenReturn(true);

        when(mockParameters.getPullRequestTitle()).thenReturn("Add feature");
        when(mockParameters.getPullRequestBody()).thenReturn("This is a test pull request.");
        when(mockApiClient.createPullRequest(repo, "feature-branch", "Add feature", "This is a test pull request.")).thenReturn(true);

        boolean result = handler.process(mockContext);

        assertTrue(result);
    }
}
