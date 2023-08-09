/*
    (c) Copyright IBM Corp. 1991, 2003  All Rights Reserved

    File generated: (3/13/03 5:55:48 PM)
*/
#ifndef J9THREAD_H
#define J9THREAD_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stddef.h>
#include "j9comp.h"

typedef UDATA j9thread_tls_key_t;

#define J9THREAD_PROC VMCALL

typedef int(J9THREAD_PROC* j9thread_entrypoint_t)(void*);
typedef struct J9Thread *j9thread_t;
typedef struct J9ThreadMonitor *j9thread_monitor_t;

struct J9PortLibrary;

#define J9THREAD_FLAG_BLOCKED  1
#define J9THREAD_ALREADY_INITIALIZED  4
#define J9THREAD_TIMED_OUT  3
#define J9THREAD_FLAG_STARTED  0x800
#define J9THREAD_PRIORITY_NORMAL  5
#define J9THREAD_ILLEGAL_MONITOR_STATE  1
#define J9THREAD_FLAG_PRIORITY_INTERRUPTED  0x100
#define J9THREAD_FLAG_INTERRUPTED  4
#define J9THREAD_INVALID_ARGUMENT  7
#define J9THREAD_FLAG_DETACHED  0x80
#define J9THREAD_PRIORITY_USER_MIN  1
#define J9THREAD_PRIORITY_MIN  0
#define J9THREAD_PRIORITY_INTERRUPTED  5
#define J9THREAD_PRIORITY_MAX  11
#define J9THREAD_FLAG_CANCELED  0x400
#define J9THREAD_FLAG_NOTIFIED  16
#define J9THREAD_FLAG_ATTACHED  0x200
#define J9THREAD_FLAG_DEAD  32
#define J9THREAD_FLAG_WAITING  2
#define J9THREAD_PRIORITY_USER_MAX  10
#define J9THREAD_INTERRUPTED  2
#define J9THREAD_ALREADY_ATTACHED  6
#define J9THREAD_FLAG_SUSPENDED  8
#define J9THREAD_FLAG_SLEEPING  64

#define J9THREAD_MONITOR_SYSTEM  0
#define J9THREAD_MONITOR_INVALID  0x80000
#define J9THREAD_MONITOR_OBJECT  0x70000
#define J9THREAD_MONITOR_NO_REENTER_AFTER_WAIT  0x10000
#define J9THREAD_MONITOR_INTERRUPTABLE  0x20000
#define J9THREAD_MONITOR_PRIORITY_INTERRUPTABLE  0x40000



extern J9_CFUNC void VMCALL j9thread_detach PROTOTYPE((j9thread_t thread));
extern J9_CFUNC IDATA VMCALL j9thread_tls_alloc PROTOTYPE((j9thread_tls_key_t* handle));
extern J9_CFUNC IDATA VMCALL j9thread_sleep_interruptable PROTOTYPE((IDATA millis, IDATA nanos));
extern J9_CFUNC void  VMCALL j9thread_cancel PROTOTYPE((j9thread_t thread));
extern J9_CFUNC UDATA  VMCALL j9thread_clear_interrupted PROTOTYPE((void));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_enter PROTOTYPE((j9thread_monitor_t monitor));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_notify_all PROTOTYPE((j9thread_monitor_t monitor));
extern J9_CFUNC IDATA VMCALL j9thread_attach PROTOTYPE((j9thread_t * handle));
extern J9_CFUNC UDATA VMCALL j9thread_priority_interrupted PROTOTYPE((j9thread_t thread));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_destroy PROTOTYPE((j9thread_monitor_t monitor));
extern J9_CFUNC UDATA VMCALL j9thread_interrupted PROTOTYPE((j9thread_t thread));
#if (defined(J9VM_THR_TRACING)) /* priv. proto (autogen) */
extern J9_CFUNC void VMCALL j9thread_monitor_dump_trace PROTOTYPE((j9thread_monitor_t monitor));
#endif /* J9VM_THR_TRACING (autogen) */

extern J9_CFUNC void VMCALL j9thread_monitor_lock PROTOTYPE((j9thread_t self));
extern J9_CFUNC j9thread_monitor_t VMCALL j9thread_monitor_acquire PROTOTYPE((j9thread_t self));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_try_enter PROTOTYPE((j9thread_monitor_t monitor));
extern J9_CFUNC j9thread_t VMCALL j9thread_self PROTOTYPE((void));
extern J9_CFUNC UDATA  VMCALL j9thread_clear_priority_interrupted PROTOTYPE((void));
extern J9_CFUNC IDATA VMCALL j9thread_tls_free PROTOTYPE((j9thread_tls_key_t key));
extern J9_CFUNC UDATA VMCALL j9thread_monitor_exit_all PROTOTYPE((j9thread_monitor_t monitor));
extern J9_CFUNC void VMCALL j9thread_monitor_unlock PROTOTYPE((j9thread_t self));
#if (defined(J9VM_THR_TRACING)) /* priv. proto (autogen) */
extern J9_CFUNC IDATA VMCALL j9thread_monitor_init_tracing PROTOTYPE((j9thread_monitor_t* handle, UDATA flags, char* name));
#endif /* J9VM_THR_TRACING (autogen) */

