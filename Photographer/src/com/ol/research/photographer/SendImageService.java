package com.ol.research.photographer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

public class SendImageService extends IntentService{
	
	
	
	public SendImageService() 
	{
		super("SendImageService");
	}
	
	@Override
	protected synchronized void onHandleIntent(Intent intent) 
	{
		try {
		OutputStream os = CommsThread.s.getOutputStream();
		
		String path = Environment.getExternalStorageDirectory().getPath()+"/custom_photos"+"/__1.jpg";
   		File myFile = new File (path);
        byte [] mybytearray  = new byte [(int)myFile.length()];
        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        System.out.println("Sending...");
        os.write(mybytearray,0,mybytearray.length);
        
        try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
        
        os.flush();
        //CommsThread.socket_flag = false;
        synchronized (CommsThread.s)
   		{
        	CommsThread.s.notifyAll();;
   		}
        
        
		} catch (IOException e) {
            e.printStackTrace();
            
		} 
		// Send response
		/*Intent respIntent = new Intent(MainActivity.KEY_REST_FILTER);
		respIntent.putExtra(MainActivity.KEY_REST_RESPONSE, result);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(respIntent);*/
	}
}
