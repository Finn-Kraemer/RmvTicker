package com.rmv;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args[0].equals("-test")) {
            String startStation = "Lauterbach Sportzentrum";
            String endStation = "Fulda Hochschule";
            String startStation_id = RmvApi.getLocationId(startStation);
            String endStation_id = RmvApi.getLocationId(endStation);

            String msg = (RmvApi.findTrip(startStation_id, endStation_id, Controller.getTime(false)));

            Notify notify = new Notify();
            notify.sendMessage(msg);
        } else if (args[0].equals("-start")) {
            Controller controller = new Controller();
        }


    }
}