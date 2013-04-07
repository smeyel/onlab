package com.aut.smeyel;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.aut.smeyel.R;

public class MainActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener {
	
	private static final String TAG = "SMEyeL::MainActivity";
	private CameraBridgeViewBase mOpenCvCameraView;
	private Mat mRgba;
	private Mat mGray;
	
	private Mat mResult;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* OpenCV specific init, for example: enable camera view */
                    
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("native_sample");
                    
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    mOpenCvCameraView.enableView();
                    
//                    InitMarkerHandler(800, 480);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_start_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			break;
		}
		return true;
	}
	
	@Override
    public void onPause()
    {
		super.onPause();
		if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
		Release();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        Release();
    }

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		mResult = new Mat();
		InitMarkerHandler(width, height);
		
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
		mResult.release();
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
//		inputFrame.rgba().copyTo(mRgba);
//      Core.putText(mRgba, "OpenCV+Android", new Point(10, inputFrame.rgba().rows() - 10), 3, 1, new Scalar(255, 0, 0, 255), 2);
        
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        //FindCircles(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
        FastColor(mRgba.getNativeObjAddr(), mResult.getNativeObjAddr());
		return mResult;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public native void FindFeatures(long matAddrGr, long matAddrRgba);
	public native void FindCircles(long matAddrGr, long matAddrRgba);
	
	public native void InitMarkerHandler(int width, int height);
	public native void FastColor(long matAddrInput, long matAddrResult);
	public native void Release();

}
