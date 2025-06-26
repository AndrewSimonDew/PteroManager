package org.andrexserver.pteroManager.Pterodactyl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParseException;
import org.andrexserver.pteroManager.Main;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class PteroAPI {


    public PowerStatus power(String url,String apiKey, String serverUUID, PowerAction action) {
        if(serverUUID == null) {
            return PowerStatus.INVALID_SERVER;
        }
        StatsResponse stats = parseResources(url, apiKey, serverUUID);
        if(stats == null) {
            return PowerStatus.INVALID_STATUS;
        }
        boolean state = stats.attributes.current_state.equalsIgnoreCase("running");
        if(state && action == PowerAction.START) {
            return PowerStatus.ALREADY_RUNNING;
        }
        if(!state && (action == PowerAction.STOP || action == PowerAction.RESTART)) {
            return PowerStatus.ALREADY_DOWN;
        }


        HttpClient client = HttpClient.newHttpClient();
        String json = "{\"signal\":\""+ action.getActionName() +"\"}";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url+"/api/client/servers/" + serverUUID + "/power"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.body().length() > 10) {
                Main.logger.warn("Response: {}", response.body());
                Main.logger.error("Attempted to {} {}\nServer with that UUID does not exist!", action.getActionName(), serverUUID);
                return PowerStatus.INVALID_UUID;
            }
            return PowerStatus.SUCCESS;
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return PowerStatus.INVALID_REQUEST;
        }
    }




    public StatsResponse parseResources(String url,String apiKey,String serverUUID) throws JsonParseException {
        if(serverUUID == null) {
            return null;
        }
        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url+"/api/client/servers/" + serverUUID + "/resources"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.body(), StatsResponse.class);

        } catch (IOException | InterruptedException | URISyntaxException e) {
            Main.logger.error("Invalid Request!");
            return null;
        }

    }

    public String resolveServerName(String serverName) throws IllegalArgumentException {
        if(!Main.serverList.containsKey(serverName)) {
            return null;
        }
        return (String) Main.serverList.get(serverName);
    }
}

