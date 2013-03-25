package com.ol.research.photographer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.Message;

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
               	
               	
               	if (message.equals("p") )
               	{
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
               	    
               		
               	}
               	
               	if(message.equals("ping"))
               	{
               		out = s.getOutputStream();       
                    DataOutputStream output = new DataOutputStream(out);     
                    output.writeUTF("pong");
                    output.flush();
               	}
               	MainActivity.mClientMsg = message;
               	handler.sendMessage(m);
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
