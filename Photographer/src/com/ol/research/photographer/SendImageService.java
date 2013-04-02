package com.ol.research.photographer;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
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
		byte [] mybytearray = intent.getByteArrayExtra("BYTE_ARRAY");
		long timestamp = intent.getLongExtra("TIMESTAMP", 0);
        String buff = Integer.toString(mybytearray.length);
        String JSON_message = new String("{\"type\":\"JPEG\",\"size\":\"");
        JSON_message = JSON_message.concat(buff);
        JSON_message = JSON_message.concat("\",\"timestamp\":\"");
        JSON_message = JSON_message.concat(Long.toString(timestamp));
        JSON_message = JSON_message.concat("\"}#");
        
        System.out.println("Sending...");    
        DataOutputStream output = new DataOutputStream(os);     
        output.writeUTF(JSON_message);
        output.flush();
        
        os.write(mybytearray,0,mybytearray.length);
        
        /*try {
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
              
        os.flush();
        //CommsThread.socket_flag = false;
        synchronized (CommsThread.s)
   		{
        	CommsThread.s.notifyAll();
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
