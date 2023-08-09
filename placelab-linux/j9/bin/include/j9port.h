/*
    (c) Copyright IBM Corp. 1991, 2003  All Rights Reserved

    File generated: (3/13/03 5:54:45 PM)
*/
#ifndef portlibrarydefines_h
#define portlibrarydefines_h

#include <stdarg.h>	/* for va_list */
#if defined(J9OSE)
#include <unistd.h>
#endif
#include "j9comp.h"
#include "j9cfg.h"
#include "j9thread.h"
#include "j9socket.h"
#include "gp.h"	/* for typedefs of function arguments to gp functions */

#define PORT_ACCESS_FROM_VMC(vmContext) J9PortLibrary *privatePortLibrary = (vmContext)->javaVM->portLibrary
#define PORT_ACCESS_FROM_JAVAVM(javaVM) J9PortLibrary *privatePortLibrary = (javaVM)->portLibrary
#define PORT_ACCESS_FROM_GINFO(javaVM) J9PortLibrary *privatePortLibrary = (javaVM)->portLibrary
#define PORT_ACCESS_FROM_PORT(portLibrary) J9PortLibrary *privatePortLibrary = (portLibrary)

#define PORTLIB privatePortLibrary

/* required for j9file */
#ifdef BREW
#include "AEEFile.h"
#define	EsSeekSet	_SEEK_START
#define	EsSeekCur	_SEEK_CURRENT
#define	EsSeekEnd	_SEEK_END
#else
#ifdef		SEEK_SET
#define	EsSeekSet	SEEK_SET	/* Values for EsFileSeek */
#else
#define	EsSeekSet	0
#endif
#ifdef 		SEEK_CUR
#define	EsSeekCur	SEEK_CUR
#else
#define	EsSeekCur	1
#endif
#ifdef		SEEK_END
#define	EsSeekEnd	SEEK_END
#else
#define	EsSeekEnd	2
#endif
#endif

#define	EsOpenRead		1	/* Values for EsFileOpen */
#define	EsOpenWrite		2
#define	EsOpenCreate	4
#define	EsOpenTruncate	8
#define	EsOpenAppend	16

#define EsIsDir 	0	/* Return values for EsFileAttr */
#define EsIsFile 	1

/** EsMaxPath was chosen from unix MAXPATHLEN.  Override in platform
  * specific j9file implementations if needed.
  */
#define EsMaxPath 	1024


