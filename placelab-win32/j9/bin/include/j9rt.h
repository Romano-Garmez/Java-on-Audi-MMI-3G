/*
    (c) Copyright IBM Corp. 1991, 2003  All Rights Reserved

    File generated: (3/13/03 5:55:49 PM)
*/
#ifndef J9RT_H
#define J9RT_H

#ifdef __cplusplus
extern "C" {
#endif

#include "jni.h"

#define J9_FIRE_ASYNC_EVENT(javaVM, eventRef) (((jint (JNICALL *)(JavaVM * vm, jobject asyncEvent)) (((void **) (javaVM))[7]))(vm, eventRef))

#ifdef __cplusplus
}
#endif

#endif /* J9RT_H */
