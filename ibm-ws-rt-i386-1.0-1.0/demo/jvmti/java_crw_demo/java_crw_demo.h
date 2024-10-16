/*
 * @(#)src/demo/jvmti/java_crw_demo/java_crw_demo.h, dsdev, dsdev, 20060202 1.3
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
 * @(#)java_crw_demo.h	1.13 04/07/27
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

#ifndef JAVA_CRW_DEMO_H
#define JAVA_CRW_DEMO_H

#include <jni.h>

/* This callback is used to notify the caller of a fatal error. */

typedef void (*FatalErrorHandler)(const char*message, const char*file, int line);

/* This callback is used to return the method information for a class.
 *   Since the information was already read here, it was useful to
 *   return it here, with no JVMTI phase restrictions.
 *   If the class file does represent a "class" and it has methods, then
 *   this callback will be called with the class number and pointers to
 *   the array of names, array of signatures, and the count of methods.
 */

typedef void (*MethodNumberRegister)(unsigned, const char**, const char**, int);

/* Class file reader/writer interface. Basic input is a classfile image
 *     and details about what to inject. The output is a new classfile image
 *     that was allocated with malloc(), and should be freed by the caller.
 */

/* Names of external symbols to look for. These are the names that we
 *   try and lookup in the shared library. On Windows 2000, the naming
 *   convention is to prefix a "_" and suffix a "@N" where N is 4 times
 *   the number or arguments supplied.It has 19 args, so 76 = 19*4.
 *   On Windows 2003, Linux, and Solaris, the first name will be
 *   found, on Windows 2000 a second try should find the second name.
 *
 *   WARNING: If You change the JavaCrwDemo typedef, you MUST change
 *            multiple things in this file, including this name.
 */

#define JAVA_CRW_DEMO_SYMBOLS { "java_crw_demo", "_java_crw_demo@76" }

/* Typedef needed for type casting in dynamic access situations. */

typedef void (JNICALL *JavaCrwDemo)(
	 unsigned class_number,
	 const char *name,
	 const unsigned char *file_image,
	 long file_len,
	 int system_class,
	 char* tclass_name,
	 char* tclass_sig,
	 char* call_name,
	 char* call_sig,
	 char* return_name,
	 char* return_sig,
	 char* obj_init_name,
	 char* obj_init_sig,
	 char* newarray_name,
	 char* newarray_sig,
	 unsigned char **pnew_file_image,
	 long *pnew_file_len,
	 FatalErrorHandler fatal_error_handler,
	 MethodNumberRegister mnum_callback
);

/* Function export (should match typedef above) */

JNIEXPORT void JNICALL java_crw_demo(

	 unsigned class_number, /* Caller assigned class number for class */

	 const char *name,      /* Internal class name, e.g. java/lang/Object */
				/*   (Do not use "java.lang.Object" format) */

	 const unsigned char
	   *file_image,         /* Pointer to classfile image for this class */

	 long file_len, 	/* Length of the classfile in bytes */

	 int system_class,      /* Set to 1 if this is a system class */
				/*   (prevents injections into empty */
				/*   <clinit>, finalize, and <init> methods) */

	 char* tclass_name,	/* Class that has methods we will call at */
				/*   the injection sites (tclass) */

	 char* tclass_sig,	/* Signature of tclass */
				/*  (Must be "L" + tclass_name + ";") */

	 char* call_name,	/* Method name in tclass to call at offset 0 */
				/*   for every method */

	 char* call_sig,	/* Signature of this call_name method */
				/*  (Must be "(II)V") */

	 char* return_name,	/* Method name in tclass to call at all */
				/*  return opcodes in every method */

	 char* return_sig,	/* Signature of this return_name method */
				/*  (Must be "(II)V") */

	 char* obj_init_name,	/* Method name in tclass to call first thing */
				/*   when injecting java.lang.Object.<init> */

	 char* obj_init_sig,	/* Signature of this obj_init_name method */
				/*  (Must be "(Ljava/lang/Object;)V") */

	 char* newarray_name,	/* Method name in tclass to call after every */
				/*   newarray opcode in every method */

	 char* newarray_sig,	/* Signature of this method */
				/*  (Must be "(Ljava/lang/Object;II)V") */

	 unsigned char
	   **pnew_file_image,   /* Returns a pointer to new classfile image */

	 long *pnew_file_len,   /* Returns the length of the new image */

	 FatalErrorHandler
	   fatal_error_handler, /* Pointer to function to call on any */
			        /*  fatal error. NULL sends error to stderr */

	 MethodNumberRegister
	   mnum_callback        /* Pointer to function that gets called */
				/*   with all details on methods in this */
				/*   class. NULL means skip this call. */

	   );

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

