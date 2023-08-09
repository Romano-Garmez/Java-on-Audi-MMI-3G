/*
*	(c) Copyright IBM Corp. 1991, 2003 All Rights Reserved
*/
#ifndef jvmpi_h
#define jvmpi_h

#include "jni.h"

/* JVMPI Constants */
#define JVMPI_VERSION_1 ((jint)0x10000001)

/* for compatability with older specs */
#define JVMPI_EVENT_LOAD_COMPILED_METHOD JVMPI_EVENT_COMPILED_METHOD_LOAD
#define JVMPI_EVENT_UNLOAD_COMPILED_METHOD JVMPI_EVENT_COMPILED_METHOD_UNLOAD

#define JVMPI_EVENT_METHOD_ENTRY		((jint)1)
#define JVMPI_EVENT_METHOD_ENTRY2		((jint)2)
#define JVMPI_EVENT_METHOD_EXIT		((jint)3)
#define JVMPI_EVENT_OBJECT_ALLOC		((jint)4)
#define JVMPI_EVENT_OBJECT_FREE		((jint)5)
#define JVMPI_EVENT_OBJECT_MOVE		((jint)6)
#define JVMPI_EVENT_COMPILED_METHOD_LOAD		((jint)7)
#define JVMPI_EVENT_COMPILED_METHOD_UNLOAD		((jint)8)
#define JVMPI_EVENT_UNUSED_9		((jint)9)
#define JVMPI_EVENT_UNUSED_10		((jint)10)
#define JVMPI_EVENT_UNUSED_11		((jint)11)
#define JVMPI_EVENT_UNUSED_12		((jint)12)
#define JVMPI_EVENT_UNUSED_13		((jint)13)
#define JVMPI_EVENT_UNUSED_14		((jint)14)
#define JVMPI_EVENT_UNUSED_15		((jint)15)
#define JVMPI_EVENT_UNUSED_16		((jint)16)
#define JVMPI_EVENT_UNUSED_17		((jint)17)
#define JVMPI_EVENT_UNUSED_18		((jint)18)
#define JVMPI_EVENT_UNUSED_19		((jint)19)
#define JVMPI_EVENT_UNUSED_20		((jint)20)
#define JVMPI_EVENT_UNUSED_21		((jint)21)
#define JVMPI_EVENT_UNUSED_22		((jint)22)
#define JVMPI_EVENT_UNUSED_23		((jint)23)
#define JVMPI_EVENT_UNUSED_24		((jint)24)
#define JVMPI_EVENT_UNUSED_25		((jint)25)
#define JVMPI_EVENT_UNUSED_26		((jint)26)
#define JVMPI_EVENT_UNUSED_27		((jint)27)
#define JVMPI_EVENT_UNUSED_28		((jint)28)
#define JVMPI_EVENT_UNUSED_29		((jint)29)
#define JVMPI_EVENT_UNUSED_30		((jint)30)
#define JVMPI_EVENT_UNUSED_31		((jint)31)
#define JVMPI_EVENT_UNUSED_32		((jint)32)
#define JVMPI_EVENT_THREAD_START		((jint)33)
#define JVMPI_EVENT_THREAD_END		((jint)34)
#define JVMPI_EVENT_CLASS_LOAD_HOOK		((jint)35)
#define JVMPI_EVENT_UNUSED_36		((jint)36)
#define JVMPI_EVENT_HEAP_DUMP		((jint)37)
#define JVMPI_EVENT_JNI_GLOBALREF_ALLOC		((jint)38)
#define JVMPI_EVENT_JNI_GLOBALREF_FREE		((jint)39)
#define JVMPI_EVENT_JNI_WEAK_GLOBALREF_ALLOC		((jint)40)
#define JVMPI_EVENT_JNI_WEAK_GLOBALREF_FREE		((jint)41)
#define JVMPI_EVENT_CLASS_LOAD		((jint)42)
#define JVMPI_EVENT_CLASS_UNLOAD		((jint)43)
#define JVMPI_EVENT_DATA_DUMP_REQUEST		((jint)44)
#define JVMPI_EVENT_DATA_RESET_REQUEST		((jint)45)
#define JVMPI_EVENT_JVM_INIT_DONE		((jint)46)
#define JVMPI_EVENT_JVM_SHUT_DOWN		((jint)47)
#define JVMPI_EVENT_ARENA_NEW		((jint)48)
#define JVMPI_EVENT_ARENA_DELETE		((jint)49)
#define JVMPI_EVENT_OBJECT_DUMP		((jint)50)
#define JVMPI_EVENT_RAW_MONITOR_CONTENDED_ENTER		((jint)51)
#define JVMPI_EVENT_RAW_MONITOR_CONTENDED_ENTERED		((jint)52)
#define JVMPI_EVENT_RAW_MONITOR_CONTENDED_EXIT		((jint)53)
#define JVMPI_EVENT_MONITOR_CONTENDED_ENTER		((jint)54)
#define JVMPI_EVENT_MONITOR_CONTENDED_ENTERED		((jint)55)
#define JVMPI_EVENT_MONITOR_CONTENDED_EXIT		((jint)56)
#define JVMPI_EVENT_MONITOR_WAIT		((jint)57)
#define JVMPI_EVENT_MONITOR_WAITED		((jint)58)
#define JVMPI_EVENT_MONITOR_DUMP		((jint)59)
#define JVMPI_EVENT_GC_START		((jint)60)
#define JVMPI_EVENT_GC_FINISH		((jint)61)

