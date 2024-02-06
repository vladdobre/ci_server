package src;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

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

        System.out.println(target);

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
     * This function handles the push event from the webhook and prints the latest commit message.
     * 
     * @param payload
     */
    private void handlePushEvent(String payload) {
        // Parse the payload to extract push event information
        Gson gson = new Gson();
        Map payloadMap = gson.fromJson(payload, Map.class);
        List commits = (List) payloadMap.get("commits");
        if (!commits.isEmpty()) {
            Map latestCommit = (Map) commits.get(commits.size() - 1);
            System.out.println("Latest commit message: " + latestCommit.get("message"));
        }
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
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer()); 
        server.start();
        server.join();
    }
}
