#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

#include "FastColorFilter.h"
#include "MarkerCC2Tracker.h"
#include "DetectionResultExporterBase.h"
#include "TimeMeasurementCodeDefines.h"
#include "ConfigManagerBase.h"
#include "AndroidLogger.h"

#define LOG_TAG "SMEyeL"

using namespace std;
using namespace cv;
using namespace TwoColorCircleMarker;
using namespace Logging;

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
//		LOGD("aaaaaamarkerfound");
//		stream << endl;
		int valid = marker->isCenterValid ? 1 : 0;
		Logger::log(Logger::LOGLEVEL_INFO, LOG_TAG, "Position: %f %f Valid: %d\n", marker->center.x, marker->center.y, valid);
	}
};

class MyConfigManager : public MiscTimeAndConfig::ConfigManagerBase
{
	// This method is called by init of the base class to read the configuration values.
	virtual bool readConfiguration(CSimpleIniA *ini)
	{
		resizeImage = ini->GetBoolValue("Main","resizeImage",false,NULL);
		showInputImage = ini->GetBoolValue("Main","showInputImage",false,NULL);
		verboseColorCodedFrame = ini->GetBoolValue("Main","verboseColorCodedFrame",false,NULL);
		verboseOverlapMask = ini->GetBoolValue("Main","verboseOverlapMask",false,NULL);
		waitFor25Fps = ini->GetBoolValue("Main","waitFor25Fps",false,NULL);
		pauseIfNoValidMarkers = ini->GetBoolValue("Main","pauseIfNoValidMarkers",false,NULL);
		waitKeyPressAtEnd = ini->GetBoolValue("Main","waitKeyPressAtEnd",false,NULL);
		runMultipleIterations = ini->GetBoolValue("Main","runMultipleIterations",false,NULL);
		return true;
	}

public:
	// --- Settings
	bool resizeImage;
	bool pauseIfNoValidMarkers;
	bool verboseOverlapMask;
	bool verboseColorCodedFrame;
	bool showInputImage;
	bool waitFor25Fps;
	bool waitKeyPressAtEnd;
	bool runMultipleIterations;
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
TwoColorCircleMarker::MarkerCC2Tracker* tracker = NULL;
ResultExporter resultExporter;

MyConfigManager configManager;
char *configfilename = "/sdcard/testini2.ini";

AndroidLogger* logger = NULL;



JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_Init(JNIEnv*, jobject, jint width, jint height);

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_Init(JNIEnv*, jobject, jint width, jint height)
{
	logger = new AndroidLogger();
	Logger::registerLogger(*logger);
//	Logger::log(Logger::LOGLEVEL_ERROR, LOG_TAG, "Szam:%d %d %s %d\n", 1, 2, "Hello", 3);

	configManager.init(configfilename);
//	int ii = configManager.runMultipleIterations ? 1 : 0;
//	int iii = configManager.waitKeyPressAtEnd ? 1 : 0;


	if(tracker == NULL) {
		tracker = new TwoColorCircleMarker::MarkerCC2Tracker();
		tracker->setResultExporter(&resultExporter);
		tracker->init(configfilename, true, width, height); // ez sokszor meghivodik (minden resume-kor), memoriaszivargasra figyelni
	}





}

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FastColor(JNIEnv*, jobject, jlong addrInput, jlong addrResult);

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_FastColor(JNIEnv*, jobject, jlong addrInput, jlong addrResult)
{
	Mat& mInput  = *(Mat*)addrInput;
	Mat& mResult = *(Mat*)addrResult;

	Mat mInputBgr;
	cvtColor(mInput, mInputBgr, COLOR_RGBA2BGR);

	tracker->processFrame(mInputBgr,0,-1.0f);

//	Mat* mOut = tracker->visColorCodeFrame;
//	cvtColor(*mOut, mResult, COLOR_BGR2RGBA);
	cvtColor(mInputBgr, mResult, COLOR_BGR2RGBA);

//	Logger::log(Logger::LOGLEVEL_INFO, LOG_TAG, "ProcessAll: %f ms", tracker->timeMeasurement->getavgms(TimeMeasurementCodeDefs::ProcessAll));
//	Logger::log(Logger::LOGLEVEL_INFO, LOG_TAG, "FastColorFilter: %f ms", tracker->timeMeasurement->getavgms(TimeMeasurementCodeDefs::FastColorFilter));
//	Logger::log(Logger::LOGLEVEL_INFO, LOG_TAG, "VisualizeDecomposedImage: %f ms", tracker->timeMeasurement->getavgms(TimeMeasurementCodeDefs::VisualizeDecomposedImage));
//	Logger::log(Logger::LOGLEVEL_INFO, LOG_TAG, "TwoColorLocator: %f ms", tracker->timeMeasurement->getavgms(TimeMeasurementCodeDefs::TwoColorLocator));
//	Logger::log(Logger::LOGLEVEL_INFO, LOG_TAG, "LocateMarkers: %f ms", tracker->timeMeasurement->getavgms(TimeMeasurementCodeDefs::LocateMarkers));
//	Logger::log(Logger::LOGLEVEL_INFO, LOG_TAG, "---------------------");

}

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_Release(JNIEnv*, jobject);

JNIEXPORT void JNICALL Java_com_aut_smeyel_MainActivity_Release(JNIEnv*, jobject)
{
	if(tracker != NULL) {
		delete tracker;
		tracker = NULL;
	}

	if(logger != NULL) {
		delete logger;
		logger = NULL;
	}

}
}
