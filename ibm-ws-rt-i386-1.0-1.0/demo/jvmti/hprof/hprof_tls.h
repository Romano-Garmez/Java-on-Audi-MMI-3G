/*
 * @(#)src/demo/jvmti/hprof/hprof_tls.h, dsdev, dsdev, 20060202 1.3
 * ===========================================================================
 * Licensed Materials - Property of IBM
 * "Restricted Materials of IBM"
 *
 * IBM SDK, Java(tm) 2 Technology Edition, v5.0
 * (C) Copyright IBM Corp. 1998, 2005. All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or disclosure
 * restricted by GSA ADP Schedule Contract with IBM Corp.
 * ===========================================================================
 */

/*
 * ===========================================================================
 (C) Copyright Sun Microsystems Inc, 1992, 2004. All rights reserved.
 * ===========================================================================
 */
/*
 * @(#)hprof_tls.h	1.15 04/07/27
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

#ifndef HPROF_TLS_H
#define HPROF_TLS_H

void         tls_init(void);
TlsIndex     tls_find_or_create(JNIEnv *env, jthread thread);
void         tls_agent_thread(JNIEnv *env, jthread thread);
SerialNumber tls_get_thread_serial_number(TlsIndex index);
void         tls_list(void);
void         tls_delete_global_references(JNIEnv *env);
void         tls_cleanup(void);
void         tls_free(JNIEnv *env, TlsIndex index);
void         tls_sample_all_threads(void);

MonitorIndex tls_get_monitor(TlsIndex index);
void         tls_set_monitor(TlsIndex index, MonitorIndex monitor_index);

void         tls_set_thread_object_index(TlsIndex index,
			ObjectIndex thread_object_index);

jint         tls_get_tracker_status(JNIEnv *env, jthread thread,
			jboolean skip_init, jint **ppstatus, TlsIndex* pindex,
		        SerialNumber *pthread_serial_num,
			TraceIndex *ptrace_index);

void         tls_set_sample_status(ObjectIndex object_index, jint sample_status);
jint         tls_sum_sample_status(void);

void         tls_dump_traces(JNIEnv *env);
void         tls_output_heap_threads(void);

void         tls_monitor_start_timer(TlsIndex index);
jlong        tls_monitor_stop_timer(TlsIndex index);

void         tls_dump_monitor_state(JNIEnv *env);

void         tls_push_method(TlsIndex index, jmethodID method);
void         tls_pop_method(TlsIndex index, jmethodID method);
void         tls_pop_exception_catch(TlsIndex index, jmethodID method);

TraceIndex   tls_get_trace(TlsIndex index, int depth, jboolean skip_init);

#endif
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

#ifndef HPROF_TLS_H
#define HPROF_TLS_H

void         tls_init(void);
TlsIndex     tls_find_or_create(JNIEnv *env, jthread thread);
void         tls_agent_thread(JNIEnv *env, jthread thread);
SerialNumber tls_get_thread_serial_number(TlsIndex index);
void         tls_list(void);
void         tls_delete_global_references(JNIEnv *env);
void         tls_cleanup(void);
void         tls_free(JNIEnv *env, TlsIndex index);
void         tls_sample_all_threads(void);

MonitorIndex tls_get_monitor(TlsIndex index);
void         tls_set_monitor(TlsIndex index, MonitorIndex monitor_index);

void         tls_set_thread_object_index(TlsIndex index,
			ObjectIndex thread_object_index);

jint         tls_get_tracker_status(JNIEnv *env, jthread thread,
			jboolean skip_init, jint **ppstatus, TlsIndex* pindex,
		        SerialNumber *pthread_serial_num,
			TraceIndex *ptrace_index);

void         tls_set_sample_status(ObjectIndex object_index, jint sample_status);
jint         tls_sum_sample_status(void);

void         tls_dump_traces(JNIEnv *env);
void         tls_output_heap_threads(void);

void         tls_monitor_start_timer(TlsIndex index);
jlong        tls_monitor_stop_timer(TlsIndex index);

void         tls_dump_monitor_state(JNIEnv *env);

void         tls_push_method(TlsIndex index, jmethodID method);
void         tls_pop_method(TlsIndex index, jmethodID method);
void         tls_pop_exception_catch(TlsIndex index, jmethodID method);

TraceIndex   tls_get_trace(TlsIndex index, int depth, jboolean skip_init);

#endif
