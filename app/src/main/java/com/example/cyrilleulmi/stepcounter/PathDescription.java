package com.example.cyrilleulmi.stepcounter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cyrilleulmi on 11/17/2014.
 */
public class PathDescription{
    private int amountOfSteps;
    private StepDirection stepDirection;

    public PathDescription(int amountOfSteps, StepDirection stepDirection) {
        this.amountOfSteps = amountOfSteps;
        this.stepDirection = stepDirection;
    }

    public Integer getAmountOfSteps(){
        return amountOfSteps;
    }

    public StepDirection getStepDirection(){
        return stepDirection;
    }
}