struct J9PortLibrary ; /* Forward struct declaration */
struct J9PortLibrary; /* Forward struct declaration */
typedef struct J9PortLibrary {
    struct ESPortPlatformGlobals* portPlatformGlobals;
    I_32  (VMCALL *port_shutdown_library) ( struct J9PortLibrary*portLib ) ;
    U_32  (VMCALL *time_msec_clock) (struct J9PortLibrary *portlib) ;
    U_32  (VMCALL *time_usec_clock) (struct J9PortLibrary *portlib) ;
    void  (VMCALL *time_current_time_millis) (struct J9PortLibrary *portlib, I_64 *time_millis) ;
    U_32  (VMCALL *sysinfo_get_pid) (struct J9PortLibrary *portlib) ;
    UDATA  (VMCALL *sysinfo_get_physical_memory) (struct J9PortLibrary *portlib) ;
    I_32  (VMCALL *sysinfo_get_OS_version) (struct J9PortLibrary *portlib, char *infoString) ;
    I_32  (VMCALL *sysinfo_get_env) (struct J9PortLibrary *portLibrary, char *envVar, char *infoString, U_32 bufSize) ;
    I_32  (VMCALL *sysinfo_get_CPU_architecture) (struct J9PortLibrary *portlib, char *infoString) ;
    I_32  (VMCALL *sysinfo_get_OS_type) (struct J9PortLibrary *portlib, char *infoString) ;
    U_16  (VMCALL *sysinfo_get_classpathSeparator) (struct J9PortLibrary *portlib) ;
    IDATA  (VMCALL *sysinfo_get_executable_name) (struct J9PortLibrary *portlib, char *argv0, char **result) ;
    UDATA  (VMCALL *sysinfo_get_number_CPUs) (struct J9PortLibrary *portlib) ;
    I_32  (VMCALL *file_write) (struct J9PortLibrary *portLib, I_32 fd, void * buf, I_32 nbytes) ;
    I_32  (VMCALL *file_open) (struct J9PortLibrary *portLib, char * path, I_32 flags, I_32 mode) ;
    I_32  (VMCALL *file_close) (struct J9PortLibrary *portLib, I_32 fd) ;
    I_32  (VMCALL *file_seek) (struct J9PortLibrary *portLib, I_32 fd, I_32 offset, I_32 whence) ;
    I_32  (VMCALL *file_read) (struct J9PortLibrary *portLib, I_32 fd, void * buf, I_32 nbytes) ;
    I_32  (VMCALL *file_unlink) (struct J9PortLibrary *portLib, char * path) ;
    I_32  (VMCALL *file_attr) (struct J9PortLibrary *portLib, char * path) ;
    I_64  (VMCALL *file_lastmod) (struct J9PortLibrary *portLib, char * path) ;
    I_32  (VMCALL *file_length) (struct J9PortLibrary *portLib, char * path) ;
    I_32  (VMCALL *file_mkdir) (struct J9PortLibrary *portLib, char * path) ;
    I_32  (VMCALL *file_move) (struct J9PortLibrary *portLib, char * pathExist, char * pathNew) ;
    I_32  (VMCALL *file_unlinkdir) (struct J9PortLibrary *portLib, char * path) ;
    UDATA  (VMCALL *file_findfirst) (struct J9PortLibrary *portLib, char * path, char * resultbuf) ;
    I_32  (VMCALL *file_findnext) (struct J9PortLibrary *portLib,  UDATA findhandle, char * resultbuf) ;
    void  (VMCALL *file_findclose) (struct J9PortLibrary *portLib, UDATA findhandle) ;
    I_32  (VMCALL *file_sync) (struct J9PortLibrary *portLib, I_32 fd) ;
    UDATA  (VMCALL *sl_close_shared_library) (struct J9PortLibrary *portLib, UDATA descriptor) ;
    UDATA  (VMCALL *sl_open_shared_library) (struct J9PortLibrary *portLib, char *name, UDATA *descriptor, char* errorBuffer, UDATA bufferLength) ;
    UDATA  (VMCALL *sl_map_name) (struct J9PortLibrary *portLib, char *baseName, char *result, UDATA resultLength) ;
    UDATA  (VMCALL *sl_open) (struct J9PortLibrary *portLib, char *path, UDATA *descriptor) ;
    UDATA  (VMCALL *sl_close) (struct J9PortLibrary *portLib, UDATA descriptor) ;
    UDATA  (VMCALL *sl_lookup_name) (struct J9PortLibrary *portLib, UDATA descriptor, char *name, UDATA *func, UDATA sendArgs) ;
    IDATA  (VMCALL *tty_initialize) (struct J9PortLibrary *portLib) ;
    void  (VMCALL *tty_shutdown) (struct J9PortLibrary *portLib) ;
    void  (VMCALL *tty_printf) (struct J9PortLibrary *portLib, char *format, ...) ;
    IDATA  (VMCALL *tty_get_chars) (struct J9PortLibrary *portLib, char * s, UDATA length) ;
    IDATA  (VMCALL *tty_available) (struct J9PortLibrary *portLib) ;
    void  (VMCALL *tty_output_chars) (struct J9PortLibrary *portLib, char * s, UDATA length) ;
    void  (VMCALL *tty_err_printf) (struct J9PortLibrary *portLib, char *format, ...) ;
    void  (VMCALL *tty_err_output_chars) (struct J9PortLibrary *portLib, char * s, UDATA length) ;
    void*  (VMCALL *mem_allocate_code_memory) (struct J9PortLibrary *portLib, U_32 byteAmount) ;
    void*  (VMCALL *mem_allocate_memory) (struct J9PortLibrary *portLib, U_32 byteAmount) ;
    void  (VMCALL *mem_free_code_memory) (struct J9PortLibrary *portLib, void * memoryPointer) ;
    void  (VMCALL *mem_free_memory) (struct J9PortLibrary *portLib, void * memoryPointer) ;
    U_32  (VMCALL *mem_total_physical_memory) (struct J9PortLibrary *portLib) ;
    void*  (VMCALL *vmem_commit_memory) (struct J9PortLibrary *portLib, void *address, U_32 byteAmount) ;
    I_32  (VMCALL *vmem_decommit_memory) (struct J9PortLibrary *portLib, void *address, U_32 byteAmount) ;
    I_32  (VMCALL *vmem_free_memory) (struct J9PortLibrary *portLib, void *address) ;
    void*  (VMCALL *vmem_reserve_memory) (struct J9PortLibrary *portLib, void *address, U_32 byteAmount) ;
    U_16  (VMCALL *sock_htons) ( struct J9PortLibrary *portlib, U_16 val ) ;
    I_32  (VMCALL *sock_htonl) ( struct J9PortLibrary *portlib, I_32 val ) ;
    I_32  (VMCALL *sock_bind) ( struct J9PortLibrary *portlib, j9socket_t sock, j9sockaddr_t addr ) ;
    I_32  (VMCALL *sock_write) ( struct J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte ) ;
    I_32  (VMCALL *sock_sockaddr) ( struct J9PortLibrary *portlib, j9sockaddr_t *handle, char *address, U_16 port) ;
    I_32  (VMCALL *sock_freeaddr) ( struct J9PortLibrary *portlib, j9sockaddr_t handle) ;
    I_32  (VMCALL *sock_read) ( struct J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte, I_32 flags ) ;
    I_32  (VMCALL *sock_startup) (struct J9PortLibrary *portlib) ;
    I_32  (VMCALL *sock_socket) ( struct J9PortLibrary *portlib, j9socket_t *handle, I_32 family, I_32 t,  I_32 protocol) ;
    I_32  (VMCALL *sock_accept) ( struct J9PortLibrary *portlib, j9socket_t serverSock, j9sockaddr_t addrHandle, j9socket_t *sockHandle ) ;
    I_32  (VMCALL *sock_close) ( struct J9PortLibrary *portlib, j9socket_t sock ) ;
    I_32  (VMCALL *sock_shutdown_input) ( struct J9PortLibrary *portlib, j9socket_t sock ) ;
    I_32  (VMCALL *sock_shutdown_output) ( struct J9PortLibrary *portlib, j9socket_t sock ) ;
    I_32  (VMCALL *sock_shutdown) (struct J9PortLibrary *portlib) ;
    I_32  (VMCALL *sock_listen) ( struct J9PortLibrary *portlib, j9socket_t sock, I_32 backlog ) ;
    I_32  (VMCALL *sock_ntohl) ( struct J9PortLibrary *portlib, I_32 val ) ;
    U_16  (VMCALL *sock_ntohs) ( struct J9PortLibrary *portlib, U_16 val ) ;
    I_32  (VMCALL *sock_connect) ( struct J9PortLibrary *portlib, j9socket_t sock, j9sockaddr_t addr ) ;
    I_32  (VMCALL *sock_getsockname) ( struct J9PortLibrary *portlib, j9socket_t handle, j9sockaddr_t addrHandle ) ;
    I_32  (VMCALL *sock_getpeername) ( struct J9PortLibrary *portlib, j9socket_t handle, j9sockaddr_t addrHandle ) ;
    I_32  (VMCALL *sock_readfrom) ( struct J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte, I_32 flags, j9sockaddr_t addrHandle ) ;
    I_32  (VMCALL *sock_select) (struct J9PortLibrary *portlib, I_32 nfds, j9fdset_t readfds, j9fdset_t writefds, j9fdset_t exceptfds, j9timeval_t timeout) ;
    I_32  (VMCALL *sock_writeto) ( struct J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte, I_32 flags, j9sockaddr_t addrHandle ) ;
    I_32  (VMCALL *sock_inetntoa) ( struct J9PortLibrary *portlib, char **addrStr, U_32 nipAddr ) ;
    I_32  (VMCALL *sock_inetaddr) ( struct J9PortLibrary *portlib, char *addrStr, U_32 *addr ) ;
    I_32  (VMCALL *sock_gethostbyaddr) ( struct J9PortLibrary *portlib, char *addr, I_32 length, I_32 type, j9hostent_t hostent ) ;
    I_32  (VMCALL *sock_gethostbyname) ( struct J9PortLibrary *portlib, char *name, j9hostent_t hostent ) ;
    I_32  (VMCALL *sock_gethostname) ( struct J9PortLibrary *portlib, char *buffer, I_32 length ) ;
    I_32  (VMCALL *sock_hostent_addrlist) ( struct J9PortLibrary *portlib, j9hostent_t handle, U_32 index) ;
    I_32  (VMCALL *sock_hostent_aliaslist) ( struct J9PortLibrary *portlib, j9hostent_t handle, char ***aliasList) ;
    I_32  (VMCALL *sock_hostent_alloc) ( struct J9PortLibrary *portlib, j9hostent_t *handle) ;
    I_32  (VMCALL *sock_hostent_free) ( struct J9PortLibrary *portlib, j9hostent_t handle) ;
    I_32  (VMCALL *sock_hostent_hostname) ( struct J9PortLibrary *portlib, j9hostent_t handle, char** hostName) ;
    I_32  (VMCALL *sock_sockaddr_create) ( struct J9PortLibrary *portlib, j9sockaddr_t *handle, I_16 family, U_32 nipAddr, U_16 nPort ) ;
    I_16  (VMCALL *sock_sockaddr_port) ( struct J9PortLibrary *portlib, j9sockaddr_t handle) ;
    I_32  (VMCALL *sock_sockaddr_address) ( struct J9PortLibrary *portlib, j9sockaddr_t handle) ;
    I_32  (VMCALL *sock_fdset_create) ( struct J9PortLibrary *portlib, j9socket_t socketP, j9fdset_t *fdsetP ) ;
    I_32  (VMCALL *sock_fdset_free) ( struct J9PortLibrary *portlib, j9fdset_t fdsetP ) ;
    I_32  (VMCALL *sock_fdset_size) ( struct J9PortLibrary *portlib, j9socket_t handle ) ;
    I_32  (VMCALL *sock_timeval_create) ( struct J9PortLibrary *portlib, U_32 secTime, U_32 uSecTime, j9timeval_t *timeP ) ;
    I_32  (VMCALL *sock_timeval_free) ( struct J9PortLibrary *portlib,  j9timeval_t timeP) ;
    I_32  (VMCALL *sock_linger_create) ( struct J9PortLibrary *portlib, j9linger_t *handle, I_32 enabled, U_16 timeout) ;
    I_32  (VMCALL *sock_linger_free) ( struct J9PortLibrary *portlib, j9linger_t handle ) ;
    I_32  (VMCALL *sock_getopt_int) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  I_32 *optval) ;
    I_32  (VMCALL *sock_setopt_int) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  I_32 *optval) ;
    I_32  (VMCALL *sock_getopt_bool) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  BOOLEAN *optval) ;
    I_32  (VMCALL *sock_setopt_bool) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  BOOLEAN *optval) ;
    I_32  (VMCALL *sock_getopt_byte) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  U_8 *optval) ;
    I_32  (VMCALL *sock_setopt_byte) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  U_8 *optval) ;
    I_32  (VMCALL *sock_getopt_linger) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  j9linger_t optval) ;
    I_32  (VMCALL *sock_setopt_linger) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  j9linger_t optval) ;
    I_32  (VMCALL *sock_getopt_sockaddr) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname, j9sockaddr_t optval) ;
    I_32  (VMCALL *sock_setopt_sockaddr) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname, j9sockaddr_t optval) ;
    I_32  (VMCALL *sock_setopt_ipmreq) ( struct J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  j9ipmreq_t optval) ;
    I_32  (VMCALL *sock_linger_enabled) ( struct J9PortLibrary *portlib, j9linger_t handle, BOOLEAN *enabled) ;
    I_32  (VMCALL *sock_linger_linger) ( struct J9PortLibrary *portlib, j9linger_t handle, U_16 *linger) ;
    I_32  (VMCALL *sock_ipmreq_create) ( struct J9PortLibrary *portlib, j9ipmreq_t *handle, U_32 nipmcast, U_32 nipinterface) ;
    I_32  (VMCALL *sock_ipmreq_free) ( struct J9PortLibrary *portlib, j9ipmreq_t ipmreqP ) ;
    I_32  (VMCALL *sock_setflag_read) ( struct J9PortLibrary *portlib, I_32 flag, I_32 *arg ) ;
    UDATA  (VMCALL *gp_protect) (struct J9PortLibrary *portLib, protected_fn, void* arg) ;
    void  (VMCALL *gp_register_handler) (struct J9PortLibrary *portLib, handler_fn, void * aUserData) ;
    void  (VMCALL *gp_dump_registers) (struct J9PortLibrary *portLib, void* state) ;
    I_32  (VMCALL *str_printf) (struct J9PortLibrary *portLib, char* buffer, U_32 bufLen, const char* format, ...) ;
    I_32  (VMCALL *str_vprintf) (struct J9PortLibrary *portLib, char* buffer, U_32 bufLen, const char* format, va_list args) ;
    struct J9ObjectHeader* exit_get_exit_code;
    struct J9ObjectHeader* exit_shutdown_and_exit;
    void* self_handle;
    void  (VMCALL *gp_handler_function) (void* state) ;
} J9PortLibrary;