extern J9_CFUNC void VMCALL j9thread_yield PROTOTYPE((void));
extern J9_CFUNC void VMCALL j9thread_suspend PROTOTYPE((void));
extern J9_CFUNC void VMCALL j9thread_interrupt PROTOTYPE((j9thread_t thread));
extern J9_CFUNC void* VMCALL j9thread_tls_get PROTOTYPE((j9thread_t thread, j9thread_tls_key_t key));
extern J9_CFUNC IDATA VMCALL j9thread_create PROTOTYPE((j9thread_t* handle, UDATA stacksize, UDATA priority, UDATA suspend, j9thread_entrypoint_t entrypoint, void* entryarg));
extern J9_CFUNC IDATA VMCALL j9thread_tls_set PROTOTYPE((j9thread_t thread, j9thread_tls_key_t key, void* value));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_wait PROTOTYPE((j9thread_monitor_t monitor));
#if (defined(J9VM_THR_TRACING)) /* priv. proto (autogen) */
extern J9_CFUNC void VMCALL j9thread_dump_trace PROTOTYPE((j9thread_t thread));
#endif /* J9VM_THR_TRACING (autogen) */

extern J9_CFUNC IDATA VMCALL j9thread_monitor_exit PROTOTYPE((j9thread_monitor_t monitor));
extern J9_CFUNC UDATA VMCALL j9thread_get_priority PROTOTYPE((j9thread_t thread));
extern J9_CFUNC IDATA VMCALL j9thread_set_priority PROTOTYPE((j9thread_t thread, UDATA priority));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_init PROTOTYPE((j9thread_monitor_t* handle, UDATA flags));
extern J9_CFUNC IDATA VMCALL j9thread_sleep PROTOTYPE((IDATA millis));
#if (defined(J9VM_THR_TRACING)) /* priv. proto (autogen) */
extern J9_CFUNC void VMCALL j9thread_monitor_dump_all PROTOTYPE((void));
#endif /* J9VM_THR_TRACING (autogen) */

extern J9_CFUNC UDATA* VMCALL j9thread_global PROTOTYPE((char* name));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_exit_no_mutex PROTOTYPE((j9thread_monitor_t monitor, j9thread_t self));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_wait_timed PROTOTYPE((j9thread_monitor_t monitor, IDATA millis, IDATA nanos));
extern J9_CFUNC void VMCALL j9thread_resume PROTOTYPE((j9thread_t thread));
extern J9_CFUNC void VMCALL j9thread_priority_interrupt PROTOTYPE((j9thread_t thread));
extern J9_CFUNC IDATA VMCALL j9thread_destroy PROTOTYPE((j9thread_t thread));
extern J9_CFUNC void VMCALL NORETURN j9thread_exit PROTOTYPE((j9thread_monitor_t monitor));
extern J9_CFUNC IDATA VMCALL j9thread_monitor_notify PROTOTYPE((j9thread_monitor_t monitor));
#if (defined(J9VM_INTERP_VERBOSE))  || (defined(J9VM_PROF_PROFILING)) /* priv. proto (autogen) */
extern J9_CFUNC void VMCALL j9thread_enable_stack_usage PROTOTYPE((UDATA enable));
#endif /* J9VM_('INTERP_VERBOSE' 'PROF_PROFILING') (autogen) */

#if (defined(J9VM_PROF_PROFILING)) /* priv. proto (autogen) */
extern J9_CFUNC UDATA VMCALL j9thread_get_flags PROTOTYPE((j9thread_t thread, j9thread_monitor_t* blocker));
#endif /* J9VM_PROF_PROFILING (autogen) */

#if (defined(J9VM_PROF_PROFILING)) /* priv. proto (autogen) */
extern J9_CFUNC UDATA VMCALL j9thread_get_handle PROTOTYPE((j9thread_t thread));
#endif /* J9VM_PROF_PROFILING (autogen) */

#if (defined(J9VM_PROF_PROFILING)) /* priv. proto (autogen) */
extern J9_CFUNC I_64 VMCALL j9thread_get_cpu_time PROTOTYPE((j9thread_t thread));
#endif /* J9VM_PROF_PROFILING (autogen) */

#if (defined(J9VM_INTERP_VERBOSE))  || (defined(J9VM_PROF_PROFILING)) /* priv. proto (autogen) */
extern J9_CFUNC UDATA VMCALL j9thread_get_stack_usage PROTOTYPE((j9thread_t thread));
#endif /* J9VM_('INTERP_VERBOSE' 'PROF_PROFILING') (autogen) */

#if (defined(J9VM_PROF_PROFILING)) /* priv. proto (autogen) */
extern J9_CFUNC UDATA VMCALL j9thread_get_stack_size PROTOTYPE((j9thread_t thread));
#endif /* J9VM_PROF_PROFILING (autogen) */

#if (defined(J9VM_PROF_PROFILING)) /* priv. proto (autogen) */
extern J9_CFUNC IDATA VMCALL j9thread_get_os_priority PROTOTYPE((j9thread_t thread, IDATA* policy, IDATA *priority));
#endif /* J9VM_PROF_PROFILING (autogen) */


#ifdef J9VM_THR_TRACING
#define j9thread_monitor_init(handle, flags) j9thread_monitor_init_tracing(handle, flags, #handle)
#endif

#define j9thread_global_monitor() (*(j9thread_monitor_t*)j9thread_global("global_monitor"))

#ifdef __cplusplus
}
#endif

#endif /* J9THREAD_H */
