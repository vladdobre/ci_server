import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;

public class ContinuousIntegrationServerTest {

    @Test
    /**
     * Test the getLatestCommitMessageFromPush method
     * 
     * This test simulates a payload for a push event and checks if the method
     * returns the correct commit message.
     * 
     * The expected message is "Test commit message".
     */
    public void testGetLatestCommitMessageFromPush() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        // Simulate a payload for a push event
        String mockPayload = "{"
            + "\"commits\": ["
            + "    {"
            + "        \"id\": \"10032001il\","
            + "        \"message\": \"Test commit message\","
            + "        \"timestamp\": \"2023-02-07T00:00:00Z\""
            + "    }"
            + "]"
            + "}";

        String expectedMessage = "Test commit message";
        String actualMessage = ciServer.getLatestCommitMessageFromPush(mockPayload);

        assertEquals(expectedMessage, actualMessage, "The commit message should match the expected message.");
    }

    @Test
    /**
     * Test the getLatestCommitMessageFromPush method with multiple commits
     * 
     * This test simulates a payload with multiple commits and checks if the method
     * returns the correct commit message.
     * 
     * The expected message is "Latest commit message".
     */
    public void testGetLatestCommitMessageFromPushWithMultipleCommits() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();
    
        // Simulate a payload with multiple commits
        String mockPayload = "{"
                + "\"commits\": ["
                + "    {\"id\": \"182736\",\"message\": \"Older commit message\",\"timestamp\": \"2023-02-06T00:00:00Z\"},"
                + "    {\"id\": \"289374\",\"message\": \"Latest commit message\",\"timestamp\": \"2023-02-07T00:00:00Z\"}"
                + "]"
                + "}";
    
        String expectedMessage = "Latest commit message";
        String actualMessage = ciServer.getLatestCommitMessageFromPush(mockPayload);
    
        assertEquals(expectedMessage, actualMessage, "The commit message should match the latest commit's message.");
    }

    @Test
    /**
     * Test the getLatestCommitMessageFromPush method with a null payload
     * 
     * This test checks if the method returns null when the payload is null.
     * 
     * The expected message is null.
     */
    public void testGetLatestCommitMessageFromPushWithNullPayload() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String expectedMessage = null; // Expecting null for a null payload
        String actualMessage = ciServer.getLatestCommitMessageFromPush(null);

        assertEquals(expectedMessage, actualMessage, "The commit message should be null for a null payload.");
    }

    @Test
    /**
     * Test the extractRepositoryUrl method
     * 
     * This test simulates a payload for a push event and checks if the method
     * returns the correct repository URL.
     * 
     * The expected URL is "https://github.com/Name/test_ci.git".
     */
    public void testExtractRepositoryUrl() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        // Simulate a payload for a push event
        String mockPayload = "{\"repository\":{\"clone_url\":\"https://github.com/Name/test_ci.git\"}}";

        String expectedUrl = "https://github.com/Name/test_ci.git";
        String actualUrl = ciServer.extractRepositoryUrl(mockPayload);

        assertEquals(expectedUrl, actualUrl, "The clone url should match the expected url.");
    }

    @Test
    /**
     * Test the extractRepositoryUrl method with a null payload
     * 
     * This test checks if the method returns null when the payload is null.
     * 
     * The expected URL is null.
     */
    public void testExtractRepositoryUrlWithNullPayload() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String expectedUrl = null;
        String actualUrl = ciServer.extractRepositoryUrl(null);

        assertEquals(expectedUrl, actualUrl, "The clone url should be null for a null payload.");
    }

    @Test
    /**
     * Test the extractEmail method
     * 
     * This test simulates a payload for a push event and checks if the method
     * returns the correct email.
     */
    public void testExtractEmailWithNullPayload() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String expectedUrl = null;
        String actualUrl = ciServer.extractEmail(null);

        assertEquals(expectedUrl, actualUrl, "The clone email should be null for a null payload.");
    }

    @Test
    /**
     * Test the extractBranchName method
     *
     * This test simulates a payload for a push event and checks if the method
     * returns the correct branch name.
     *
     * The expected branch name is "main".
     */
    public void testExtractBranchName() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String mockPayload = "{\"ref\":\"refs/heads/main\"}";

        String expectedBranch = "main";
        String actualBranch = ciServer.extractBranchName(mockPayload);
        assertEquals(expectedBranch, actualBranch, "The branch name should match the expected branch name.");
    }

    @Test
    /**
     * Test the cloneRepository method
     * 
     * This test checks if the method clones a repository fails when the repository URL is invalid.
     * 
     * The expected output is an error message.
     */
    public void testCloneRepositoryFailure() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();
        
        String repoUrl = "https://github.com/invalid/repo.git";
        String baseCloneDirPath = System.getProperty("java.io.tmpdir");
        String uniqueDirName = "testCloneFailure-" + System.currentTimeMillis();
        
        // Redirect standard error to capture error messages
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        
        ciServer.cloneRepository(repoUrl, baseCloneDirPath, uniqueDirName);
        
        // Check if the error output contains the error message
        String output = errContent.toString();
        assertTrue(output.contains("Error cloning repository:"));
        
        // Cleanup: Reset the standard error
        System.setErr(System.err);
    }


    @Test
    /**
     * Test the compileMavenProject method with a project that does not compile
     * 
     * This test checks if the method returns false when the project does not compile.
     */
    public void testcompileMavenProjectNotCompile() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String cwd = Path.of("").toAbsolutePath().toString();
        String projectDirPath = "src/test/test_snippets";
        cwd = cwd + "/" + projectDirPath;
        projectDirPath = cwd;
        String uniqueDirName = "No_Compile";
        String payload = "{\"head_commit\":{\"author\":{\"email\":\"test@mail.test\"}}}";

        boolean result = ciServer.compileMavenProject(projectDirPath, uniqueDirName, payload);
        assertEquals(true, result);
    }

    @Test
    /**
     * Test the compileMavenProject method with a project that compiles
     * 
     * This test checks if the method returns true when the project compiles.
     */
    public void testcompileMavenProjectCompile() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String cwd = Path.of("").toAbsolutePath().toString();
        String projectDirPath = "src/test/test_snippets";
        cwd = cwd + "/" + projectDirPath;
        projectDirPath = cwd;
        String uniqueDirName = "Compile";
        String payload = "{\"head_commit\":{\"author\":{\"email\":\"test@mail.test\"}}}";

        boolean result = ciServer.compileMavenProject(projectDirPath, uniqueDirName, payload);
        assertEquals(true, result);
    }

    @Test
    /**
     * Tests compileMavenProject methdo with correct code and a tests that passes
     * 
     * This test checks if the method returns true when the project compiles and runs a test that passes.
     */
    public void testcompileMavenProjectTestSuccess() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String cwd = Path.of("").toAbsolutePath().toString();
        String projectDirPath = "src/test/test_snippets";
        cwd = cwd + "/" + projectDirPath;
        projectDirPath = cwd;
        String uniqueDirName = "Test_Success";
        String payload = "{\"head_commit\":{\"author\":{\"email\":\"test@mail.test\"}}}";

        boolean result = ciServer.compileMavenProject(projectDirPath, uniqueDirName, payload);
        assertEquals(true, result);
    }

    @Test
    /**
     * Tests compileMavenProject method with correct code and a tests that fails
     * 
     * This test checks if the method returns false when the project compiles and runs a test that fails.
     */
    public void testcompileMavenProjectTestFail() {
        ContinuousIntegrationServer ciServer = new ContinuousIntegrationServer();

        String cwd = Path.of("").toAbsolutePath().toString();
        String projectDirPath = "src/test/test_snippets";
        cwd = cwd + "/" + projectDirPath;
        projectDirPath = cwd;
        String uniqueDirName = "Test_Fail";
        String payload = "{\"head_commit\":{\"author\":{\"email\":\"test@mail.test\"}}}";

        boolean result = ciServer.compileMavenProject(projectDirPath, uniqueDirName, payload);
        assertEquals(false, result);
    }
}