#define J9PORT_ARCH_MIPS  "mips"
#define J9PORT_SL_INVALID  2
#define J9PORT_ARCH_SH4  "sh4"
#define J9PORT_SL_NOT_FOUND  1
#define J9PORT_ARCH_ARM  "arm"
#define J9PORT_ARCH_SPARC  "sparc"
#define J9PORT_ARCH_370  "370"
#define J9PORT_ARCH_PPC  "ppc"
#define J9PORT_ARCH_ALPHA  "alpha"
#define J9PORT_ARCH_S390  "S390"
#define J9PORT_ARCH_X86  "x86"
#define J9PORT_ARCH_68K  "M68000"
#define J9PORT_ARCH_SH3  "sh3"
#define J9PORT_ARCH_PARISC  "PA-RISC"
#define J9PORT_SL_FOUND  0
#define J9SIZEOF_J9PortLibrary 484

/* prototypes to init and shut down port libraries */
I_32 VMCALL j9port_init_library( J9PortLibrary*portLib );
I_32 VMCALL j9port_shutdown_library( J9PortLibrary*portLib );

/* for internal reaches to the port library */
J9PortLibrary * VMCALL j9sl_private_open_lib(void);
void VMCALL j9sl_private_close_lib(J9PortLibrary *lib);
#if !defined(J9PORT_LIBRARY_DEFINE)
#define j9port_shutdown_library() privatePortLibrary->port_shutdown_library(privatePortLibrary)
#define j9time_msec_clock() privatePortLibrary->time_msec_clock(privatePortLibrary)
#define j9time_usec_clock() privatePortLibrary->time_usec_clock(privatePortLibrary)
#define j9time_current_time_millis(param1) privatePortLibrary->time_current_time_millis(privatePortLibrary,param1)
#define j9sysinfo_get_pid() privatePortLibrary->sysinfo_get_pid(privatePortLibrary)
#define j9sysinfo_get_physical_memory() privatePortLibrary->sysinfo_get_physical_memory(privatePortLibrary)
#define j9sysinfo_get_OS_version(param1) privatePortLibrary->sysinfo_get_OS_version(privatePortLibrary,param1)
#define j9sysinfo_get_env(param1,param2,param3) privatePortLibrary->sysinfo_get_env(privatePortLibrary,param1,param2,param3)
#define j9sysinfo_get_CPU_architecture(param1) privatePortLibrary->sysinfo_get_CPU_architecture(privatePortLibrary,param1)
#define j9sysinfo_get_OS_type(param1) privatePortLibrary->sysinfo_get_OS_type(privatePortLibrary,param1)
#define j9sysinfo_get_classpathSeparator() privatePortLibrary->sysinfo_get_classpathSeparator(privatePortLibrary)
#define j9sysinfo_get_executable_name(param1,param2) privatePortLibrary->sysinfo_get_executable_name(privatePortLibrary,param1,param2)
#define j9sysinfo_get_number_CPUs() privatePortLibrary->sysinfo_get_number_CPUs(privatePortLibrary)
#define j9file_write(param1,param2,param3) privatePortLibrary->file_write(privatePortLibrary,param1,param2,param3)
#define j9file_open(param1,param2,param3) privatePortLibrary->file_open(privatePortLibrary,param1,param2,param3)
#define j9file_close(param1) privatePortLibrary->file_close(privatePortLibrary,param1)
#define j9file_seek(param1,param2,param3) privatePortLibrary->file_seek(privatePortLibrary,param1,param2,param3)
#define j9file_read(param1,param2,param3) privatePortLibrary->file_read(privatePortLibrary,param1,param2,param3)
#define j9file_unlink(param1) privatePortLibrary->file_unlink(privatePortLibrary,param1)
#define j9file_attr(param1) privatePortLibrary->file_attr(privatePortLibrary,param1)
#define j9file_lastmod(param1) privatePortLibrary->file_lastmod(privatePortLibrary,param1)
#define j9file_length(param1) privatePortLibrary->file_length(privatePortLibrary,param1)
#define j9file_mkdir(param1) privatePortLibrary->file_mkdir(privatePortLibrary,param1)
#define j9file_move(param1,param2) privatePortLibrary->file_move(privatePortLibrary,param1,param2)
#define j9file_unlinkdir(param1) privatePortLibrary->file_unlinkdir(privatePortLibrary,param1)
#define j9file_findfirst(param1,param2) privatePortLibrary->file_findfirst(privatePortLibrary,param1,param2)
#define j9file_findnext(param1,param2) privatePortLibrary->file_findnext(privatePortLibrary,param1,param2)
#define j9file_findclose(param1) privatePortLibrary->file_findclose(privatePortLibrary,param1)
#define j9file_sync(param1) privatePortLibrary->file_sync(privatePortLibrary,param1)
#define j9sl_close_shared_library(param1) privatePortLibrary->sl_close_shared_library(privatePortLibrary,param1)
#define j9sl_open_shared_library(param1,param2,param3,param4) privatePortLibrary->sl_open_shared_library(privatePortLibrary,param1,param2,param3,param4)
#define j9sl_map_name(param1,param2,param3) privatePortLibrary->sl_map_name(privatePortLibrary,param1,param2,param3)
#define j9sl_open(param1,param2) privatePortLibrary->sl_open(privatePortLibrary,param1,param2)
#define j9sl_close(param1) privatePortLibrary->sl_close(privatePortLibrary,param1)
#define j9sl_lookup_name(param1,param2,param3,param4) privatePortLibrary->sl_lookup_name(privatePortLibrary,param1,param2,param3,param4)
#define j9tty_initialize() privatePortLibrary->tty_initialize(privatePortLibrary)
#define j9tty_shutdown() privatePortLibrary->tty_shutdown(privatePortLibrary)
#define j9tty_printf privatePortLibrary->tty_printf
#define j9tty_get_chars(param1,param2) privatePortLibrary->tty_get_chars(privatePortLibrary,param1,param2)
#define j9tty_available() privatePortLibrary->tty_available(privatePortLibrary)
#define j9tty_output_chars(param1,param2) privatePortLibrary->tty_output_chars(privatePortLibrary,param1,param2)
#define j9tty_err_printf privatePortLibrary->tty_err_printf
#define j9tty_err_output_chars(param1,param2) privatePortLibrary->tty_err_output_chars(privatePortLibrary,param1,param2)
#define j9mem_allocate_code_memory(param1) privatePortLibrary->mem_allocate_code_memory(privatePortLibrary,param1)
#define j9mem_allocate_memory(param1) privatePortLibrary->mem_allocate_memory(privatePortLibrary,param1)
#define j9mem_free_code_memory(param1) privatePortLibrary->mem_free_code_memory(privatePortLibrary,param1)
#define j9mem_free_memory(param1) privatePortLibrary->mem_free_memory(privatePortLibrary,param1)
#define j9mem_total_physical_memory() privatePortLibrary->mem_total_physical_memory(privatePortLibrary)
#define j9vmem_commit_memory(param1,param2) privatePortLibrary->vmem_commit_memory(privatePortLibrary,param1,param2)
#define j9vmem_decommit_memory(param1,param2) privatePortLibrary->vmem_decommit_memory(privatePortLibrary,param1,param2)
#define j9vmem_free_memory(param1) privatePortLibrary->vmem_free_memory(privatePortLibrary,param1)
#define j9vmem_reserve_memory(param1,param2) privatePortLibrary->vmem_reserve_memory(privatePortLibrary,param1,param2)
#define j9sock_htons(param1) privatePortLibrary->sock_htons(privatePortLibrary,param1)
#define j9sock_htonl(param1) privatePortLibrary->sock_htonl(privatePortLibrary,param1)
#define j9sock_bind(param1,param2) privatePortLibrary->sock_bind(privatePortLibrary,param1,param2)
#define j9sock_write(param1,param2,param3) privatePortLibrary->sock_write(privatePortLibrary,param1,param2,param3)
#define j9sock_sockaddr(param1,param2,param3) privatePortLibrary->sock_sockaddr(privatePortLibrary,param1,param2,param3)
#define j9sock_freeaddr(param1) privatePortLibrary->sock_freeaddr(privatePortLibrary,param1)
#define j9sock_read(param1,param2,param3,param4) privatePortLibrary->sock_read(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_startup() privatePortLibrary->sock_startup(privatePortLibrary)
#define j9sock_socket(param1,param2,param3,param4) privatePortLibrary->sock_socket(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_accept(param1,param2,param3) privatePortLibrary->sock_accept(privatePortLibrary,param1,param2,param3)
#define j9sock_close(param1) privatePortLibrary->sock_close(privatePortLibrary,param1)
#define j9sock_shutdown_input(param1) privatePortLibrary->sock_shutdown_input(privatePortLibrary,param1)
#define j9sock_shutdown_output(param1) privatePortLibrary->sock_shutdown_output(privatePortLibrary,param1)
#define j9sock_shutdown() privatePortLibrary->sock_shutdown(privatePortLibrary)
#define j9sock_listen(param1,param2) privatePortLibrary->sock_listen(privatePortLibrary,param1,param2)
#define j9sock_ntohl(param1) privatePortLibrary->sock_ntohl(privatePortLibrary,param1)
#define j9sock_ntohs(param1) privatePortLibrary->sock_ntohs(privatePortLibrary,param1)
#define j9sock_connect(param1,param2) privatePortLibrary->sock_connect(privatePortLibrary,param1,param2)
#define j9sock_getsockname(param1,param2) privatePortLibrary->sock_getsockname(privatePortLibrary,param1,param2)
#define j9sock_getpeername(param1,param2) privatePortLibrary->sock_getpeername(privatePortLibrary,param1,param2)
#define j9sock_readfrom(param1,param2,param3,param4,param5) privatePortLibrary->sock_readfrom(privatePortLibrary,param1,param2,param3,param4,param5)
#define j9sock_select(param1,param2,param3,param4,param5) privatePortLibrary->sock_select(privatePortLibrary,param1,param2,param3,param4,param5)
#define j9sock_writeto(param1,param2,param3,param4,param5) privatePortLibrary->sock_writeto(privatePortLibrary,param1,param2,param3,param4,param5)
#define j9sock_inetntoa(param1,param2) privatePortLibrary->sock_inetntoa(privatePortLibrary,param1,param2)
#define j9sock_inetaddr(param1,param2) privatePortLibrary->sock_inetaddr(privatePortLibrary,param1,param2)
#define j9sock_gethostbyaddr(param1,param2,param3,param4) privatePortLibrary->sock_gethostbyaddr(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_gethostbyname(param1,param2) privatePortLibrary->sock_gethostbyname(privatePortLibrary,param1,param2)
#define j9sock_gethostname(param1,param2) privatePortLibrary->sock_gethostname(privatePortLibrary,param1,param2)
#define j9sock_hostent_addrlist(param1,param2) privatePortLibrary->sock_hostent_addrlist(privatePortLibrary,param1,param2)
#define j9sock_hostent_aliaslist(param1,param2) privatePortLibrary->sock_hostent_aliaslist(privatePortLibrary,param1,param2)
#define j9sock_hostent_alloc(param1) privatePortLibrary->sock_hostent_alloc(privatePortLibrary,param1)
#define j9sock_hostent_free(param1) privatePortLibrary->sock_hostent_free(privatePortLibrary,param1)
#define j9sock_hostent_hostname(param1,param2) privatePortLibrary->sock_hostent_hostname(privatePortLibrary,param1,param2)
#define j9sock_sockaddr_create(param1,param2,param3,param4) privatePortLibrary->sock_sockaddr_create(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_sockaddr_port(param1) privatePortLibrary->sock_sockaddr_port(privatePortLibrary,param1)
#define j9sock_sockaddr_address(param1) privatePortLibrary->sock_sockaddr_address(privatePortLibrary,param1)
#define j9sock_fdset_create(param1,param2) privatePortLibrary->sock_fdset_create(privatePortLibrary,param1,param2)
#define j9sock_fdset_free(param1) privatePortLibrary->sock_fdset_free(privatePortLibrary,param1)
#define j9sock_fdset_size(param1) privatePortLibrary->sock_fdset_size(privatePortLibrary,param1)
#define j9sock_timeval_create(param1,param2,param3) privatePortLibrary->sock_timeval_create(privatePortLibrary,param1,param2,param3)
#define j9sock_timeval_free(param1) privatePortLibrary->sock_timeval_free(privatePortLibrary,param1)
#define j9sock_linger_create(param1,param2,param3) privatePortLibrary->sock_linger_create(privatePortLibrary,param1,param2,param3)
#define j9sock_linger_free(param1) privatePortLibrary->sock_linger_free(privatePortLibrary,param1)
#define j9sock_getopt_int(param1,param2,param3,param4) privatePortLibrary->sock_getopt_int(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_setopt_int(param1,param2,param3,param4) privatePortLibrary->sock_setopt_int(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_getopt_bool(param1,param2,param3,param4) privatePortLibrary->sock_getopt_bool(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_setopt_bool(param1,param2,param3,param4) privatePortLibrary->sock_setopt_bool(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_getopt_byte(param1,param2,param3,param4) privatePortLibrary->sock_getopt_byte(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_setopt_byte(param1,param2,param3,param4) privatePortLibrary->sock_setopt_byte(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_getopt_linger(param1,param2,param3,param4) privatePortLibrary->sock_getopt_linger(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_setopt_linger(param1,param2,param3,param4) privatePortLibrary->sock_setopt_linger(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_getopt_sockaddr(param1,param2,param3,param4) privatePortLibrary->sock_getopt_sockaddr(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_setopt_sockaddr(param1,param2,param3,param4) privatePortLibrary->sock_setopt_sockaddr(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_setopt_ipmreq(param1,param2,param3,param4) privatePortLibrary->sock_setopt_ipmreq(privatePortLibrary,param1,param2,param3,param4)
#define j9sock_linger_enabled(param1,param2) privatePortLibrary->sock_linger_enabled(privatePortLibrary,param1,param2)
#define j9sock_linger_linger(param1,param2) privatePortLibrary->sock_linger_linger(privatePortLibrary,param1,param2)
#define j9sock_ipmreq_create(param1,param2,param3) privatePortLibrary->sock_ipmreq_create(privatePortLibrary,param1,param2,param3)
#define j9sock_ipmreq_free(param1) privatePortLibrary->sock_ipmreq_free(privatePortLibrary,param1)
#define j9sock_setflag_read(param1,param2) privatePortLibrary->sock_setflag_read(privatePortLibrary,param1,param2)
#define j9gp_protect(param1,param2) privatePortLibrary->gp_protect(privatePortLibrary,param1,param2)
#define j9gp_register_handler(param1,param2) privatePortLibrary->gp_register_handler(privatePortLibrary,param1,param2)
#define j9gp_dump_registers(param1) privatePortLibrary->gp_dump_registers(privatePortLibrary,param1)
#define j9str_printf privatePortLibrary->str_printf
#define j9str_vprintf(param1,param2,param3,param4) privatePortLibrary->str_vprintf(privatePortLibrary,param1,param2,param3,param4)
#define j9gp_handler_function() privatePortLibrary->gp_handler_function(privatePortLibrary)
#else

