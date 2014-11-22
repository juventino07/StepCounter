package com.example.cyrilleulmi.stepcounter;

import android.animation.TypeConverter;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.List;
import java.util.Locale;

public class TurnActivity extends Activity implements StepListener, TextToSpeech.OnInitListener {

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
    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turn);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.mTts = new TextToSpeech(this, this);
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
        this.DisplayPathDescriptionOnUi();
        this.stepCounter = new StepCounter(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
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

        if(JsonSerializer.endStationNumber != null){
            HsrLogger.LogMessage(JsonSerializer.getJsonString(), this);
        }
        else if (JsonSerializer.startStationNumber != null){
            this.DisplayPathDescriptionOnUi();
            this.SpeakStatus();
        }
    }

    public void ScanBarcode_Click(MenuItem item) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    public void Next(View view) {
        this.HandleStep();
        this.DisplayPathDescriptionOnUi();
    }

    private void HandleStep() {
        try {
            this.amountOfTakenSteps++;

            if (this.amountOfTakenSteps == this.pathDescription.get(this.currentPathItem).getAmountOfSteps()) {
                this.amountOfTakenSteps = 0;
                this.currentPathItem++;
                this.SpeakStatus();
            }
        }
        catch (Exception e) {
            TextView textView = (TextView)findViewById(R.id.amountOfStepsTextView);
            textView.setText("");
            SetShownImageTo(R.drawable.destination);
            String destinationmessage = "Here is your target destination";
            SpeakText(destinationmessage);
            Toast toast = Toast.makeText(this, destinationmessage, Toast.LENGTH_LONG);
            toast.show();
        }


    }

    private void SpeakStatus() {
        StepDirection stepDirection = this.getCurrentDirection();
        String string = this.pathDescription.get(this.currentPathItem).getAmountOfSteps().toString();
        String amountOfStepsToRead = "Walk " + string + " Steps forward";
        if (stepDirection == StepDirection.Right){
            this.SpeakText("Turn Right, then " + amountOfStepsToRead);
        }
        else if (stepDirection == StepDirection.Left){
            this.SpeakText("TurnLeft, then " + amountOfStepsToRead);
        }
        else if (stepDirection == StepDirection.None){
            this.SpeakText(amountOfStepsToRead);
        }
    }

    private void SpeakText(String textToRead) {
        this.mTts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null);
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
            TextView textViewAmountOfSteps = (TextView) findViewById(R.id.amountOfStepsTextView);
            Integer amountOfStepsToTake = this.pathDescription.get(this.currentPathItem).getAmountOfSteps() - this.amountOfTakenSteps;
            textViewAmountOfSteps.setText(amountOfStepsToTake.toString());
    }

    private void SetShownArrowImage() {
        StepDirection direction = getCurrentDirection();
        if (direction == StepDirection.Right) {
            SetShownImageTo(R.drawable.right);
        }
        else if (direction == StepDirection.Left) {
            SetShownImageTo(R.drawable.left);
        }
    }

    private StepDirection getCurrentDirection() {
        return this.pathDescription.get(this.currentPathItem).getStepDirection();
    }

    private void SetShownImageTo(int pictureIndex) {
        ImageView imageViewDirection = (ImageView) findViewById(R.id.FollowingDirectionImageView);
        imageViewDirection.setImageResource(pictureIndex);
    }

    private List<PathDescription> TryParseReturnCode(String returnedCode) {
            return JsonParser.Parse(returnedCode);
    }

    @Override
    public void onStep() {
        if(this.pathDescription != null){
            this.HandleStep();
            this.DisplayPathDescriptionOnUi();
        }
        else {

        }
    }

    @Override
    public void onInit(int status) {
        mTts.setLanguage(Locale.UK);
    }

}
