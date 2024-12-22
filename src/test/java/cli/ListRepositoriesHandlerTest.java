package cli;

import apiClients.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;

public class ListRepositoriesHandlerTest {

    private ListRepositoriesHandler handler;
    private CommandContext mockContext;
    private ApiClient mockApiClient;

    @BeforeEach
    public void setUp() {
        handler = new ListRepositoriesHandler();
        mockContext = Mockito.mock(CommandContext.class);
        mockApiClient = Mockito.mock(ApiClient.class);

        when(mockContext.getApiClient()).thenReturn(mockApiClient);
    }

    @Test
    public void testProcessWithRepositories() throws IOException {
        // Mock the API response with repositories
        JsonArray mockRepos = new JsonArray();

        JsonObject repo1 = new JsonObject();
        repo1.addProperty("full_name", "user/repo1");
        mockRepos.add(repo1);

        JsonObject repo2 = new JsonObject();
        repo2.addProperty("full_name", "user/repo2");
        mockRepos.add(repo2);

        when(mockApiClient.listRepositories()).thenReturn(mockRepos);

        // Execute the handler
        boolean result = handler.process(mockContext);

        // Verify that the process() method behaves as expected
        assertFalse(result); // Should return false to allow the next handler to execute

        // Capture the argument passed to setRepositoryMap
        ArgumentCaptor<Map<Integer, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockContext).setRepositoryMap(captor.capture());

        // Validate the captured repository map
        Map<Integer, String> repoMap = captor.getValue();
        assertNotNull(repoMap);
        assertEquals(2, repoMap.size());
        assertEquals("user/repo1", repoMap.get(1));
        assertEquals("user/repo2", repoMap.get(2));
    }

    @Test
    public void testProcessWithNoRepositories() throws IOException {
        // Mock the API response with no repositories
        JsonArray mockRepos = new JsonArray();
        when(mockApiClient.listRepositories()).thenReturn(mockRepos);

        // Execute the handler
        boolean result = handler.process(mockContext);

        // Verify that the process() method behaves as expected
        assertFalse(result); // Should return false
        verify(mockContext, never()).setRepositoryMap(any(Map.class)); // No map should be set
    }

    @Test
    public void testProcessWithIOException() throws IOException {
        // Mock the API throwing an IOException
        when(mockApiClient.listRepositories()).thenThrow(new IOException("API error"));

        // Execute the handler
        boolean result = handler.process(mockContext);

        // Verify that the process() method behaves as expected
        assertFalse(result); // Should return false
        verify(mockContext, never()).setRepositoryMap(any(Map.class)); // No map should be set
    }
}
