// No package declaration here

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File; 

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
// import org.eclipse.jetty.util.Callback;

/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class ContinuousIntegrationServer extends AbstractHandler
{

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
        throws IOException{
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println("Target: " + target);

        // Determine the event type from the X-GitHub-Event header
        String eventType = request.getHeader("X-GitHub-Event");
        System.out.println("Received event: " + eventType);


        // Read the JSON payload
        StringBuilder payloadBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                payloadBuilder.append(line);
            }
        }
        String payload = payloadBuilder.toString();


        //For the Issue #2, we need to implement the  Webhook listener
        // so this part get listened to the push and pull_request events
        // and print the last commit in a push of the action in case of pull_request.
        if ("push".equals(eventType)) {
            handlePushEvent(payload);
        } else if ("pull_request".equals(eventType)) {
            handlePullRequestEvent(payload);
        } else {
            System.out.println("Unhandled event type: " + eventType);
        }

        // System.out.println("Payload: ");
        // Map<String, Object> payload = request_to_map(request.getParameter("payload"));
        // for(String key : payload.keySet()){
        //     System.out.println(payload.get(key));
        // }

        response.getWriter().println("CI job done");
    }


    /**
     * This function transforms a string in json format into a String,Object map.
     * @param request - Request payload from webhook. It is in the JSON format (but json isn't selected in the Webhook options!!)
     * @return A mapping String,Object of the json payload
     */
    private Map<String, Object> request_to_map(String request){

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(request, mapType);
    }

    /**
     * This function extracts the latest commit message from the push event payload.
     * It uses the Gson library to parse the JSON payload and extract the commit message.
     * If no commits are found, it returns null.
     * 
     * @param payload - The JSON payload from the push event
     * @return The latest commit message
     */
    public String getLatestCommitMessageFromPush(String payload) {
        Gson gson = new Gson();
        Map<String, Object> payloadMap = gson.fromJson(payload, new TypeToken<HashMap<String, Object>>(){}.getType());
        List<Map<String, Object>> commits = (List<Map<String, Object>>) payloadMap.get("commits");
        if (commits != null && !commits.isEmpty()) {
            Map<String, Object> latestCommit = commits.get(commits.size() - 1);
            return (String) latestCommit.get("message");
        }
        return null; // Return null if no commits found
    }

    /**
     * Clones a Git repository to a specified directory.
     * The repository is cloned to a unique directory name generated from the commit hash and the current time.
     * 
     * @param repoUrl the URL of the repository to clone.
     * @param baseCloneDirPath the base path to the directory where the repository will be cloned.
     * @param commitHash the hash of the commit to clone.
     */
    public void cloneRepository(String repoUrl, String baseCloneDirPath, String commitHash) {
        // Generate a unique directory name
        String uniqueDirName = commitHash + "_" + System.currentTimeMillis();
        File cloneDir = new File(baseCloneDirPath, uniqueDirName);
        if (!cloneDir.mkdirs()) {
            System.err.println("Failed to create directory for clone: " + cloneDir.getPath());
            return;
        }
        
        try {
            System.out.println("Cloning repository from " + repoUrl + " to " + cloneDir.getPath());
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(cloneDir)
                .call();
            System.out.println("Repository cloned successfully.");
        } catch (GitAPIException e) {
            System.err.println("Error cloning repository: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Compiles the Maven project and runs tests.
     *
     * @param projectDirPath the path to the directory where the Maven project is located.
     */
    public void compileMavenProject(String projectDirPath) {
        try {
            // Define the command to run mvn clean install
            // [TODO]: Update the command to use the correct path to the mvn executable
            List<String> command = Arrays.asList("C:\\Program Files\\Maven\\apache-maven-3.9.6\\bin\\mvn.cmd", "clean", "install");
            
            // Create a process builder to execute the command in the project directory
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(projectDirPath)); // Set the working directory
            
            // Inherit IO to display output in the console
            processBuilder.inheritIO();
            
            // Start the process
            Process process = processBuilder.start();
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            // Check the exit code to determine if the build was successful
            if (exitCode == 0) {
                System.out.println("Maven project compiled successfully.");
            } else {
                System.err.println("Maven project compilation failed.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error compiling Maven project: " + e.getMessage());
            e.printStackTrace();
        }
    }    
    

     /**
     * Extracts the repository clone URL from the webhook payload.
     * This function uses the Gson library to parse the JSON payload and extract the repository URL.
     * If the repository URL cannot be extracted, it returns null.
     *
     * @param payload the JSON payload received from the webhook.
     * @return the clone URL of the repository.
     */
    public String extractRepositoryUrl(String payload) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> payloadMap = gson.fromJson(payload, type);

        Map<String, Object> repository = (Map<String, Object>) payloadMap.get("repository");
        if (repository != null) {
            return (String) repository.get("clone_url");
        }
        return null; // [TODO]: throw an exception if the URL cannot be extracted
    }

    /**
     * Extracts the latest commit hash from the push event payload.
     *
     * @param payload the JSON payload received from the webhook.
     * @return the commit hash of the latest commit.
     */
    private String getLatestCommitHashFromPush(String payload) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> payloadMap = gson.fromJson(payload, type);
        
        List<Map<String, Object>> commits = (List<Map<String, Object>>) payloadMap.get("commits");
        if (commits != null && !commits.isEmpty()) {
            // The first commit in the list is the latest commit
            Map<String, Object> latestCommit = commits.get(0); 
            return (String) latestCommit.get("id");
        }
        return null; 
    }

    /**
     * This function handles the push event from the webhook and prints the latest commit message.
     *
     * @param payload
     */
    private void handlePushEvent(String payload) {
        String latestCommitMessage = getLatestCommitMessageFromPush(payload);
        if (latestCommitMessage != null) {
            System.out.println("Latest commit message: " + latestCommitMessage);
        }

        String repoUrl = extractRepositoryUrl(payload);

        String baseDirPath = System.getProperty("user.dir");
        String repoDir = "/repo_bdd"; //maybe a slight change in path is needed as we want ot be inside the repo we just cloned this folder holds all recent cloned copies
        String cloneDirPath = baseDirPath + repoDir;
        
        String commitHash = getLatestCommitHashFromPush(payload);

        cloneRepository(repoUrl, cloneDirPath, commitHash);
        // compileMavenProject(cloneDirPath); // [TODO]: Uncomment this line to compile the Maven project
    }

    /**
     * This function handles the pull request event from the webhook and prints the action.
     *
     * @param payload
     */
    private void handlePullRequestEvent(String payload) {
        // Parse the payload to extract pull request event information
        Gson gson = new Gson();
        Map payloadMap = gson.fromJson(payload, Map.class);
        String action = (String) payloadMap.get("action");
        System.out.println("Pull request action: " + action);
    }



    // used to start the CI server in command line
   public static void main(String[] args) throws Exception
    {
        Server server = new Server(8028);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }


}
