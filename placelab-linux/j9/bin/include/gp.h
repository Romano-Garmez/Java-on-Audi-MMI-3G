/*
*	(c) Copyright IBM Corp. 1991, 2003 All Rights Reserved
*/
#ifndef gp_h
#define gp_h

#ifdef WIN32_IBMC
typedef UDATA (* VMCALL protected_fn)(void*);
typedef void (* VMCALL handler_fn)(UDATA gpType, void* gpInfo, void* userData);
#else
typedef UDATA (*protected_fn)(void*);
typedef void (*handler_fn)(UDATA gpType, void* gpInfo, void* userData);
#endif

#define J9PrimErrGPF 0
#define J9PrimErrGPFInvalidRead 1
#define J9PrimErrGPFInvalidWrite 2
#define J9PrimErrGPFInvalidInstruction 3
#define J9PrimErrGPFFloat 4

#endif     /* gp_h */
   
