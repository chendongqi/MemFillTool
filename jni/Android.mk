LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_LDLIBS :=-llog

LOCAL_SRC_FILES:= \
	chendongqi_me_memfilltool_MemOpUtils.cpp

LOCAL_C_INCLUDES += \
    $(JNI_H_INCLUDE) \

LOCAL_SHARED_LIBRARIES := \
    libutils \
    libcutils \
    libc
  
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= libjnimemop
include $(BUILD_SHARED_LIBRARY)