/* J9SourceSockets*/
extern J9_CFUNC I_32 VMCALL j9socket_sockaddr_address PROTOTYPE(( J9PortLibrary *portlib, j9sockaddr_t handle));
extern J9_CFUNC I_32 VMCALL j9socket_hostent_addrlist PROTOTYPE(( J9PortLibrary *portlib, j9hostent_t handle, U_32 index));
extern J9_CFUNC I_32 VMCALL j9socket_shutdown PROTOTYPE(( J9PortLibrary *portlib ));
extern J9_CFUNC I_32 VMCALL j9socket_gethostbyname PROTOTYPE(( J9PortLibrary *portlib, char *name, j9hostent_t handle ));
extern J9_CFUNC I_32 VMCALL j9socket_select PROTOTYPE(( J9PortLibrary *portlib, I_32 nfds, j9fdset_t readfds, j9fdset_t writefds, j9fdset_t exceptfds, j9timeval_t timeout));
extern J9_CFUNC I_32 VMCALL j9socket_setopt_linger PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  j9linger_t optval));
extern J9_CFUNC I_32 VMCALL j9socket_setopt_ipmreq PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  j9ipmreq_t optval));
extern J9_CFUNC I_32 VMCALL j9socket_sockaddr_create PROTOTYPE(( J9PortLibrary *portlib, j9sockaddr_t *handle, I_16 family, U_32 nipAddr, U_16 nPort));
extern J9_CFUNC I_32 VMCALL j9socket_getopt_linger PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  j9linger_t optval));
extern J9_CFUNC I_32 VMCALL j9socket_getpeername PROTOTYPE(( J9PortLibrary *portlib, j9socket_t handle, j9sockaddr_t addrHandle ));
extern J9_CFUNC I_32 VMCALL j9socket_gethostname PROTOTYPE(( J9PortLibrary *portlib, char *buffer, I_32 length ));
extern J9_CFUNC I_32 VMCALL j9socket_setflag_read PROTOTYPE(( J9PortLibrary *portlib, I_32 flag, I_32 *arg ));
extern J9_CFUNC I_32 VMCALL j9socket_getsockname PROTOTYPE(( J9PortLibrary *portlib, j9socket_t handle, j9sockaddr_t addrHandle ));
extern J9_CFUNC I_32 VMCALL j9socket_fdset_create PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, j9fdset_t *fdsetP ));
extern J9_CFUNC I_32 VMCALL j9socket_fdset_free PROTOTYPE(( J9PortLibrary *portlib, j9fdset_t fdsetP ));
extern J9_CFUNC I_32 VMCALL j9socket_listen PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock, I_32 backlog ));
extern J9_CFUNC I_32 VMCALL j9socket_hostent_alloc PROTOTYPE(( J9PortLibrary *portlib, j9hostent_t *handle ));
extern J9_CFUNC I_32 VMCALL j9socket_sockaddr PROTOTYPE(( J9PortLibrary *portlib, j9sockaddr_t *handle, char *addrStr, U_16 port));
extern J9_CFUNC I_32 VMCALL j9socket_linger_create PROTOTYPE(( J9PortLibrary *portlib, j9linger_t *handle, I_32 enabled, U_16 timeout));
extern J9_CFUNC I_32 VMCALL j9socket_hostent_free PROTOTYPE(( J9PortLibrary *portlib, j9hostent_t handle));
extern J9_CFUNC I_32 VMCALL j9socket_gethostbyaddr PROTOTYPE(( J9PortLibrary *portlib, char *addr, I_32 length, I_32 type, j9hostent_t handle ));
extern J9_CFUNC I_32 VMCALL j9socket_ntohl PROTOTYPE(( J9PortLibrary *portlib, I_32 val ));
extern J9_CFUNC I_32 VMCALL j9socket_socket PROTOTYPE(( J9PortLibrary *portlib, j9socket_t *handle, I_32 family, I_32 t,  I_32 protocol));
extern J9_CFUNC I_32 VMCALL j9socket_getopt_int PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  I_32 *optval));
extern J9_CFUNC I_32 VMCALL j9socket_getopt_sockaddr PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname, j9sockaddr_t optval));
extern J9_CFUNC I_32 VMCALL j9socket_setopt_sockaddr PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  j9sockaddr_t optval));
extern J9_CFUNC I_32 VMCALL j9socket_fdset_size PROTOTYPE(( J9PortLibrary *portlib, j9socket_t handle ));
extern J9_CFUNC I_32 VMCALL j9socket_linger_free PROTOTYPE(( J9PortLibrary *portlib, j9linger_t handle ));
extern J9_CFUNC I_32 VMCALL j9socket_getopt_byte PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  U_8 *optval));
extern J9_CFUNC I_32 VMCALL j9socket_timeval_create PROTOTYPE(( J9PortLibrary *portlib, U_32 secTime, U_32 uSecTime, j9timeval_t *timeP ));
extern J9_CFUNC I_32 VMCALL j9socket_setopt_byte PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  U_8 *optval));
extern J9_CFUNC I_32 VMCALL j9socket_write PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte ));
extern J9_CFUNC I_32 VMCALL j9socket_hostent_hostname PROTOTYPE(( J9PortLibrary *portlib, j9hostent_t handle, char** hostName));
extern J9_CFUNC I_32 VMCALL j9socket_linger_linger PROTOTYPE(( J9PortLibrary *portlib, j9linger_t handle, U_16 *linger));
extern J9_CFUNC I_32 VMCALL j9socket_timeval_free PROTOTYPE(( J9PortLibrary *portlib,  j9timeval_t timeP));
extern J9_CFUNC I_32 VMCALL j9socket_freeaddr PROTOTYPE(( J9PortLibrary *portlib, j9sockaddr_t handle));
extern J9_CFUNC I_32 VMCALL j9socket_hostent_aliaslist PROTOTYPE((J9PortLibrary *portlib, j9hostent_t handle, char ***aliasList));
extern J9_CFUNC I_32 VMCALL j9socket_setopt_int PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  I_32 *optval));
extern J9_CFUNC I_32 VMCALL j9socket_htonl PROTOTYPE(( J9PortLibrary *portlib, I_32 val ));
extern J9_CFUNC I_32 VMCALL j9socket_linger_enabled PROTOTYPE(( J9PortLibrary *portlib, j9linger_t handle, BOOLEAN *enabled));
extern J9_CFUNC I_32 VMCALL j9socket_connect PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock, j9sockaddr_t addr ));
extern J9_CFUNC I_32 VMCALL j9socket_accept PROTOTYPE(( J9PortLibrary *portlib, j9socket_t serverSock, j9sockaddr_t addrHandle, j9socket_t *sockHandle ));
extern J9_CFUNC I_32 VMCALL j9socket_shutdown_input PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock ));
extern J9_CFUNC I_32 VMCALL j9socket_read PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte, I_32 flags ));
extern J9_CFUNC I_32 VMCALL j9socket_ipmreq_free PROTOTYPE(( J9PortLibrary *portlib, j9ipmreq_t ipmreqP ));
extern J9_CFUNC I_32 VMCALL j9socket_inetntoa PROTOTYPE(( J9PortLibrary *portlib, char **addrStr, U_32 nipAddr ));
extern J9_CFUNC I_32 VMCALL j9socket_readfrom PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte, I_32 flags, j9sockaddr_t addrHandle ));
extern J9_CFUNC I_32 VMCALL j9socket_getopt_bool PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 optlevel, I_32 optname,  BOOLEAN *optval));
extern J9_CFUNC I_32 VMCALL j9socket_setopt_bool PROTOTYPE(( J9PortLibrary *portlib, j9socket_t socketP, I_32 level, I_32 optname,  BOOLEAN *optval));
extern J9_CFUNC I_32 VMCALL j9socket_shutdown_output PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock ));
extern J9_CFUNC U_16 VMCALL j9socket_htons PROTOTYPE(( J9PortLibrary *portlib, U_16 val ));
extern J9_CFUNC I_32 VMCALL j9socket_inetaddr PROTOTYPE(( J9PortLibrary *portlib, char *addrStr, U_32 *addr ));
extern J9_CFUNC I_32 VMCALL j9socket_bind PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock, j9sockaddr_t addr ));
extern J9_CFUNC I_32 VMCALL j9socket_startup PROTOTYPE(( J9PortLibrary *portlib ));
extern J9_CFUNC I_32 VMCALL j9socket_writeto PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock, U_8 *buf, I_32 nbyte, I_32 flags, j9sockaddr_t addrHandle ));
extern J9_CFUNC U_16 VMCALL j9socket_sockaddr_port PROTOTYPE(( J9PortLibrary *portlib, j9sockaddr_t handle));
extern J9_CFUNC U_16 VMCALL j9socket_ntohs PROTOTYPE(( J9PortLibrary *portlib, U_16 val ));
extern J9_CFUNC I_32 VMCALL j9socket_close PROTOTYPE(( J9PortLibrary *portlib, j9socket_t sock ));
extern J9_CFUNC I_32 VMCALL j9socket_ipmreq_create PROTOTYPE(( J9PortLibrary *portlib, j9ipmreq_t *handle, U_32 nipmcast, U_32 nipinterface));

