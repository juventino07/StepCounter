package com.example.cyrilleulmi.stepcounter;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Created by cyrilleulmi on 11/21/2014.
 */
public class JsonSerializer {
    private static String startStation = "startStation";
    public static Integer startStationNumber;

    private static String endStation = "endStation";
    public static Integer endStationNumber;

    public static String getJsonString(){
        return "{\"" + startStation + "\": " + startStationNumber.toString() +
                ", \"" + endStation + "\": " + endStationNumber + "}";
    }
}
