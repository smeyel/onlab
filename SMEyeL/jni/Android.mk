LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../../../android-opencv/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := native_sample
FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
FILE_LIST += $(wildcard $(LOCAL_PATH)/libMiscTimeAndConfig/src/*.cpp)
FILE_LIST += $(wildcard $(LOCAL_PATH)/libTwoColorCircleMarker/src/*.cpp)
FILE_LIST += $(wildcard $(LOCAL_PATH)/miscLogConfig/src/*.cpp)
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_C_INCLUDES += $(LOCAL_PATH) \
					$(LOCAL_PATH)/libMiscTimeAndConfig/include/ \
					$(LOCAL_PATH)/libTwoColorCircleMarker/include/ \
					$(LOCAL_PATH)/miscLogConfig/include/
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
