#ifndef __TWOCOLORLOCATOR_H_
#define __TWOCOLORLOCATOR_H_

#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/core/mat.hpp>
#include "FastColorFilter.h"
//#include "ConfigManagerBase.h"

using namespace cv;

namespace TwoColorCircleMarker
{
	/** Uses the results of the FastColorFilter (which has generated a list of candidate marker rectangles)
			to check for real marker locations. For every candidate rectangle, it starts to scan for required
			colors in 4 directions starting from the center. If the colors indicate false detection (cannot be a marker),
			the candidate is omitted. Otherwise, the rectangle size is updated based on the scans in the 4 directions.
		Additionally, overlapping candidate areas are removed.
		Use this class after FastColorFilter and before MarkerCC2Locator to filter and improve the initial detection results.

		To use it,
		- call init()
		- optionally, set verboseImage
		- for every frame, call consolidateFastColorFilterRects() to process the data, and
		- see resultRectangles for the results.
	*/
	class TwoColorLocator
	{
//		// Internal configuration class
//		class ConfigManager : public MiscTimeAndConfig::ConfigManagerBase
//		{
//			// This method is called by init of the base class to read the configuration values.
//			virtual bool readConfiguration(CSimpleIniA *ini);
//
//		public:
//			// Verbose marker localization
//			bool verboseRectConsolidationCandidates;
//			bool verboseRectConsolidationResults;
//			bool verboseTxt_RectConsolidation;
//			bool verboseTxt_RectConsolidationSummary;
//		};
//
//		ConfigManager configManager;

	public:
		/** Constructor
		*/
		TwoColorLocator();

		/** Initialize using the provided config file.
		*/
		void init(char *configFileName);

		/** Call this to process the candidates generated by FastColorFilter.
			@param candidateRects	Pointer to the list of candidate rectangles.
			@param candidateRectNum	Number of candidate rectangles in the array.
			@param srcCC	Color code image (type CV_8UC1).
		*/
		void consolidateFastColorFilterRects(Rect* candidateRects, int candidateRectNum, Mat &srcCC);

		/** Storage of consolidated results. Filled by consolidateFastColorFilterRects().
		*/
		std::list<Rect> resultRectangles;

		/** Image for verbose visualization. Set to a BGR image to see verbose information.
			Verbose functions can be enabled from the configuration file.
		*/
		Mat *verboseImage;
	private:
		/** Updates the size of a rectangle by scanning the image from the center in
			4 directions and finding the borders.
			Used by consolidateFastColorFilterRects() internally.
		*/
		bool updateRectToRealSize(Mat &srcCC, Rect &newRect, Mat *verboseImage);

		/** Goes along a line with a given (inner) color and returns the distance of the "border color" pixels. -1 indicates invalid situation.
		*/
		int findColorAlongLine(Mat &srcCC, Point startPoint, Point endPoint, uchar innerColorCode, uchar borderColorCode,Mat *verboseImage);
	};
}

#endif