/* J9SourceOS2J9TTY*/
struct J9PortLibrary ;
extern J9_CFUNC void VMCALL j9tty_shutdown PROTOTYPE((struct J9PortLibrary *portLib));
struct J9PortLibrary ;
extern J9_CFUNC IDATA VMCALL j9tty_initialize PROTOTYPE((struct J9PortLibrary *portLib));
struct J9PortLibrary ;
extern J9_CFUNC void VMCALL j9tty_err_output_chars PROTOTYPE((struct J9PortLibrary *portLib, char *c, UDATA count));
struct J9PortLibrary ;
extern J9_CFUNC void VMCALL j9tty_output_chars PROTOTYPE((struct J9PortLibrary *portLib, char *c, UDATA count));
struct J9PortLibrary ;
extern J9_CFUNC void VMCALL j9tty_err_printf PROTOTYPE((struct J9PortLibrary *portLib, char * format, ...));
struct J9PortLibrary ;
extern J9_CFUNC IDATA VMCALL j9tty_get_chars PROTOTYPE((struct J9PortLibrary *portLib, char * s, UDATA length));
struct J9PortLibrary ;
extern J9_CFUNC void VMCALL j9tty_printf PROTOTYPE((struct J9PortLibrary *portLib, char * format, ...));
struct J9PortLibrary ;
extern J9_CFUNC IDATA VMCALL j9tty_available PROTOTYPE((struct J9PortLibrary *portLib));

