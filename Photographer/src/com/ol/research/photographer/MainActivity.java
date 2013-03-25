package com.ol.research.photographer;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	 Camera mCamera;
	
	 static ServerSocket ss = null;
	 static String mClientMsg = "";
	 Thread myCommsThread = null;
	 String current_time = null;
	 protected static final int MSG_ID = 0x1337;
	 public static final int SERVERPORT = 6000;
	 protected static final int TIME_ID = 0x1338;
	 
	 private PictureCallback mPicture = new PictureCallback() {

	        @Override
	        public void onPictureTaken(byte[] data, Camera camera) {
	        	String pictureFile = Environment.getExternalStorageDirectory().getPath()+"/custom_photos"+"/__1.jpg";
	            try {
	                FileOutputStream fos = new FileOutputStream(pictureFile);
	                fos.write(data);
	                fos.close();   
	                
	            } catch (FileNotFoundException e) {
	                Log.d("Photographer", "File not found: " + e.getMessage());
	            } catch (IOException e) {
	                Log.d("Photographer", "Error accessing file: " + e.getMessage());
	            }
	            Log.v("Photographer", "Picture saved at path: " + pictureFile);
	            
	            Intent intent = new Intent(MainActivity.this, SendImageService.class);
				//intent.putExtra(pictureFile, false);
				startService(intent);            	            
	        }
	    };
	    
	    private ShutterCallback shutter = new ShutterCallback()
	    {
	    	@Override
	    	public void onShutter()
	    	{
	    		Calendar rightNow = Calendar.getInstance();
	               
	            long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
	            long sinceMidnight = (rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000);
	            current_time = String.valueOf(sinceMidnight); 
	    		
	    		
	    		
	    		Message m = new Message();
	            m.what = TIME_ID;
	            myUpdateHandler.sendMessage(m);
	    	}
	    };
	    
	   
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		final Button btnHttpGet = (Button) findViewById(R.id.btnHttpGet);
		btnHttpGet.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick (View arg0) {
				new Thread() {
					public void run() {
						httpReg();
					}
				}.start();
			}
		});
		
		//take-picture button
		/*final Button captureButton = (Button) findViewById(R.id.buttonPhoto);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mCamera.takePicture(null, null, mPicture);
						//captureButton.setEnabled(false);
					}
				});	*/
		
		mCamera = Camera.open();

// eredeti elõnézet
//		mPreview = new CameraPreview(this, mCamera);
//		preview=(FrameLayout) findViewById(R.id.cameraPreview);
//		preview.addView(mPreview);
		
		// az elõnézeti képet így nem látjuk
		SurfaceView mview = new SurfaceView(getBaseContext());
		try {
			mCamera.setPreviewDisplay(mview.getHolder());
			mCamera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//thread a socket üzenetek számára
		myCommsThread = new Thread(new CommsThread(myUpdateHandler, mCamera, mPicture, shutter));
		myCommsThread.start();	
	}

	@Override
	protected void onStop(){
		if(mCamera != null)
			{
				mCamera.release();
			}
		super.onStop();
		myCommsThread.interrupt(); //a socketkapcsolatra várakozó thread-et hogyan érdemes kezelni?
		
		try {
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	  //Handler a socketüzenet számára
 	Handler myUpdateHandler = new Handler() {
 		@Override
 	    public void handleMessage(Message msg) {
 	        switch (msg.what) {
 	        case MSG_ID:
 	            TextView tv = (TextView) findViewById(R.id.TextView_time);
 	            tv.setText(mClientMsg);
 	            break;
 	       case TIME_ID:
	        	TextView tv2 = (TextView) findViewById(R.id.TextView_receivedme);
	        	tv2.setText(current_time);
	        	break;
 	        default:
 	        	
 	            break;
 	        }
 	        super.handleMessage(msg);
 	    }
 	   };
	
	
	
	public String getLocalIpAddress() {
		try {
			for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			//Log.e(LOG_TAG, ex.toString());
		}
		return null;
	} 
	
	private void httpReg()
	{
		AndroidHttpClient httpClient = null;
		String IpAddress = new String(getLocalIpAddress());
		try{
			httpClient = AndroidHttpClient.newInstance("Android");
			HttpGet httpGet = new HttpGet("http://avalon.aut.bme.hu/~kristof/smeyel/smeyel_reg.php?IP="+IpAddress);
			final String response = httpClient.execute(httpGet, new BasicResponseHandler());
			runOnUiThread(new Runnable() {
				@Override
				public void run()
				{
					Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
				}
			});
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally{
			if (httpClient != null)
				httpClient.close();
		}
	}
}
