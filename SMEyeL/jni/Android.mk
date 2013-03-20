LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../../../android-opencv/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := native_sample
LOCAL_SRC_FILES := jni_part.cpp libTwoColorCircleMarker/src/FastColorFilter.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