/* J9SourceOS2J9Mem*/
extern J9_CFUNC void VMCALL j9mem_free_code_memory PROTOTYPE((J9PortLibrary *portLibrary, void * memoryPointer));
extern J9_CFUNC void * VMCALL j9mem_allocate_code_memory PROTOTYPE((J9PortLibrary *portLibrary, U_32 size));
extern J9_CFUNC void * VMCALL j9mem_allocate_memory PROTOTYPE((J9PortLibrary *portLibrary, U_32 byteAmount));
extern J9_CFUNC void VMCALL j9mem_free_memory PROTOTYPE((J9PortLibrary *portLibrary, void *memoryPointer));

/* J9SourceOS2J9Time*/
extern J9_CFUNC void VMCALL j9time_current_time_millis PROTOTYPE((J9PortLibrary *portLibrary, I_64 *time_millis));
extern J9_CFUNC U_32 VMCALL j9time_msec_clock PROTOTYPE((J9PortLibrary *portLibrary));
extern J9_CFUNC U_32 VMCALL j9time_usec_clock PROTOTYPE((J9PortLibrary *portLibrary));

/* J9SourceOS2J9SI*/
extern J9_CFUNC IDATA VMCALL j9sysinfo_get_executable_name PROTOTYPE((J9PortLibrary * portLib, char *argv0, char **result));
extern J9_CFUNC I_32 VMCALL j9sysinfo_get_OS_type PROTOTYPE((J9PortLibrary *portLibrary, char *infoString));
extern J9_CFUNC UDATA VMCALL j9sysinfo_get_physical_memory PROTOTYPE((J9PortLibrary *portLibrary));
extern J9_CFUNC U_32 VMCALL j9sysinfo_get_pid PROTOTYPE((J9PortLibrary *portLibrary));
extern J9_CFUNC UDATA VMCALL j9sysinfo_get_number_CPUs PROTOTYPE((J9PortLibrary *portLibrary));
extern J9_CFUNC U_16 VMCALL j9sysinfo_get_classpathSeparator PROTOTYPE((J9PortLibrary *portLibrary ));
extern J9_CFUNC I_32 VMCALL j9sysinfo_get_CPU_architecture PROTOTYPE((J9PortLibrary *portLibrary, char *infoString));
extern J9_CFUNC I_32 VMCALL j9sysinfo_get_env PROTOTYPE((J9PortLibrary *portLibrary, char *envVar, char *infoString, U_32 bufSize));
extern J9_CFUNC I_32 VMCALL j9sysinfo_get_OS_version PROTOTYPE((J9PortLibrary *portLibrary, char *infoString));

