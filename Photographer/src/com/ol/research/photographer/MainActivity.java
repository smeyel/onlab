package com.ol.research.photographer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Camera mCamera;
	
	 ServerSocket ss = null;
	// Socket s = null;
	 String mClientMsg = "";
	 Thread myCommsThread = null;
	 String current_time = null;
	 protected static final int MSG_ID = 0x1337;
	 protected static final int TIME_ID = 0x1338;
	 public static final int SERVERPORT = 6000;
		
	private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	
        	//String pictureFile = Environment.getExternalStorageDirectory().getPath()+"/custom_photos"+"/__"+Math.round(Math.random()*10)+".jpg";
        	String pictureFile = Environment.getExternalStorageDirectory().getPath()+"/custom_photos"+"/__1.jpg";
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                
               /* Calendar rightNow = Calendar.getInstance();
               
	             long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
	             long sinceMidnight = (rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000);
	             current_time = String.valueOf(sinceMidnight); */
                
            } catch (FileNotFoundException e) {
                Log.d("Photographer", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Photographer", "Error accessing file: " + e.getMessage());
            }
            Log.v("Photographer", "Picture saved at path: " + pictureFile);
            
            Message m = new Message();
            m.what = TIME_ID;
            myUpdateHandler.sendMessage(m);
            
        }
    };
	
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//TextView tv = (TextView) findViewById(R.id.TextView01);
	   // tv.setText("Nothing from client yet");
		
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
		this.myCommsThread = new Thread(new CommsThread());
		this.myCommsThread.start();
		
	}
	
	//Handler a socketüzenet számára
	Handler myUpdateHandler = new Handler() {
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        case MSG_ID:
	            TextView tv = (TextView) findViewById(R.id.TextView01);
	            tv.setText(mClientMsg);
	            break;
	        case TIME_ID:
	        	TextView tv2 = (TextView) findViewById(R.id.TextView01);
	        	tv2.setText(current_time);
	        	break;
	        default:
	        	
	            break;
	        }
	        super.handleMessage(msg);
	    }
	   };
	   
	   class CommsThread implements Runnable {
		    public void run() {
		        Socket s = null;
		        //ServerSocket ss = null;
		    	//s=null;
		        try {
		            ss = new ServerSocket(SERVERPORT);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        while (!Thread.currentThread().isInterrupted()) {
		            Message m = new Message();
		            InputStream is = null;
		            OutputStream out = null;
		            m.what = MSG_ID;
		            try {
		                //if (s == null)
		            	 	
		                    s = ss.accept();
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
		               		mCamera.takePicture(null, null, mPicture);
		               		
		               		Thread.sleep(2000L);
		               		String path = Environment.getExternalStorageDirectory().getPath()+"/custom_photos"+"/__1.jpg";
		               		File myFile = new File (path);
		                    byte [] mybytearray  = new byte [(int)myFile.length()];
		                    FileInputStream fis = new FileInputStream(myFile);
		                    BufferedInputStream bis = new BufferedInputStream(fis);
		                    bis.read(mybytearray,0,mybytearray.length);
		                    OutputStream os = s.getOutputStream();
		                    System.out.println("Sending...");
		                    os.write(mybytearray,0,mybytearray.length);
		                    os.flush();
		               		
		               		
		               	}
		                mClientMsg = message;
		                myUpdateHandler.sendMessage(m);
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
	
	@Override
	protected void onStop(){
		if(mCamera != null)
			{
				mCamera.release();
			}
		super.onStop();
		myCommsThread.interrupt(); //a socketkapcsolatra várakozó thread-et hogyan érdemes kezelni?
		try {
	        // make sure you close the socket upon exiting
	        ss.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		super.onStop();
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/
	

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
