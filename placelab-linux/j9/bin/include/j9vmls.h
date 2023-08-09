/*
    (c) Copyright IBM Corp. 1991, 2003  All Rights Reserved

    File generated: (3/13/03 5:54:45 PM)
*/
#ifndef J9VMLS_H
#define J9VMLS_H

#ifdef __cplusplus
extern "C" {
#endif

#include "j9comp.h"
#include "j9cfg.h"
#include "jni.h"

#define J9VMLS_MAX_KEYS 256

typedef struct J9VMLSFunctionTable {
    UDATA  (JNICALL *J9VMLSAllocKeys) (JNIEnv * env, UDATA * pInitCount, ...) ;
    void  (JNICALL *J9VMLSFreeKeys) (JNIEnv * env, UDATA * pInitCount, ...) ;
    void*  (JNICALL *J9VMLSGet) (JNIEnv * env, void * key) ;
    void*  (JNICALL *J9VMLSSet) (JNIEnv * env, void ** pKey, void * value) ;
} J9VMLSFunctionTable;

#define J9SIZEOF_J9VMLSFunctionTable 16
#define J9VMLS_FNTBL(env) ((J9VMLSFunctionTable *) ((((void ***) (env))[9])[4]))

#ifdef J9VM_OPT_MULTI_VM
#define J9VMLS_GET(env, key) (J9VMLS_FNTBL(env)->J9VMLSGet(env, (key)))
#define J9VMLS_SET(env, key, value) (J9VMLS_FNTBL(env)->J9VMLSSet(env, &(key), (void *) (value)))
#else
#define J9VMLS_GET(env, key) (key)
#define J9VMLS_SET(env, key, value) ((key) = (void *) (value))
#endif

#ifdef __cplusplus
}
#endif

#endif /* J9VMLS_H */
