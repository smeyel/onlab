package com.ol.research.photographer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;

import org.json.JSONException;
import org.json.JSONObject;


class CommsThread implements Runnable {
	Handler handler;
	Camera mCamera;
	PictureCallback mPicture;
	static OutputStream socket_os;
	static Socket s = null;
	ShutterCallback shutter;
	private static final String  TAG = "TMEAS";
	
	Calendar right_now;
//	Calendar last_midnight = MainActivity.last_midnight;
	long calendar_offset = MainActivity.calendar_offset;
	
	public CommsThread(Handler hand, Camera mCamera, PictureCallback mPicture, ShutterCallback shutter)
	{
		handler=hand;
		this.mCamera = mCamera;
		this.mPicture = mPicture;
		this.shutter = shutter;
	}
	
	public void run() {
        
		//offset measurement
		right_now = Calendar.getInstance();
		long actual_time = (right_now.getTimeInMillis() + calendar_offset) % (24 * 60 * 60 * 1000);
		//mCamera.takePicture(shutter, null, null);

		/*try {
			Thread.sleep(1000L);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		//long time_offset = MainActivity.millis_since_midnight - actual_time;
		
        try {
        		MainActivity.ss = new ServerSocket(MainActivity.SERVERPORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
      
        while (!Thread.currentThread().isInterrupted()) {
        	s = null;
        	Message m = new Message();
            InputStream is = null;
            OutputStream out = null;
            m.what = MainActivity.MSG_ID;
            try {          	 	
                    s = MainActivity.ss.accept();                   
                    is = s.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int ch;
                    while ((ch=is.read())!=-1) {
                    	bos.write(ch);
                    }
                   
               	String message = new String(bos.toByteArray());
               	
               	try {
               		JSONObject jObj = new JSONObject(message);
               		String type = jObj.getString("type");
               		int desired_timestamp = jObj.getInt("desiredtimestamp");
               		
    	    		right_now = Calendar.getInstance();
    	    		actual_time = (right_now.getTimeInMillis() + calendar_offset) % (24 * 60 * 60 * 1000);
    	            //long time_to_wait = desired_timestamp - (actual_time + time_offset);
    	    		
    	    		
               	if (type.equals("takepicture"))
               	{
               		//if(time_to_wait>0)
               		//{
               		//Thread.sleep(time_to_wait); //sleep-nél is ugyanúgy megjelenik a kb 300ms-os késés 
               		//}
               		if(desired_timestamp != 0 && desired_timestamp > actual_time)
               		{
               			//while(desired_timestamp >= (actual_time+time_offset))
               			while(desired_timestamp >= (actual_time)) //esetleges megoldás: offset kezelése -> (actual_time+300)
               			{
               				right_now = Calendar.getInstance();
               				actual_time = (right_now.getTimeInMillis() + calendar_offset) % (24 * 60 * 60 * 1000);
               			}
               		}
               		
               		
               //		MeasuredTimeValues.FrequBefore_takePicture = Core.getTickFrequency();
               		MeasuredTimeValues.CurrentTickCountBefore_takePicture = Core.getTickCount();
               		mCamera.takePicture(shutter, null, mPicture);
               		
               		
               		synchronized (s)
               		{
               			s.wait();
               		}             	    
               		
               		Log.i(TAG," getTickCount() before takePicture() == "+ MeasuredTimeValues.CurrentTickCountBefore_takePicture);
               		Log.i(TAG," getTickCount() right after beginning on onShutter() == "+ MeasuredTimeValues.CurrentTickCountIn_onShutter);
               		Log.i(TAG," getTickCount() right after beginning on onPictureTaken() == "+ MeasuredTimeValues.CurrentTickCountIn_onPictureTaken);
               		Log.i(TAG," getTickCount() right after image was sent == "+ MeasuredTimeValues.CurrentTickCountAfter_ImageSent);
               		
               		Log.i(TAG," delta between takePicture() and onShutter()  == "+ (MeasuredTimeValues.CurrentTickCountIn_onShutter-MeasuredTimeValues.CurrentTickCountBefore_takePicture));
               		Log.i(TAG," delta between takePicture() and onPictureTaken()  == "+ (MeasuredTimeValues.CurrentTickCountIn_onPictureTaken-MeasuredTimeValues.CurrentTickCountBefore_takePicture));
               		Log.i(TAG," delta between onShutter() and onPictureTaken()  == "+ (MeasuredTimeValues.CurrentTickCountIn_onPictureTaken - MeasuredTimeValues.CurrentTickCountIn_onShutter));
               		Log.i(TAG," delta between onPictureTaken() and after image was sent  == "+ (MeasuredTimeValues.CurrentTickCountAfter_ImageSent-MeasuredTimeValues.CurrentTickCountIn_onPictureTaken));
               		Log.i(TAG," delta between takePicture() and after image was sent  == "+ (MeasuredTimeValues.CurrentTickCountAfter_ImageSent-MeasuredTimeValues.CurrentTickCountBefore_takePicture));
               		
               		Log.i(TAG," Frequency before takePicture() == "+ MeasuredTimeValues.CurrentTickCountBefore_takePicture);
               		Log.i(TAG," Frequency in onShutter() == "+ MeasuredTimeValues.FrequIn_onShutter);
               		Log.i(TAG," Frequency in onPictureTaken() == "+ MeasuredTimeValues.FrequIn_onPictureTaken);
               		Log.i(TAG," Frequency in picture sending service == "+ MeasuredTimeValues.FrequAfter_ImageSent);
               		
               	} else if(message.equals("ping"))
               	{
               		out = s.getOutputStream();       
                    DataOutputStream output = new DataOutputStream(out);     
                    output.writeUTF("pong");
                    output.flush();
               	}
               	MainActivity.mClientMsg = message;
               	handler.sendMessage(m);
               	
            
               	} catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }   	
               	
            } catch (IOException e) {
                e.printStackTrace();
                
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
            	if (is != null) {
            		try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            	if (out != null) {
            		try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            }
        }
        
    }
}