#define JVMPI_MAX_EVENT_TYPE_VAL		((jint)61)

#define JVMPI_REQUESTED_EVENT		((jint)0x10000000)

#define JVMPI_SUCCESS		((jint)0)
#define JVMPI_NOT_AVAILABLE		((jint)1)
#define JVMPI_FAIL		((jint)-1)

enum {
	JVMPI_THREAD_RUNNABLE = 1,
	JVMPI_THREAD_MONITOR_WAIT,
	JVMPI_THREAD_CONDVAR_WAIT
};

#define JVMPI_THREAD_SUSPENDED		0x8000
#define JVMPI_THREAD_INTERRUPTED		0x4000

#define JVMPI_MINIMUM_PRIORITY		1
#define JVMPI_MAXIMUM_PRIORITY		10
#define JVMPI_NORMAL_PRIORITY		5

#define JVMPI_NORMAL_OBJECT		((jint)0)
#define JVMPI_CLASS						((jint)2)
#define JVMPI_BOOLEAN					((jint)4)
#define JVMPI_CHAR						((jint)5)
#define JVMPI_FLOAT						((jint)6)
#define JVMPI_DOUBLE					((jint)7)
#define JVMPI_BYTE						((jint)8)
#define JVMPI_SHORT						((jint)9)
#define JVMPI_INT							((jint)10)
#define JVMPI_LONG						((jint)11)    

#define JVMPI_MONITOR_JAVA		0x01
#define JVMPI_MONITOR_RAW		0x02

#define JVMPI_GC_ROOT_UNKNOWN		0xff
#define JVMPI_GC_ROOT_JNI_GLOBAL		0x01
#define JVMPI_GC_ROOT_JNI_LOCAL		0x02
#define JVMPI_GC_ROOT_JAVA_FRAME		0x03
#define JVMPI_GC_ROOT_NATIVE_STACK		0x04
#define JVMPI_GC_ROOT_STICKY_CLASS		0x05
#define JVMPI_GC_ROOT_THREAD_BLOCK		0x06
#define JVMPI_GC_ROOT_MONITOR_USED		0x07
#define JVMPI_GC_ROOT_THREAD_OBJ		0x08

#define JVMPI_GC_CLASS_DUMP		0x20
#define JVMPI_GC_INSTANCE_DUMP		0x21 
#define JVMPI_GC_OBJ_ARRAY_DUMP		0x22
#define JVMPI_GC_PRIM_ARRAY_DUMP		0x23

#define JVMPI_DUMP_LEVEL_0		((jint)0)
#define JVMPI_DUMP_LEVEL_1		((jint)1)
#define JVMPI_DUMP_LEVEL_2		((jint)2)

/* jobjectID */
struct _jobjectID;
typedef struct _jobjectID *jobjectID;

/* JVMPI_RawMonitor */
struct _JVMPI_RawMonitor;
typedef struct _JVMPI_RawMonitor * JVMPI_RawMonitor;

/* JVMPI_CallFrame */
typedef struct {
	jint lineno;
	jmethodID method_id;
} JVMPI_CallFrame;

/* JVMPI_CallTrace */
typedef struct {
	JNIEnv *env_id;
	jint num_frames;
	JVMPI_CallFrame *frames;
} JVMPI_CallTrace;

/* JVMPI_Field */
typedef struct {
	char *field_name;
	char *field_signature;
} JVMPI_Field;

/* JVMPI_HeapDumpArg */
typedef struct {
	jint heap_dump_level;
} JVMPI_HeapDumpArg;

/* JVMPI_Lineno */
typedef struct {
	jint offset;
	jint lineno;
} JVMPI_Lineno;

/* JVMPI_Method */
typedef struct {
	char *method_name;
	char *method_signature;
	jint start_lineno;
	jint end_lineno;
	jmethodID method_id;
} JVMPI_Method;