/* J9SourceOS2J9File*/
extern J9_CFUNC I_32 VMCALL j9file_unlink PROTOTYPE((J9PortLibrary *portLibrary, char * path));
extern J9_CFUNC I_32 VMCALL j9file_close PROTOTYPE((J9PortLibrary *portLibrary, I_32 fd));
extern J9_CFUNC I_32 VMCALL j9file_mkdir PROTOTYPE((J9PortLibrary *portLibrary, char * path));
extern J9_CFUNC I_32 VMCALL j9file_open PROTOTYPE((J9PortLibrary *portLibrary, char * path, I_32 flags, I_32 mode));
extern J9_CFUNC I_32 VMCALL j9file_move PROTOTYPE((J9PortLibrary *portLibrary, char * pathExist, char * pathNew));
extern J9_CFUNC I_32 VMCALL j9file_findnext PROTOTYPE((J9PortLibrary *portLibrary, UDATA findhandle, char * resultbuf));
extern J9_CFUNC I_32 VMCALL j9file_sync PROTOTYPE((J9PortLibrary *portLibrary, I_32 fd));
extern J9_CFUNC I_32 VMCALL j9file_unlinkdir PROTOTYPE((J9PortLibrary *portLibrary, char * path));
extern J9_CFUNC UDATA VMCALL j9file_findfirst PROTOTYPE((J9PortLibrary *portLibrary, char * path, char * resultbuf));
extern J9_CFUNC I_32 VMCALL j9file_attr PROTOTYPE((J9PortLibrary *portLibrary, char * path));
extern J9_CFUNC void VMCALL j9file_findclose PROTOTYPE((J9PortLibrary *portLibrary, UDATA findhandle));
extern J9_CFUNC I_32 VMCALL j9file_read PROTOTYPE((J9PortLibrary *portLibrary, 	I_32 fd, void * buf, I_32 nbytes));
extern J9_CFUNC I_32 VMCALL j9file_write PROTOTYPE((J9PortLibrary *portLibrary, I_32 fd, void * buf, I_32 nbytes));
extern J9_CFUNC I_32 VMCALL  j9file_seek PROTOTYPE((J9PortLibrary *portLibrary, I_32 fd, I_32 offset, I_32 whence));
extern J9_CFUNC I_32 VMCALL j9file_length PROTOTYPE((J9PortLibrary *portLibrary, char * path));
extern J9_CFUNC I_64 VMCALL j9file_lastmod PROTOTYPE((J9PortLibrary *portLibrary, char * path));

