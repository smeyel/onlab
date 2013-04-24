package com.ol.research.photographer;

import org.opencv.core.Core;

import android.util.Log;

public class CaptureTimeResult extends MeasurementResult {
	
		double ReceptionMs;
		double PreProcessMs;
		double WaitingMs;
		double TakePictureMs;
		double PostProcessJPEGMs;
		double PostProcessPostJpegMs;
		double SendingJsonMs;
		double SendingJpegMs;
		double AllMs;
		double AllNoCommMs;	
		double DelayTakePicture;
		double DelayOnShutter;
		
	
	@Override
	public void WriteJSON() {
		// TODO Auto-generated method stub
		
	}
	
	/*final static String TAG = "TMEAS";
	
	public void WriteLog()
	{
		Log.i(TAG,"Reception: "+ReceptionMs);
		Log.i(TAG,"PreProcess: "+PreProcessMs);
		Log.i(TAG,"WaitingMs: "+WaitingMs);
		Log.i(TAG,"TakePicture: "+TakePictureMs);
		Log.i(TAG,"PostProcessJPEG: "+PostProcessJPEGMs);
		Log.i(TAG,"PostProcessPostJpeg: "+PostProcessPostJpegMs);
		Log.i(TAG,"SendingJson: "+SendingJsonMs);
		Log.i(TAG,"SendingJpeg: "+SendingJpegMs);
		Log.i(TAG,"All: "+AllMs);
		Log.i(TAG,"AllNoComm: "+AllNoCommMs);
		Log.i(TAG,"DelayTakePicture: "+DelayTakePicture);
		Log.i(TAG,"DelayOnShutter: "+DelayOnShutter);
	}*/
}
