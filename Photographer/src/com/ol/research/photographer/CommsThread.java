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
	//ServerSocket ss = null;
	static OutputStream socket_os;
	//static boolean socket_flag = true;
	static Socket s = null;
	ShutterCallback shutter;
	
	public CommsThread(Handler hand, Camera mCamera, PictureCallback mPicture, ShutterCallback shutter)
	{
		handler=hand;
		this.mCamera = mCamera;
		this.mPicture = mPicture;
		this.shutter = shutter;
		//this.ss = ss;
	}
	
	public void run() {
        
        //ServerSocket ss = null;
    	//s=null;
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
                //if (s == null)
            	 	
                    s = MainActivity.ss.accept();
               // BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
              //  String st = null;
              //  st = input.readLine();
                    
                    is = s.getInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int ch;
                    while ((ch=is.read())!=-1) {
                    	bos.write(ch);
                    }
                    
                    
                //ide jön a válasz
             //  try {
			//	Thread.sleep(3000L);
		//	} catch (InterruptedException e) {
		//		e.printStackTrace();
		//} 
           //    out = s.getOutputStream();       
          //     DataOutputStream output = new DataOutputStream(out);     
             //  output.writeChars("Hello PC!");
            //   output.flush();
               
               	String message = new String(bos.toByteArray());
               	
               	try {
               		JSONObject jObj = new JSONObject(message);
               		//JSONArray types = jObj.getJSONArray("type");
               		String type = jObj.getString("type");
               		int desired_timestamp = jObj.getInt("desiredtimestamp");
               		
    	    		Calendar rightNow = Calendar.getInstance();               
    	            long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
    	            long sinceMidnight = (rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000);
    	            String current_time = String.valueOf(sinceMidnight);
    	            int actual_time = Integer.parseInt(current_time.toString());
    	           /* long time_to_wait;
    	            
    	            if(desired_timestamp != 0)
    	            {
    	            	time_to_wait = Integer.parseInt(current_time.toString()) - desired_timestamp;
    	            }
    	            else
    	            {
    	            	time_to_wait = 0;
    	            }*/
               	
               	
               	
               	if (type.equals("takepicture"))
               	{
               		//Thread.sleep(time_to_wait);
               		if(desired_timestamp != 0 && desired_timestamp > actual_time)
               		{
               			while(desired_timestamp >= actual_time)
               			{
               				rightNow = Calendar.getInstance(); 
               				offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
            	            sinceMidnight = (rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000);
            	            current_time = String.valueOf(sinceMidnight);
            	            actual_time = Integer.parseInt(current_time.toString());
            	            
            	            /*Message mes = new Message();
            	            mes.what = 0x1339;
            	            mes.arg1 = actual_time;
            	            handler.sendMessage(mes);*/
               			}
               		}
               		mCamera.takePicture(shutter, null, mPicture);
               		//socket_os = s.getOutputStream();
               		//Thread.sleep(2000L);
               		
               		synchronized (s)
               		{
               			s.wait();
               		}
               		/*while(socket_flag == true)
               		{
               			Thread.sleep(200L);
               		}*/
               		
               		/*String path = Environment.getExternalStorageDirectory().getPath()+"/custom_photos"+"/__1.jpg";
               		File myFile = new File (path);
                    byte [] mybytearray  = new byte [(int)myFile.length()];
                    FileInputStream fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(mybytearray,0,mybytearray.length);
                    OutputStream os = s.getOutputStream();
                    System.out.println("Sending...");
                    os.write(mybytearray,0,mybytearray.length);
                    os.flush();*/
               	    
               		
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