#ifndef JAVA_CRW_DEMO_H
#define JAVA_CRW_DEMO_H

#include <jni.h>

/* This callback is used to notify the caller of a fatal error. */

typedef void (*FatalErrorHandler)(const char*message, const char*file, int line);

/* This callback is used to return the method information for a class.
 *   Since the information was already read here, it was useful to
 *   return it here, with no JVMTI phase restrictions.
 *   If the class file does represent a "class" and it has methods, then
 *   this callback will be called with the class number and pointers to
 *   the array of names, array of signatures, and the count of methods.
 */

typedef void (*MethodNumberRegister)(unsigned, const char**, const char**, int);

/* Class file reader/writer interface. Basic input is a classfile image
 *     and details about what to inject. The output is a new classfile image
 *     that was allocated with malloc(), and should be freed by the caller.
 */

/* Names of external symbols to look for. These are the names that we
 *   try and lookup in the shared library. On Windows 2000, the naming
 *   convention is to prefix a "_" and suffix a "@N" where N is 4 times
 *   the number or arguments supplied.It has 19 args, so 76 = 19*4.
 *   On Windows 2003, Linux, and Solaris, the first name will be
 *   found, on Windows 2000 a second try should find the second name.
 *
 *   WARNING: If You change the JavaCrwDemo typedef, you MUST change
 *            multiple things in this file, including this name.
 */

#define JAVA_CRW_DEMO_SYMBOLS { "java_crw_demo", "_java_crw_demo@76" }

/* Typedef needed for type casting in dynamic access situations. */

typedef void (JNICALL *JavaCrwDemo)(
	 unsigned class_number,
	 const char *name,
	 const unsigned char *file_image,
	 long file_len,
	 int system_class,
	 char* tclass_name,
	 char* tclass_sig,
	 char* call_name,
	 char* call_sig,
	 char* return_name,
	 char* return_sig,
	 char* obj_init_name,
	 char* obj_init_sig,
	 char* newarray_name,
	 char* newarray_sig,
	 unsigned char **pnew_file_image,
	 long *pnew_file_len,
	 FatalErrorHandler fatal_error_handler,
	 MethodNumberRegister mnum_callback
);

/* Function export (should match typedef above) */

JNIEXPORT void JNICALL java_crw_demo(

	 unsigned class_number, /* Caller assigned class number for class */

	 const char *name,      /* Internal class name, e.g. java/lang/Object */
				/*   (Do not use "java.lang.Object" format) */

	 const unsigned char
	   *file_image,         /* Pointer to classfile image for this class */

	 long file_len, 	/* Length of the classfile in bytes */

	 int system_class,      /* Set to 1 if this is a system class */
				/*   (prevents injections into empty */
				/*   <clinit>, finalize, and <init> methods) */

	 char* tclass_name,	/* Class that has methods we will call at */
				/*   the injection sites (tclass) */

	 char* tclass_sig,	/* Signature of tclass */
				/*  (Must be "L" + tclass_name + ";") */

	 char* call_name,	/* Method name in tclass to call at offset 0 */
				/*   for every method */

	 char* call_sig,	/* Signature of this call_name method */
				/*  (Must be "(II)V") */

	 char* return_name,	/* Method name in tclass to call at all */
				/*  return opcodes in every method */

	 char* return_sig,	/* Signature of this return_name method */
				/*  (Must be "(II)V") */

	 char* obj_init_name,	/* Method name in tclass to call first thing */
				/*   when injecting java.lang.Object.<init> */

	 char* obj_init_sig,	/* Signature of this obj_init_name method */
				/*  (Must be "(Ljava/lang/Object;)V") */

	 char* newarray_name,	/* Method name in tclass to call after every */
				/*   newarray opcode in every method */

	 char* newarray_sig,	/* Signature of this method */
				/*  (Must be "(Ljava/lang/Object;II)V") */

	 unsigned char
	   **pnew_file_image,   /* Returns a pointer to new classfile image */

	 long *pnew_file_len,   /* Returns the length of the new image */

	 FatalErrorHandler
	   fatal_error_handler, /* Pointer to function to call on any */
			        /*  fatal error. NULL sends error to stderr */

	 MethodNumberRegister
	   mnum_callback        /* Pointer to function that gets called */
				/*   with all details on methods in this */
				/*   class. NULL means skip this call. */

	   );

#endif

