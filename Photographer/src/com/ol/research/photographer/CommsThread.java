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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class CommsThread implements Runnable {
	Handler handler;
	Camera mCamera;
	PictureCallback mPicture;
	static OutputStream socket_os;
	static Socket s = null;
	ShutterCallback shutter;
	
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
		//long actual_time = right_now.getTimeInMillis() - last_midnight.getTimeInMillis();
		long actual_time = (right_now.getTimeInMillis() + calendar_offset) % (24 * 60 * 60 * 1000);
		mCamera.takePicture(shutter, null, null);
		/*long[] timearray = new long[10];
		for(int i=0; i<5; i++)
		{
			mCamera.takePicture(shutter, null, null);
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			timearray[i]=MainActivity.millis_since_midnight;
		}*/
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long time_offset = MainActivity.millis_since_midnight - actual_time;
		
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
               		/*if(time_to_wait>0)
               		{
               		Thread.sleep(time_to_wait); //sleep-nél is ugyanúgy megjelenik a kb 300ms-os késés 
               		}*/
               		if(desired_timestamp != 0 && desired_timestamp > actual_time)
               		{
               			while(desired_timestamp >= (actual_time+time_offset)) //esetleges megoldás: offset kezelése -> (actual_time+300)
               			{
               				right_now = Calendar.getInstance();
               				actual_time = (right_now.getTimeInMillis() + calendar_offset) % (24 * 60 * 60 * 1000);
               			}
               		}
               		mCamera.takePicture(shutter, null, mPicture);
               		
               		synchronized (s)
               		{
               			s.wait();
               		}             	    
               		
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
			} /*finally {
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
            }*/
        }
        
    }
}
