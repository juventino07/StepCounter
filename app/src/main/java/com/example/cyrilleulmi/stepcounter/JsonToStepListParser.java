package com.example.cyrilleulmi.stepcounter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cyrilleulmi on 11/17/2014.
 */
public class JsonToStepListParser {
    public static List<PathDescription> Parse(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        JSONArray stepData = json.getJSONArray("input");
        List<PathDescription> pathDescriptionToReturn = new ArrayList<PathDescription>();

        for (int i = 0; i < stepData.length(); i = i + 2){
            Integer amountOfSteps = Integer.parseInt(stepData.get(i).toString());

            if (i + 1 < stepData.length()){
                String string = stepData.get(i + 1).toString();
                StepDirection direction =  string.equals("links") ? StepDirection.Left : StepDirection.Right;
                pathDescriptionToReturn.add(new PathDescription(amountOfSteps, direction));
            }
            else{
                pathDescriptionToReturn.add(new PathDescription(amountOfSteps, StepDirection.None));
            }
        }

        return pathDescriptionToReturn;
    }
}
