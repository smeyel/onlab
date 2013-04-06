package com.ol.research.photographer;

import android.util.Log;

/**
 * Temporal storage of tick counts for time measurement.
 * Used to collect data during picture taking and to avoid
 * 	LogCat operations during time measurement.
 * 
 * Events:
 * 	ConnectionReceived:	received connection, no data received yet
 *  CommandReceived:	command entirely received, no execution yet
 *  TakingPicture:		just before issuing takePicture
 *  OnShutterEvent:		onShutter event
 *  OnPictureTakenEvent:	onPictureTaken event
 *  OnSendingResponse:	just starting to send the response
 *  OnResponseSent:		just finished sending the response
 *  
 *  Warning, TickFrequency may change! TODO: check it at every GetTickCount() ?
 */
public class TempTickCountStorage {
	static public long ConnectionReceived;
	static public long CommandReceived;
	static public long TakingPicture;
	static public long OnShutterEvent;
	static public long OnPictureTakenEvent;
	static public long OnSendingResponse;
	static public long OnResponseSent;

	static public double TickFrequency;
	
	final static String TAG = "TMEAS";

	public static void WriteToLog() {
		Log.i(TAG,"TickFrequency: "+TickFrequency);
		Log.i(TAG,"ConnectionReceived: "+ConnectionReceived);
		Log.i(TAG,"CommandReceived: "+CommandReceived);
		Log.i(TAG,"TakingPicture: "+TakingPicture);
		Log.i(TAG,"OnShutterEvent: "+OnShutterEvent);
		Log.i(TAG,"OnPictureTakenEvent: "+OnPictureTakenEvent);
		Log.i(TAG,"OnSendingResponse: "+OnSendingResponse);
		Log.i(TAG,"OnResponseSent: "+OnResponseSent);
		
		double ReceptionMs = (double)(CommandReceived - ConnectionReceived)/TickFrequency;
		double PreProcessMs = (double)(TakingPicture - CommandReceived)/TickFrequency;
		double TakePictureMs = (double)(OnShutterEvent - TakingPicture)/TickFrequency;
		double PostProcessJPEGMs = (double)(OnPictureTakenEvent - OnShutterEvent)/TickFrequency;
		double PostProcessPostJpegMs = (double)(OnSendingResponse - OnPictureTakenEvent)/TickFrequency;
		double SendingMs = (double)(OnResponseSent - OnSendingResponse)/TickFrequency;
		double AllMs = (double)(OnResponseSent - ConnectionReceived)/TickFrequency;
		double AllNoCommMs = (double)(OnSendingResponse - CommandReceived)/TickFrequency;
		
		Log.i(TAG,"ReceptionMs: "+ReceptionMs);
		Log.i(TAG,"PreProcessMs: "+PreProcessMs);
		Log.i(TAG,"TakePictureMs: "+TakePictureMs);
		Log.i(TAG,"PostProcessJPEGMs: "+PostProcessJPEGMs);
		Log.i(TAG,"PostProcessPostJpegMs: "+PostProcessPostJpegMs);
		Log.i(TAG,"SendingMs: "+SendingMs);
		Log.i(TAG,"AllMs: "+AllMs);
		Log.i(TAG,"AllNoCommMs: "+AllNoCommMs);
		
		Log.i(TAG,"CSV;"+ReceptionMs+";"+PreProcessMs+";"
				+TakePictureMs+";"+PostProcessJPEGMs+";"+PostProcessPostJpegMs+";"
				+SendingMs+";"+AllMs+";"+AllNoCommMs);
	}
}
