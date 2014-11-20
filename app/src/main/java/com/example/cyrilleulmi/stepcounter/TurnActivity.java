package com.example.cyrilleulmi.stepcounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.List;

public class TurnActivity extends Activity implements SensorEventListener, StepListener {

    private static final int BUFFER_SIZE = 10;
    private SensorManager sensorManager;
    private RingBuffer initialRotation = new RingBuffer(BUFFER_SIZE);
    private RingBuffer rotation = new RingBuffer(BUFFER_SIZE);
    private Sensor rotationSensor;
    private Sensor accelerationSensor;
    private List<PathDescription> pathDescription;
    private Integer amountOfTakenSteps = 0;
    private Integer currentPathItem = 0;
    private String jsonPathDefinition;
    private StepCounter stepCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("amountOfTakenSteps", this.amountOfTakenSteps);
        outState.putInt("currentPathItem", this.currentPathItem);
        outState.putString("jsonPathDefinition", this.jsonPathDefinition);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.amountOfTakenSteps = savedInstanceState.getInt("amountOfTakenSteps");
        this.currentPathItem = savedInstanceState.getInt("currentPathItem");
        this.jsonPathDefinition = savedInstanceState.getString("jsonPathDefinition");

        this.ParseReturnCode(this.jsonPathDefinition);

        this.DisplayPathDescriptionOnUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.turn, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        this.DisplayPathDescriptionOnUi();
        this.stepCounter = new StepCounter(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    float[] rotationMatrix = new float[16];
    float[] orientationVals = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationVals);

            orientationVals[0] = (float) Math.toDegrees(orientationVals[0]);

            // Zuerst füllen wir den Buffer mit der Initialrotation um den
            // Startwinkel zu bestimmen, und wenn dieser voll ist (was sehr
            // schnell passiert), dann füllen wir einen zweiten RingBuffer.
            if (initialRotation.getCount() < BUFFER_SIZE) {
                initialRotation.put(orientationVals[0]);
            } else {
                rotation.put(orientationVals[0]);
            }

            // Wenn der zweite Buffer auch gefüllt ist, vergleichen wir die
            // beiden Durchschnittswerte fortlaufend, und sobald wir eine
            // Drehung von grösser als 50 Grad erkennen, melden wir dies.
            if (rotation.getCount() >= BUFFER_SIZE) {
                float r = Math.abs(rotation.getAverage() - initialRotation.getAverage());
                if (r > 50) {
                    Toast.makeText(this, "Du hast dich gedreht!", Toast.LENGTH_SHORT).show();
                    System.out.println("Du hast dich gedreht!");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            String returnedCode = intent.getStringExtra("SCAN_RESULT");
            ParseReturnCode(returnedCode);
        }
    }

    private void ParseReturnCode(String returnedCode) {
        this.jsonPathDefinition = returnedCode;
        this.pathDescription = TryParseReturnCode(returnedCode);

        this.DisplayPathDescriptionOnUi();
    }

    public void ScanBarcode_Click(MenuItem item) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public void NextItemClick(View view) {
        this.HandleStep();
        this.DisplayPathDescriptionOnUi();
    }

    private void HandleStep() {
        this.amountOfTakenSteps++;

        if (this.amountOfTakenSteps == this.pathDescription.get(this.currentPathItem).getAmountOfSteps()){
            this.amountOfTakenSteps = 0;
            this.currentPathItem++;
        }
    }

    private void DisplayPathDescriptionOnUi() {
        try{
            SetShownText();
            SetShownArrowImage();
           }
        catch(Exception e){
        }
    }

    private void SetShownText() {
        TextView textViewAmountOfSteps = (TextView)findViewById(R.id.amountOfStepsTextView);
        Integer amountOfStepsToTake = this.pathDescription.get(this.currentPathItem).getAmountOfSteps() - this.amountOfTakenSteps;
        textViewAmountOfSteps.setText(amountOfStepsToTake.toString());
    }

    private void SetShownArrowImage() {
        StepDirection direction = this.pathDescription.get(this.currentPathItem).getStepDirection();
        if (direction == StepDirection.Right) {
            SetShownImageTo(R.drawable.arrow_right);
        }
        else if (direction == StepDirection.Left) {
            SetShownImageTo(R.drawable.arrow_left);
        }
    }

    private void SetShownImageTo(int pictureIndex) {
        ImageView imageViewDirection = (ImageView) findViewById(R.id.FollowingDirectionImageView);
        imageViewDirection.setImageResource(pictureIndex);
    }

    private List<PathDescription> TryParseReturnCode(String returnedCode) {
        try {
            return JsonToStepListParser.Parse(returnedCode);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onStep() {
        this.HandleStep();
        this.DisplayPathDescriptionOnUi();
    }
}
