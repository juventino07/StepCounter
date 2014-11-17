package com.example.cyrilleulmi.stepcounter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class StepCounter implements SensorEventListener {

	private static final int LONG = 1000;
	private static final int SHORT = 50;

	private boolean accelerating = false;
	private StepListener listener;

	private RingBuffer shortBuffer = new RingBuffer(SHORT);
	private RingBuffer longBuffer = new RingBuffer(LONG);

	public StepCounter(StepListener listener) {
		this.listener = listener;
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