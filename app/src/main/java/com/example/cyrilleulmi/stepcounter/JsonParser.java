package com.example.cyrilleulmi.stepcounter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyrilleulmi on 11/17/2014.
 */
public class JsonParser {
    public static List<PathDescription> Parse(String jsonString)  {
        tryParseEndStation(jsonString);
        tryParseStartStation(jsonString);
        return tryParseInputFromJsonString(jsonString);
    }



    private static void tryParseEndStation(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            JsonSerializer.endStationNumber = json.getInt("endStation");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static void tryParseStartStation(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            JsonSerializer.startStationNumber = json.getInt("startStation");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static List<PathDescription> tryParseInputFromJsonString(String jsonString){
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray stepData = json.getJSONArray("input");
            List<PathDescription> pathDescriptionToReturn = new ArrayList<PathDescription>();

            for (int i = 0; i < stepData.length(); i = i + 2){
                if(i == 0){
                    HandleFirstStep(stepData, pathDescriptionToReturn, i);
                    i++;
                }

                Integer amountOfSteps = Integer.parseInt(stepData.get(i + 1).toString());
                if (i  < stepData.length()){
                    String string = stepData.get(i).toString();
                    StepDirection direction =  string.equals("links") ? StepDirection.Left : StepDirection.Right;
                    pathDescriptionToReturn.add(new PathDescription(amountOfSteps, direction));
                }

            }

            return pathDescriptionToReturn;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void HandleFirstStep(JSONArray stepData, List<PathDescription> pathDescriptionToReturn, int i) throws JSONException {
        Integer amountOfSteps = Integer.parseInt(stepData.get(i).toString());
        pathDescriptionToReturn.add(new PathDescription(amountOfSteps, StepDirection.None));
    }
}
