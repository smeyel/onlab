package com.ol.research.photographer;

import org.opencv.core.Core;

import android.util.Log;

public class TimeMeasurement {
	
	static public boolean isOpenCVLoaded = false; // it has to be set on "true", when openCV is loaded!!! otherwise the CommsThread waits
	double TickFrequency;	
	long StartTick;
	
	void TimeMeasurementStart()
	{
		if (isOpenCVLoaded)
		{
			TickFrequency = Core.getTickFrequency();
			//double divider = TickFrequency / 1000000.0;
			StartTick = Core.getCPUTickCount();
			//StartTimestamp = (long)(Math.round(tick / divider)); // in microseconds
			return;	
		}
		else
		{
			Log.e("openCV", "OpenCV is not loaded!");
			return;
		}
	}
	
	double TimeMeasurementStop()
	{
		if (isOpenCVLoaded)
		{
			long StopTick = Core.getCPUTickCount();
			TickFrequency = Core.getTickFrequency();
			double divider = TickFrequency / 1000000.0;
			double TimeInterval = (double)(Math.round((StopTick - StartTick) / divider) / 1000.0); // in milliseconds
			return TimeInterval;	
		}
		else
		{
			Log.e("openCV", "OpenCV is not loaded!");
			return -1;
		}
	}
	
	public static long GetTimeStamp()
	{
		if (isOpenCVLoaded)
		{
			double TickFrequency = Core.getTickFrequency();
			double divider = TickFrequency / 1000000.0;
			long tick = Core.getCPUTickCount();
			long timestamp = (long)(Math.round(tick / divider));
			return timestamp;	// Returns in microseconds
		}
		else
		{
			return 0;
		}
	}
	
	public static double CalculateIntervall(long StartTick, long FinishTick)
	{
		double TickFrequency = Core.getTickFrequency();
		double divider = TickFrequency / 1000000.0;
		double TimeInterval = (double)(Math.round((FinishTick - StartTick) / divider) / 1000.0); // in milliseconds
		return TimeInterval;
	}
}
