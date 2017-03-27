//
// Created by chendongqi on 17-3-21.
//

#include "chendongqi_me_memfilltool_MemOpUtils.h"
#include "stdlib.h"
#include "stdio.h"
#include "string.h"
#include <unistd.h>
#include "android/log.h"
#include "android_runtime/AndroidRuntime.h"

#define MEMOP_LOG_TAG "libjnimemop"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, MEMOP_LOG_TAG, __VA_ARGS__)

#define PARAMINT 20
#define MAX_SIZE 200
bool reachMemoryValue = false;
static int *p[MAX_SIZE];

bool shouldStopMalloc() {
    LOGI("shouldStopMalloc reachMemoryValue = %d" + reachMemoryValue);
    return reachMemoryValue;
}

/*
 * Class:     chendongqi_me_memfilltool_MemOpUtils
 * Method:    memfill
 * Signature: ()I
 */

JNIEXPORT jint JNICALL Java_chendongqi_me_memfilltool_MemOpUtils_memfill
  (JNIEnv *, jclass) {
        int i;
        for(i = 0 ; i < MAX_SIZE && !shouldStopMalloc(); i++) {
            LOGI("malloc one time");
            p[i] = (int *) malloc(256 * 1024 * PARAMINT * sizeof(int));
            memset(p[i], 2, 1024 * 1024 * PARAMINT);
            sleep(1);
        }
        LOGI("malloc stop, malloced memory size is %d" + i*PARAMINT);
        return i * PARAMINT;
  }

/*
 * Class:     chendongqi_me_memfilltool_MemOpUtils
 * Method:    memfree
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_chendongqi_me_memfilltool_MemOpUtils_memfree
  (JNIEnv *, jclass){
        int i;
        for(i = 0;i < MAX_SIZE; i++) {
            free(p[i]);
            p[i] = NULL;
        }
        return i * PARAMINT;
  }

/*
 * Class:     chendongqi_me_memfilltool_MemOpUtils
 * Method:    setflag
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_chendongqi_me_memfilltool_MemOpUtils_setflag
  (JNIEnv *, jclass){
        LOGI("set stop malloc flag to true");
        reachMemoryValue = true;
        return 0;
  }