/* J9SourceOS2J9SL*/
extern J9_CFUNC UDATA VMCALL j9sl_close PROTOTYPE((J9PortLibrary *portLibrary, UDATA * descriptor));
extern J9_CFUNC UDATA VMCALL j9sl_lookup_name PROTOTYPE((J9PortLibrary *portLibrary, UDATA descriptor, char * name, UDATA * func, UDATA argCount));
extern J9_CFUNC J9PortLibrary *j9sl_private_open_lib PROTOTYPE((void));
extern J9_CFUNC UDATA VMCALL j9sl_map_name PROTOTYPE((J9PortLibrary *portLibrary, char *baseName, char *result, UDATA * resultLength));
extern J9_CFUNC UDATA VMCALL j9sl_open PROTOTYPE((J9PortLibrary *portLibrary, char * path, UDATA * descriptor));
extern J9_CFUNC UDATA VMCALL j9sl_open_shared_library PROTOTYPE((J9PortLibrary *portLibrary, char *name, UDATA *descriptor, char* errBuf, UDATA bufLen));
extern J9_CFUNC void j9sl_private_close_lib PROTOTYPE((J9PortLibrary *lib));
extern J9_CFUNC UDATA VMCALL j9sl_close_shared_library PROTOTYPE((J9PortLibrary *portLibrary, UDATA descriptor));

/* J9SourceJ9GP*/
struct J9PortLibrary ;
extern J9_CFUNC void VMCALL j9gp_dump_registers PROTOTYPE((struct J9PortLibrary *portLib, void* info));
struct J9PortLibrary ;
extern J9_CFUNC void VMCALL j9gp_register_handler PROTOTYPE((struct J9PortLibrary *portLib,  handler_fn fn, void* aUserData ));
struct J9PortLibrary ;
extern J9_CFUNC UDATA VMCALL j9gp_protect PROTOTYPE((struct J9PortLibrary *portLib,  protected_fn fn, void* arg ));

/* J9SourceStaticSL*/
extern J9_CFUNC UDATA VMCALL j9sl_up_open_shared_library PROTOTYPE((J9PortLibrary *portLibrary, char * name, UDATA * descriptor, char* errBuf, UDATA bufLen));
extern J9_CFUNC UDATA VMCALL j9sl_split_close_shared_library PROTOTYPE((J9PortLibrary *portLibrary, UDATA descriptor));
extern J9_CFUNC UDATA VMCALL j9sl_up_close_shared_library PROTOTYPE((J9PortLibrary *portLibrary, UDATA descriptor));
extern J9_CFUNC UDATA VMCALL j9sl_split_open_shared_library PROTOTYPE((J9PortLibrary *portLibrary, char * name, UDATA * descriptor, char* errBuf, UDATA bufLen));
extern J9_CFUNC UDATA VMCALL j9sl_split_lookup_name PROTOTYPE((J9PortLibrary *portLibrary, UDATA descriptor, char * name, UDATA * func, UDATA sendArgs));
extern J9_CFUNC UDATA VMCALL j9sl_up_lookup_name PROTOTYPE((J9PortLibrary *portLibrary, UDATA descriptor, char * name, UDATA * func, UDATA sendArgs));

/* J9SourcePort*/
extern J9_CFUNC I_32 VMCALL j9port_shutdown_library PROTOTYPE(( J9PortLibrary*portLib ));
extern J9_CFUNC I_32 VMCALL j9port_init_library PROTOTYPE((J9PortLibrary * portLib));
extern J9_CFUNC I_32 VMCALL j9port_startup_library PROTOTYPE((J9PortLibrary * portLib));
extern J9_CFUNC I_32 VMCALL j9port_create_library PROTOTYPE((J9PortLibrary * portLib));

/* J9SourceJ9VMem*/
extern J9_CFUNC void *VMCALL j9vmem_commit_memory PROTOTYPE((J9PortLibrary * portLibrary, void *address, U_32 byteAmount));
extern J9_CFUNC I_32 VMCALL j9vmem_free_memory PROTOTYPE((J9PortLibrary * portLibrary, void *address));
extern J9_CFUNC I_32 VMCALL j9vmem_decommit_memory PROTOTYPE((J9PortLibrary * portLibrary, void *address, U_32 byteAmount));
extern J9_CFUNC void *VMCALL j9vmem_reserve_memory PROTOTYPE((J9PortLibrary * portLibrary, void *address, U_32 byteAmount));

/* J9SourceJ9Str*/
extern J9_CFUNC I_32 VMCALL j9str_printf PROTOTYPE((J9PortLibrary* portLib, char* buf, U_32 bufLen, const char* format, ...));
extern J9_CFUNC I_32 VMCALL  j9str_vprintf PROTOTYPE((J9PortLibrary* portLib, char* buf, U_32 bufLen, const char* format, va_list args));

/* J9SourceWinCEJ9Exit*/
extern J9_CFUNC int VMCALL j9exit_get_exit_code PROTOTYPE((J9PortLibrary * portLib));
extern J9_CFUNC IDATA VMCALL j9exit_initialize PROTOTYPE((J9PortLibrary * portLib));
extern J9_CFUNC void VMCALL j9exit_shutdown PROTOTYPE((J9PortLibrary * portLib));
extern J9_CFUNC void VMCALL j9exit_shutdown_and_exit PROTOTYPE((J9PortLibrary * portLib, int exitCode));

#endif

#endif
