package com.example.cyrilleulmi.stepcounter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class StepCounter implements SensorEventListener {

	private static final int LONG = 500;
	private static final int SHORT = 250;
    private final Sensor accelerationSensor;

    private boolean accelerating = false;
	private StepListener listener;

	private RingBuffer shortBuffer = new RingBuffer(SHORT);
	private RingBuffer longBuffer = new RingBuffer(LONG);

    private SensorManager sensorManager;

	public StepCounter(StepListener listener, Context context) {
		this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_UI);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		float magnitude = (float) (Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

		shortBuffer.put(magnitude);
		longBuffer.put(magnitude);

		float shortAverage = shortBuffer.getAverage();
		float longAverage = longBuffer.getAverage();

		if (!accelerating && (shortAverage > longAverage * 1.1)) {
			accelerating = true;
			listener.onStep();
		}

		if ((accelerating && shortAverage < longAverage * 0.9)) {
			accelerating = false;
		}
	}
}