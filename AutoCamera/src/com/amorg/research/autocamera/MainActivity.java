package com.amorg.research.autocamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	Camera mCamera;
	CameraPreview mPreview;
	Button takePictureBtn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        takePictureBtn = (Button) findViewById(R.id.button_capture);
        
        try{
        	mCamera = Camera.open();
        	
        	mPreview = new CameraPreview(this, mCamera);
        	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        	preview.addView(mPreview);
        	
        } catch (Exception e) {
			// TODO rendes hibakezeles!
        	Log.v("AutoCamera", "Camera is not available!");
        	
		}
        
        
        takePictureBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCamera.takePicture(null, null, mPicture);
			}
		});
        
        
        
    }
    
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	
            String pictureFile = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+"/__"+Math.round(Math.random()*10)+".jpg";
            
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("AutoCamera", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("AutoCamera", "Error accessing file: " + e.getMessage());
            }
            Log.v("AutoCamera", "Picture saved at path: " + pictureFile);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    
}
