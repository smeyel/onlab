LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../../../android-opencv/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := native_sample
LOCAL_SRC_FILES := jni_part.cpp \
                   libMiscTimeAndConfig/src/ConfigManagerBase.cpp \
                   libMiscTimeAndConfig/src/TimeMeasurement.cpp \
                   libTwoColorCircleMarker/src/FastColorFilter.cpp \
                   libTwoColorCircleMarker/src/MarkerCC2.cpp \
                   libTwoColorCircleMarker/src/MarkerCC2Locator.cpp \
                   libTwoColorCircleMarker/src/MarkerCC2Tracker.cpp \
                   libTwoColorCircleMarker/src/TwoColorLocator.cpp \
                   miscLogConfig/src/ConfigReader.cpp \
                   miscLogConfig/src/Logger.cpp \
                   miscLogConfig/src/AndroidLogger.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
