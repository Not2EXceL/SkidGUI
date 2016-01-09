package me.lpk.util;
public class Timer {
	private long previousTime;

	public Timer() {
		reset();
	}

	public boolean check(float milliseconds) {
		return getTime() >= milliseconds;
	}
	
	public long getTime(){
		return getCurrentTime() - previousTime;
	}

	public void reset() {
		previousTime = getCurrentTime();
	}

	public long getCurrentTime() {
		return System.currentTimeMillis();
	}
}