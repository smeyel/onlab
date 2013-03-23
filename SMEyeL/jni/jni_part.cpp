#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

#include <android/log.h>
#include "libTwoColorCircleMarker/include/FastColorFilter.h"
#include "libTwoColorCircleMarker/include/MarkerCC2Tracker.h"
#include "libTwoColorCircleMarker/include/DetectionResultExporterBase.h"

#define LOG_TAG "SMEyeL"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;
using namespace TwoColorCircleMarker;

const char* stringToCharStar(string str) {
	const char *cstr = str.c_str();
	return cstr;
}

string intToString(int i) {
	stringstream ss;
	ss << i;
	string str = ss.str();
	return str;
}

const char* intToCharStar(int i) {
	return stringToCharStar(intToString(i));
}

class ResultExporter : public TwoColorCircleMarker::DetectionResultExporterBase
{
//	ofstream stream;
public:
	void open(char *filename)
	{
//		stream.open(filename);
	}

	void close()
	{
//		stream.flush();
//		stream.close();
	}

	int currentFrameIdx;
	int currentCamID;

	virtual void writeResult(MarkerBase *marker)
	{
//		stream << "FID:" << currentFrameIdx << ",CID:" << currentCamID << " ";
//		marker->exportToTextStream(&stream);
		LOGD("aaaaaamarkerfound");
		std::cout << "eeeeeeeee";
//		stream << endl;
	}
};


class MarkerHandler {
private:
	Mat *defaultColorCodeFrame;
	Mat *defaultOverlapMask;
	Mat *defaultVisColorCodeFrame;
public:
	Mat *colorCodeFrame;
	Mat *overlapMask;
	Mat *visColorCodeFrame;

	FastColorFilter fastColorFilter;
	MarkerHandler(){
		defaultColorCodeFrame = NULL;
		defaultOverlapMask = NULL;
		defaultVisColorCodeFrame = NULL;
	}
	~MarkerHandler(){

		if (defaultColorCodeFrame != NULL)
		{
			if (defaultColorCodeFrame == colorCodeFrame)
			{
				colorCodeFrame = NULL;
			}
			delete defaultColorCodeFrame;
			defaultColorCodeFrame = NULL;
		}
		if (defaultOverlapMask != NULL)
		{
			if (defaultOverlapMask == overlapMask)
			{
				overlapMask = NULL;
			}
			delete defaultOverlapMask;
			defaultOverlapMask = NULL;
		}
		if (defaultVisColorCodeFrame != NULL)
		{
			if (defaultVisColorCodeFrame == visColorCodeFrame)
			{
				visColorCodeFrame = NULL;
			}
			delete defaultVisColorCodeFrame;
			defaultVisColorCodeFrame = NULL;
		}

	}
	void init(int width, int height){
		defaultColorCodeFrame = new Mat(height, width,CV_8UC1);
		defaultOverlapMask = new Mat(height, width,CV_8UC1);
		defaultVisColorCodeFrame = new Mat(height, width,CV_8UC3);

		colorCodeFrame = defaultColorCodeFrame;
		overlapMask = defaultOverlapMask;
		visColorCodeFrame = defaultVisColorCodeFrame;

		fastColorFilter.maskColorCode[0]=COLORCODE_RED;
		fastColorFilter.maskColorCode[1]=COLORCODE_BLU;
		fastColorFilter.overlapMask=overlapMask;
		fastColorFilter.backgroundColorCode=COLORCODE_WHT;
	}
	void processFrame(Mat &src){
		fastColorFilter.FindMarkerCandidates(src,*colorCodeFrame);

		fastColorFilter.VisualizeDecomposedImage(*colorCodeFrame,*visColorCodeFrame);
	}
};

extern "C" {
JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    FastFeatureDetector detector(50);
    detector.detect(mGr, v);
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
}

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FindCircles(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FindCircles(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;


    // COLOR FILTERING
//    Mat mHsv;
//    cvtColor(mRgb, mHsv, COLOR_RGB2HSV, 3);
//
//    Mat mHSVThreshed;
//    inRange(mHsv, Scalar(0, 100, 30), Scalar(5, 255, 255), mHSVThreshed);
//
//    Mat rgba;
//    cvtColor(mHSVThreshed, mRgb, COLOR_GRAY2RGBA, 0);


    // DETECTING CIRCLES
    // Reduce the noise so we avoid false circle detection
    GaussianBlur( mGr, mGr, Size(9, 9), 2, 2 );

    vector<Vec3f> circles;
    HoughCircles(mGr, circles, CV_HOUGH_GRADIENT, 2, mGr.rows/4, 200, 100 );

    for( int i = 0; i < circles.size(); i++ )
    {
    	Point center(cvRound((double)circles[i][0]), cvRound((double)circles[i][1]));
    	int radius = cvRound((double)circles[i][2]);
    	// draw the circle center
    	circle( mRgb, center, 3, Scalar(0,255,0), -1, 8, 0 );
    	// draw the circle outline
    	circle( mRgb, center, radius, Scalar(0,0,255), 3, 8, 0 );
    }

}

//MarkerHandler markerHandler;
TwoColorCircleMarker::MarkerCC2Tracker tracker;
ResultExporter resultExporter;



JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_InitMarkerHandler(JNIEnv*, jobject, jint width, jint height);

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_InitMarkerHandler(JNIEnv*, jobject, jint width, jint height)
{
	//markerHandler.init(width, height);
	tracker.setResultExporter(&resultExporter);
	tracker.init("",true,width,height);


}

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FastColor(JNIEnv*, jobject, jlong addrInput, jlong addrResult);

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FastColor(JNIEnv*, jobject, jlong addrInput, jlong addrResult)
{
	Mat& mInput  = *(Mat*)addrInput;
	Mat& mResult = *(Mat*)addrResult;

	Mat mInputBgr;
	cvtColor(mInput, mInputBgr, COLOR_RGBA2BGR);

//	markerHandler.processFrame(mInputBgr);
	tracker.processFrame(mInputBgr,0,-1.0f);

//	Mat* mOut = markerHandler.visColorCodeFrame;
	Mat* mOut = tracker.visColorCodeFrame;
	cvtColor(*mOut, mResult, COLOR_BGR2RGBA);

}
}
