package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";

    public static String eventsAsHTML(String user) throws IOException, ParseException {
        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (int i = 0; i < response.size(); i++) {
            JSONObject event = response.get(i);
            // Get event type
            String type = event.getString("type");
            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);

            // Get a vertical list of SHA's within the commit
            JSONObject payload = event.getJSONObject("payload");
            JSONArray commits = payload.getJSONArray("commits");
            ArrayList<String> arSha = new ArrayList<String>();
            ArrayList<String> arMessage = new ArrayList<String>();
            int s = 0;
//
            for (int j = 0; j < commits.length(); j++) {
                JSONObject com = commits.getJSONObject(j);
                arSha.add(com.getString("sha"));
                arMessage.add(com.getString("message"));
            }

            /*ArrayList to Array Conversion */
            String arrayS[] = new String[arSha.size()];
            for(int j =0;j<arSha.size();j++){
                arrayS[j] = arSha.get(j);
            }
            /*ArrayList to Array Conversion */
            String arrayM[] = new String[arMessage.size()];
            for(int j =0;j<arMessage.size();j++){
                arrayM[j] = arMessage.get(j);
            }
            // end SHA extraction

            // Add type of event as header
            sb.append("<h3 class=\"type\">");
            sb.append(type);
            sb.append("</h3>");
//            // Add formatted date
//            sb.append(" on ");
//            sb.append(formatted);
//            sb.append("<br />");

            // Add SHA's and commit messages
            if (arSha.size() == 1) sb.append("This event on " + formatted + " has 1 commit, and its SHA identifier " +
                    "and commit message are shown below in chronological order:");
            else sb.append("This event on " + formatted + " has " + arSha.size() + " commits, and their SHA identifiers " +
                    "and commit messages are shown below in chronological order:");
            for (int k = 0; k < arSha.size(); k++){
                sb.append("<br />SHA: " + arrayS[k] + "<br />");
                sb.append("Commit message (blank if none): " + arrayM[k] + "<br />");
            }
            sb.append("<br />");

            // Add collapsible JSON textbox (don't worry about this for the homework; it's just a nice CSS thing I like)
            sb.append("<a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(event.toString());
            sb.append("</pre> </div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        String url = BASE_URL + user + "/events?per_page=100&state=all";

        int page = 1;
        int count = 0;

        while (true) {
            String copy = url + "&page=" + page;

            System.out.println(url); // might not need
            //JSONObject json = Util.queryAPI(new URL(url));
            JSONObject json = Util.queryAPI(new URL(copy));
            System.out.println(json); // might not need

            JSONArray events = json.getJSONArray("root");
            if (count >=10 || events.length() == 0){
                break;
            }

            for (int i = 0; i < events.length(); i++) {
                // Base case
                if (count >= 10) break;

                JSONObject ob = events.getJSONObject(i);

                if (ob.getString("type").equals("PushEvent")) {
                    eventList.add(ob);
                    count++;
                }
            }
            page++;
        }

        return eventList;
    }
}