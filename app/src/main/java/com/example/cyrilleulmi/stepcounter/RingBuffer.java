package com.example.cyrilleulmi.stepcounter;

class RingBuffer {
	private float[] buffer;
	private int capacity;
	private int current = 0;
	private int count = 0;

	public int getCount() {
		return count;
	}

	public RingBuffer(int capacity) {
		this.capacity = capacity;
		buffer = new float[capacity];
	}

	public void put(float f) {
		buffer[current] = f;
		current++;
		count++;
		current = current % capacity;
	}

	public float getAverage() {
		float avg = 0;
		for (float f : buffer) {
			avg += f;
		}
		if (count > capacity) {
			return avg / capacity;
		} else {
			return avg / count;
		}
	}
}