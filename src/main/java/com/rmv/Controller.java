package com.rmv;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Controller {
    static int timeSwitch = 100000;

    public Controller() throws IOException, InterruptedException {
        arrival();
        departure();
    }

    private static void arrival() throws IOException, InterruptedException {
        String startStation = decisionStation();
        String endStation = String.valueOf(Station.Fulda_Bahnhof);
        if (startStation != null && Integer.parseInt(getTime(true))<timeSwitch) {
            String startStation_id = RmvApi.getLocationId(startStation);
            String endStation_id = RmvApi.getLocationId(endStation);

            String msg = (RmvApi.findTrip(startStation_id, endStation_id, getTime(false)));

            Notify notify = new Notify();
            notify.sendMessage(msg);
        }
    }

    private static void departure() throws IOException, InterruptedException {
        String startStation = String.valueOf(Station.Fulda_Hochschule);
        String endStation = String.valueOf(Station.Lauterbach_Sportzentrum);
        if (decisionStation() != null && Integer.parseInt(getTime(true))>timeSwitch) {
            String startStation_id = RmvApi.getLocationId(startStation);
            String endStation_id = RmvApi.getLocationId(endStation);

            String msg = (RmvApi.findTrip(startStation_id, endStation_id, getTime(false)));

            Notify notify = new Notify();
            notify.sendMessage(msg);
        }
    }

    private static String decisionStation() {
        switch (getDate()) {
            case "Montag", "Freitag":
                return String.valueOf(Station.Lauterbach_Schulzentrum_Wascherde);
            case "Dienstag", "Donnerstag":
                return String.valueOf(Station.Lauterbach_Sportzentrum);
            default:
                return null;
        }
    }


    public static String getTime(boolean numberOnly) {
        Date dNow = new Date( );
        if (numberOnly) {
            SimpleDateFormat ft = new SimpleDateFormat ("HHmmss");
            return ft.format(dNow);
        }else {
            SimpleDateFormat ft = new SimpleDateFormat ("HH:mm:ss");
            return ft.format(dNow);
        }
    }

    public static String getDate() {
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("EEEE");
        return ft.format(dNow);
    }
}