/* JVMPI Event */
typedef struct {
	jint event_type;
	JNIEnv *env_id;

	union {
		struct {
			jint arena_id;
		} delete_arena;

		struct {
			jint arena_id;
			char *arena_name;
		} new_arena;
		
		struct {
			char *class_name;
			char *source_name;
			jint num_interfaces;
			jint num_methods;
			JVMPI_Method *methods;
			jint num_static_fields;
			JVMPI_Field *statics;
			jint num_instance_fields;
			JVMPI_Field *instances;
			jobjectID class_id;
		} class_load;

		struct {
			unsigned char *class_data;
			jint class_data_len;
			unsigned char *new_class_data;
			jint new_class_data_len;
			void * (*malloc_f)(unsigned int);
		} class_load_hook;

		struct {
			jobjectID class_id;
		} class_unload;

		struct {
			jmethodID method_id;
			void *code_addr;
			jint code_size;
			jint lineno_table_size;
			JVMPI_Lineno *lineno_table;
		} compiled_method_load;

		struct {
			jmethodID method_id;
		} compiled_method_unload;

		struct {
			jlong used_objects;
			jlong used_object_space;
			jlong total_object_space;
		} gc_info;

		struct {
			int dump_level;
			char *begin;
			char *end;
			jint num_traces;
			JVMPI_CallTrace *traces;
		} heap_dump;

		struct {
			jobjectID obj_id;
			jobject ref_id;
		} jni_globalref_alloc;

		struct {
			jobject ref_id;
		} jni_globalref_free;

		struct {
			jmethodID method_id;
		} method;

		struct {
			jmethodID method_id;
			jobjectID obj_id;
		} method_entry2;

		struct {
			jobjectID object;
		} monitor;

		struct {
			char *begin;
			char *end;
			jint num_traces;
			JVMPI_CallTrace *traces;
			jint *threads_status;
		} monitor_dump;

		struct {
			jobjectID object;
			jlong timeout;
		} monitor_wait;

		struct {
			jint arena_id;
			jobjectID class_id;
			jint is_array;
			jint size;
			jobjectID obj_id;
		} obj_alloc;

		struct {
			jint data_len;
			char *data;
		} object_dump;

		struct {
			jobjectID obj_id;
		} obj_free;

		struct {
			jint arena_id;
			jobjectID obj_id;
			jint new_arena_id;
			jobjectID new_obj_id;
		} obj_move;

		struct {
			char *name;
			JVMPI_RawMonitor id;
		} raw_monitor;

		struct {
			char *thread_name;
			char *group_name;
			char *parent_name;
			jobjectID thread_id;
			JNIEnv *thread_env_id;
		} thread_start;
	} u;
} JVMPI_Event;

/* interface functions */
typedef struct {
	jint version;	/* JVMPI version */

	/* interface implemented by the profiler */
	void (*NotifyEvent)(JVMPI_Event *event);

	/* interface implemented by the JVM */
	jint (*EnableEvent) (jint event_type, void *arg);
	jint (*DisableEvent) (jint event_type, void *arg);
	jint (*RequestEvent) (jint event_type, void *arg);

	void (*GetCallTrace) (JVMPI_CallTrace *trace, jint depth);

	void (*ProfilerExit) (jint);

	JVMPI_RawMonitor (*RawMonitorCreate) (char *lock_name);
	void (*RawMonitorEnter) (JVMPI_RawMonitor lock_id);
	void (*RawMonitorExit) (JVMPI_RawMonitor lock_id);
	void (*RawMonitorWait) (JVMPI_RawMonitor lock_id, jlong ms);
	void (*RawMonitorNotifyAll) (JVMPI_RawMonitor lock_id);
	void (*RawMonitorDestroy) (JVMPI_RawMonitor lock_id);

	jlong (*GetCurrentThreadCpuTime) (void);
	void (*SuspendThread) (JNIEnv *env);
	void (*ResumeThread) (JNIEnv *env);
	jint (*GetThreadStatus) (JNIEnv *env);
	jboolean (*ThreadHasRun) (JNIEnv *env);
	jint (*CreateSystemThread) (char *name, jint priority, void (*f)(void *));
	void (*SetThreadLocalStorage) (JNIEnv *env_id, void *ptr);
	void * (*GetThreadLocalStorage) (JNIEnv *env_id);

	void (*DisableGC) (void);
	void (*EnableGC) (void);
	void (*RunGC) (void);

	jobjectID (*GetThreadObject) (JNIEnv *env);
	jobjectID (*GetMethodClass) (jmethodID mid);
} JVMPI_Interface;

#endif     /* jvmpi_h */

