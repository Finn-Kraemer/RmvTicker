package com.rmv;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Notify {

    public Notify() {
    }

    public void sendMessage(String msg) throws IOException, InterruptedException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh/" + new Secrets().getNtfy_key()))
                    .POST(HttpRequest.BodyPublishers.ofString(createMessage(msg)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ignored) {}
    }

    public static String createMessage(String tripJson) {
        int noon = 0;
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("a");
        if(ft.format(dNow).equals("PM")){
            noon = 12;
        }

        JSONObject json = new JSONObject(tripJson);

        // Prüfen, ob bei der Abfrage ein Fehler aufgetreten ist.
        if (json.has("error")) {
            return "Fehler: " + json.getString("error");
        }

        StringBuilder message = new StringBuilder();
        message.append("Deine Verbindung von ")
                .append(json.getString("overallOrigin"))
                .append(" nach ")
                .append(json.getString("overallDestination"))
                .append(":\n");

        JSONArray legs = json.getJSONArray("legs");

        for (int i = 0; i < legs.length(); i++) {
            JSONObject leg = legs.getJSONObject(i);
            JSONObject details = leg.getJSONObject("details");
            JSONObject departure = leg.getJSONObject("departure");
            JSONObject arrival = leg.getJSONObject("arrival");

            message.append("--------------------------------------------------\n");

            if (leg.getString("type").equals("WALK")) {
                message.append(String.format("🚶 %d min Fußweg nach: %s\n",
                        details.getInt("durationMinutes"),
                        arrival.getString("station")));
            } else { // Typ ist "RIDE"
                message.append(String.format("Abfahrt: %s Uhr von %s\n",
                        departure.getString("time"),
                        departure.getString("station")));
                message.append(String.format("--> %s in Richtung %s\n",
                        details.getString("line"),
                        details.getString("direction")));
                message.append(String.format("Ankunft: %s Uhr an %s\n",
                        arrival.getString("time"),
                        arrival.getString("station")));
            }
        }
        message.append("--------------------------------------------------\n");
        return message.toString();
    }


}
