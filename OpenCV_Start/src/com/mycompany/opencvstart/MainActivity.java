package com.mycompany.opencvstart;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import com.mycompany.opencvstart.R;

public class MainActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener {
	
	private static final String TAG = "Start::OpenCV_Start::Activity";
	private CameraBridgeViewBase mOpenCvCameraView;
	private Mat mRgba;
	private Mat mGray;
	
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
    public void onPause()
    {
		if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
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
    }

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
//		inputFrame.rgba().copyTo(mRgba);
//      Core.putText(mRgba, "OpenCV+Android", new Point(10, inputFrame.rgba().rows() - 10), 3, 1, new Scalar(255, 0, 0, 255), 2);
        
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        FindFeatures(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr());
		return mRgba;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public native void FindFeatures(long matAddrGr, long matAddrRgba);

}
