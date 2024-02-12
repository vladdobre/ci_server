// No package declaration here

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    // [IMPORTANT]: Update the path to the mvn executable
    // public final String[] mavenCommand = {
    //     "C:\\Program Files\\Maven\\apache-maven-3.9.6\\bin\\mvn.cmd",
    //     "clean",
    //     "install",
    //     ">", 
    //     "mavenOutput.txt"
    // };
    // Working in Ubuntu Linux
    public final String[] mavenCommand = {
        "mvn",
        "clean",
        "install",
        ">", 
        "mavenOutput.txt"
    };
    // Directory to store cloned repositories and build summaries. It's located in the server.
    public final  String repoDir = "../build_history"; 
     
    @Override
    /**
     * This function handles the incoming webhook requests from GitHub.
     * It reads the X-GitHub-Event header to determine the event type and
     * reads the JSON payload to extract the necessary information.
     * 
     * @param target - The target of the request
     * @param baseRequest - The original unwrapped request object
     * @param request - The request either as the Request object or a wrapper of that request
     * @param response - The response as the Response object or a wrapper of that request
     * @throws IOException - If an input or output exception occurs
     * @throws ServletException - If a servlet exception occurs
     * 
     * @return void
     */
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

        if ("push".equals(eventType)) {
            handlePushEvent(payload);
        } else if ("pull_request".equals(eventType)) {
            handlePullRequestEvent(payload);
        } else {
            System.out.println("Unhandled event type: " + eventType);
        }

        if ("/builds".equals(target)) {
            System.out.println("Target : "+ target);
            listBuilds(response);
        } else if (target.startsWith("/builds/")) {
            showBuildDetails(target, response);
        } else {
            // Handle other requests or show default message
            response.getWriter().println("CI Server is running. Use /builds to list all builds.");
        }
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
        try {
            Map<String, Object> payloadMap = request_to_map(payload);
            List<Map<String, Object>> commits = (List<Map<String, Object>>) payloadMap.get("commits");
            if (commits != null && !commits.isEmpty()) {
                Map<String, Object> latestCommit = commits.get(commits.size() - 1);
                return (String) latestCommit.get("message");
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Return null if no commits found
    }

    /**
     * Clones a Git repository to a specified directory.
     * The repository is cloned to a unique directory name generated from the commit hash and the current time.
     * 
     * @param repoUrl the URL of the repository to clone.
     * @param baseCloneDirPath the base path to the directory where the repository will be cloned.
     * @param uniqueDirName the hash of the commit to clone.
     * @return void
     * @throws GitAPIException
     */
    public void cloneRepository(String repoUrl, String baseCloneDirPath, String uniqueDirName) {
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
     * @param uniqueDirName the unique directory name generated from the commit hash and the current time.
     * @param payload the JSON payload received from the webhook.
     * @return void
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean compileMavenProject(String projectDirPath, String uniqueDirName, String payload){
        int exitCode = -1; // Default exit code for failure
        boolean buildSuccess = false;
        try {
            // Define the command to run mvn clean install
            // [IMPORTANT]: Update the command to use the correct path to the mvn executable
            List<String> command = Arrays.asList(mavenCommand);
            
            // Create a process builder to execute the command in the project directory
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(projectDirPath, uniqueDirName)); // Set the working directory
            
            // Inherit IO to display output in the console
            processBuilder.inheritIO();
            
            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete
            exitCode = process.waitFor();

            // Check the exit code to determine if the build was successful
            if (exitCode == 0) {
                System.out.println("Maven project compiled successfully.");
                buildSuccess = true;
            } else {
                System.err.println("Maven project compilation failed.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error compiling Maven project: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Generate and write JSON summary file
            generateSummaryFile(projectDirPath, uniqueDirName);
            
            String toEmail = extractEmail(payload);
            System.out.println("Email: " + toEmail);
            String subject = "Build Result Notification";
            try {
                sendBuildResultEmail(toEmail, subject, projectDirPath, uniqueDirName);
            } catch (Exception e) {
                System.err.println("Error sending build result email: " + e.getMessage());
                e.printStackTrace();
            }
            
        }
        return buildSuccess;
    } 
    
    /**
     * Generates a summary file for the build process.
     * The summary file contains information about the build status, compilation errors, and total time.
     * 
     * @param projectDirPath the path to the directory where the Maven project is located.
     * @param uniqueDirName the unique directory name generated from the commit hash and the current time.
     * @return void
     * @throws IOException
     */
    private void generateSummaryFile(String projectDirPath, String uniqueDirName) {
        // Prepare summary data
        Map<String, Object> summary = new HashMap<>();
        summary.put("uniqueDirName", uniqueDirName);

        // Read the Maven output file and extract relevant information
        String mavenOutputFile = projectDirPath + File.separator + uniqueDirName + File.separator + "mavenOutput.txt";
        List<String> errorLines = new ArrayList<>();
        List<String> infoLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(mavenOutputFile))) {
            String line;
            boolean errorEncountered = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("[ERROR]")) {
                    errorEncountered = true;
                    errorLines.add(line);
                } else if (line.contains("[INFO]")) {
                    infoLines.add(line);
                }
                if (line.startsWith("[INFO] Total time:")) {
                    String[] parts = line.split(": ");
                    if (parts.length == 2) {
                        String totalTime = parts[1].trim();
                        summary.put("totalTime", totalTime);
                    }
                }
                if (line.startsWith("[INFO] Finished at:")) {
                    String finishedAt = line.substring("[INFO] Finished at:".length()).trim();
                    summary.put("finishedAt", finishedAt);
                }
            }
            // If errors were encountered, mark the summary as a failure
            if (errorEncountered) {
                summary.put("buildStatus", "FAILURE");
            } else {
                summary.put("buildStatus", "SUCCESS");
            }
        } catch (IOException e) {
            System.err.println("Error reading Maven output file: " + e.getMessage());
            e.printStackTrace();
        }

        // Add compilation errors and info lines to the summary
        summary.put("errorLines", errorLines);
        summary.put("infoLines", infoLines);

        // Write summary to JSON file
        String summaryFile = projectDirPath + File.separator + uniqueDirName + File.separator + "build_summary.json";
        try (FileWriter fileWriter = new FileWriter(summaryFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(summary, fileWriter);
            System.out.println("Build summary file written to: " + summaryFile);
        } catch (IOException e) {
            System.err.println("Error writing build summary file: " + e.getMessage());
            e.printStackTrace();
        }   
    }

    /**
     * This function sends an email notification with the build result.
     * @param toEmail - The email address to send the notification to
     * @param subject - The subject of the email
     * @param projectDirPath - The path to the directory where the Maven project is located
     * @param uniqueDirName - The unique directory name generated from the commit hash and the current time
     * @return void
     * @throws Exception
     */
    public void sendBuildResultEmail(String toEmail, String subject, String projectDirPath, String uniqueDirName) {
        final String fromEmail = "group28github@gmail.com"; //requires valid Gmail id
        final String password = "asux vdff sxnl gprt"; // correct password for Gmail id

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
        props.put("mail.smtp.port", "587"); // TLS Port
        props.put("mail.smtp.auth", "true"); // enable authentication
        props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS

        // Create a session with account credentials
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Read the contents of build_summary.json
            String summaryFilePath = projectDirPath + "/" + uniqueDirName + "/build_summary.json";
            StringBuilder summaryContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(summaryFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    summaryContent.append(line).append("\n");
                }
            }

            // Create the message
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail, "CI Server"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(subject);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Create the HTML part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(summaryContent.toString(), "text/plain");
            multipart.addBodyPart(messageBodyPart);

            // Set the multipart message
            msg.setContent(multipart);

            // Send the message
            Transport.send(msg);

            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the cloned repository from the file system.
     * This function deletes the cloned repository directory and all its contents, except for the build summary file.
     * 
     * @param cloneDirPath the path to the directory where the repository was cloned.
     * @param uniqueDirName the unique directory name generated from the commit hash and the current time.
     * @return void
     */
    public void removeClonedRepository(String cloneDirPath, String uniqueDirName) {
        File clonedRepo = new File(cloneDirPath, uniqueDirName);
        if (clonedRepo.exists() && clonedRepo.isDirectory()) {
            System.out.println("Deleting cloned repository: " + clonedRepo.getPath());
            File[] files = clonedRepo.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().equals("build_summary.json")) {
                        deleteFile(file);
                    }
                }
            }
        }
    }

    /**
     * Deletes a file or directory from the file system.
     * This function is used to delete the cloned repository directory and its contents.
     * 
     * @param file the file or directory to delete.
     * @return void
     */
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteFile(f);
                }
            }
        }
        if (!file.delete()) {
            System.err.println("Failed to delete file: " + file.getPath());
        }
    }
    
    /**
     * Extracts the email of the committer from the webhook payload.
     * @param payload - The JSON payload received from the webhook
     * @return The email of the committer
     * @throws Exception
     */
    public String extractEmail(String payload) {
        try {
            Map<String, Object> payloadMap = request_to_map(payload);
            Map<String, Object> head_commit = (Map<String, Object>) payloadMap.get("head_commit");
            if (head_commit != null) {
                Map<String, Object> author = (Map<String, Object>) head_commit.get("author");
                if (author != null) {
                    return (String) author.get("email");
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
            e.printStackTrace();
        }
        return null; 
    }

     /**
     * Extracts the repository clone URL from the webhook payload.
     * This function uses the Gson library to parse the JSON payload and extract the repository URL.
     * If the repository URL cannot be extracted, it returns null.
     *
     * @param payload the JSON payload received from the webhook.
     * @return the clone URL of the repository.
     * @throws Exception
     */
    public String extractRepositoryUrl(String payload) {
        try {
            Map<String, Object> payloadMap = request_to_map(payload);
            Map<String, Object> repository = (Map<String, Object>) payloadMap.get("repository");
            if (repository != null) {
                return (String) repository.get("clone_url");
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Extracts the repository name from the webhook payload.
     * 
     * @param payload
     * @return the name of the repository
     * @throws Exception 
     */
    public String extractRepositoryName(String payload) {
        try {
            Map<String, Object> payloadMap = request_to_map(payload);
            Map<String, Object> repository = (Map<String, Object>) payloadMap.get("repository");
            if (repository != null) {
                return (String) repository.get("name");
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Extracts the branch name from the webhook payload.
     * 
     * @param payload
     * @return the name of the branch
     * @throws Exception
     */
    public String extractBranchName(String payload) {
        try {
            Map<String, Object> payloadMap = request_to_map(payload);
            String ref = (String) payloadMap.get("ref");
            if (ref != null && ref.startsWith("refs/heads/")) {
                return ref.substring("refs/heads/".length());
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON payload for branch name: " + e.getMessage());
            e.printStackTrace();
        }
        return null; 
    }
    
    /**
     * Extracts the latest commit hash from the push event payload.
     *
     * @param payload the JSON payload received from the webhook.
     * @return the commit hash of the latest commit.
     * @throws Exception
     */
    private String getLatestCommitHashFromPush(String payload) {
        try {
            Map<String, Object> payloadMap = request_to_map(payload);
            List<Map<String, Object>> commits = (List<Map<String, Object>>) payloadMap.get("commits");
            if (commits != null && !commits.isEmpty()) {
                // The first commit in the list is the latest commit
                Map<String, Object> latestCommit = commits.get(0); 
                return (String) latestCommit.get("id");
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
            e.printStackTrace();
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
        String repoName = extractRepositoryName(payload);
        String branchName = extractBranchName(payload);
        String baseDirPath = System.getProperty("user.dir");
        String cloneDirPath = baseDirPath +"/"+ repoDir;
        String commitHash = getLatestCommitHashFromPush(payload);
        // Create a unique directory name using the commit hash and the current time
        String uniqueDirName = repoName + "_" + branchName + "_" +commitHash + "_" + System.currentTimeMillis();

        cloneRepository(repoUrl, cloneDirPath, uniqueDirName);
        compileMavenProject(cloneDirPath, uniqueDirName, payload);
        removeClonedRepository(cloneDirPath, uniqueDirName);
    }

    /**
     * This function handles the pull request event from the webhook and prints the action.
     *
     * @param payload
     */
    private void handlePullRequestEvent(String payload) {
        // Parse the payload to extract pull request event information
        Map payloadMap = request_to_map(payload);
        String action = (String) payloadMap.get("action");
        System.out.println("Pull request action: " + action);
    }


    /**
     * This function lists all the builds in the repoDir directory.
     * 
     * @param response
     * @return void
     * @throws IOException
     */
    private void listBuilds(HttpServletResponse response) throws IOException {
        File buildDir = new File(repoDir); 
        File[] buildDirs = buildDir.listFiles(File::isDirectory); // List all directories within repoDir
    
        PrintWriter out = response.getWriter();
        out.println("<h1>Build History</h1>");
    
        if (buildDirs != null) {
            for (File dir : buildDirs) {
                String buildId = dir.getName(); // Use the directory name as the build ID
                out.println("<div><a href='/builds/" + buildId + "'>" + buildId + "</a></div>");
            }
        }
    }
    
    /**
     * This function shows the build details for a specific build.
     * 
     * @param target
     * @param response
     * @return void
     * @throws IOException
     */
    private void showBuildDetails(String target, HttpServletResponse response) throws IOException {
        String buildId = target.substring("/builds/".length()); // Extract the build ID from the URL
        File buildDir = new File(repoDir, buildId);
    
        PrintWriter out = response.getWriter();
        if (buildDir.exists() && buildDir.isDirectory()) {
            File buildSummaryFile = new File(buildDir, "build_summary.json");
            if (buildSummaryFile.exists()) {
                String content = new String(Files.readAllBytes(buildSummaryFile.toPath()), StandardCharsets.UTF_8);
                out.println("<h1>Build Details for " + buildId + "</h1>");
                out.println("<pre>" + content + "</pre>");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("<h1>Build Details Not Found</h1>");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("<h1>Build Not Found</h1>");
        }
    }
    
    /**
     * This function starts the CI server on port 8028.
     * @param args - Command line arguments
     * @throws Exception 
     */
   public static void main(String[] args) throws Exception {
        Server server = new Server(8028);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}
