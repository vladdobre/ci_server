import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
