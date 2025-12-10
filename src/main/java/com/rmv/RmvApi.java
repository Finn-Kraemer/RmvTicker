package com.rmv;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Scanner;

public class RmvApi {
    private static final String API_KEY = new Secrets().getApi_key();

    private static final String LOCATION_URL = "https://www.rmv.de/hapi/location.name";

    private static final String TRIP_URL = "https://www.rmv.de/hapi/trip";

    public RmvApi() {
    }

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String getLocationId(String stationName) throws IOException, InterruptedException {
        String encodedStationName = URLEncoder.encode(stationName, StandardCharsets.UTF_8);
        String requestUri = LOCATION_URL + "?accessId=" + API_KEY + "&format=json&maxNo=1&input=" + encodedStationName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUri))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Fehler bei der Abfrage der Haltestellen-ID: " + response.statusCode());
            return null;
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        JSONArray locations = jsonResponse.getJSONArray("stopLocationOrCoordLocation");

        if (locations.isEmpty()) {
            System.err.println("Keine Haltestelle für '" + stationName + "' gefunden.");
            return null;
        }

        return locations.getJSONObject(0).getJSONObject("StopLocation").getString("extId");
    }

    public static String findTrip(String originId, String destinationId, String time) throws IOException, InterruptedException {
        String requestUri = null;

        if (originId == null || destinationId == null || time == null) {
            requestUri = TRIP_URL + "?accessId=" + API_KEY + "&format=json&originExtId=" + originId + "&destExtId=" + destinationId;
        } else {
            requestUri = TRIP_URL + "?accessId=" + API_KEY + "&format=json&originExtId=" + originId + "&destExtId=" + destinationId + "&time=" + time /*+ "&searchForArrival=true"*/;
        }

        System.out.println(requestUri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUri))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            // Bei einem Fehler geben wir ein JSON-Objekt mit der Fehlermeldung zurück
            return new JSONObject().put("error", "Fehler bei der Verbindungsabfrage: " + response.statusCode()).toString();
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        if (!jsonResponse.has("Trip") || jsonResponse.getJSONArray("Trip").isEmpty()) {
            return new JSONObject().put("error", "Keine Verbindung zwischen den angegebenen Haltestellen gefunden.").toString();
        }

        // JSON-Erstellung

        JSONObject resultJson = new JSONObject();
        JSONArray legsJsonArray = new JSONArray();

        JSONObject firstTrip = jsonResponse.getJSONArray("Trip").getJSONObject(0);
        JSONArray legs = firstTrip.getJSONObject("LegList").getJSONArray("Leg");

        // Gesamt-Start und -Ziel zum finalen JSON-Objekt hinzufügen
        if (!legs.isEmpty()) {
            resultJson.put("overallOrigin", legs.getJSONObject(0).getJSONObject("Origin").getString("name"));
            resultJson.put("overallDestination", legs.getJSONObject(legs.length() - 1).getJSONObject("Destination").getString("name"));
        }

        for (int i = 0; i < legs.length(); i++) {

            JSONObject leg = legs.getJSONObject(i);
            JSONObject legJson = new JSONObject();
            JSONObject origin = leg.getJSONObject("Origin");
            JSONObject destination = leg.getJSONObject("Destination");

            legJson.put("departure", new JSONObject()
                    .put("station", origin.getString("name"))
                    .put("time", origin.getString("time").substring(0, 5)));
            legJson.put("arrival", new JSONObject()
                    .put("station", destination.getString("name"))
                    .put("time", destination.getString("time").substring(0, 5)));

            if (leg.getString("type").equals("WALK")) {
                legJson.put("type", "WALK");

                // Dauer aus "PT5M" extrahieren
                String durationStr = leg.getString("duration");
                int durationMinutes = 0;
                try {
                    // Entfernt "PT" am Anfang und "M" am Ende, dann Umwandlung in eine Zahl
                    durationMinutes = Integer.parseInt(durationStr.replace("PT", "").replace("M", ""));
                } catch (NumberFormatException e) {
                    System.err.println("Konnte Dauer nicht parsen: " + durationStr);
                }

                legJson.put("details", new JSONObject()
                        .put("durationMinutes", durationMinutes)
                        .put("distanceMeters", leg.getInt("dist")));
            } else {
                legJson.put("type", "RIDE");
                String lineName = "Unbekannt";
                if (leg.has("Product")) {
                    JSONArray products = leg.getJSONArray("Product");
                    if (!products.isEmpty()) {
                        lineName = products.getJSONObject(0).getString("name").trim();
                    }
                }
                legJson.put("details", new JSONObject()
                        .put("line", lineName)
                        .put("direction", leg.has("direction") ? leg.getString("direction") : "N/A"));
            }
            legsJsonArray.put(legJson);
        }

        resultJson.put("legs", legsJsonArray);
        return resultJson.toString(4);
    }
}
