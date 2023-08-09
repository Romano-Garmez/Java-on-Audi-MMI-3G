/*
*	(c) Copyright IBM Corp. 1991, 2003 All Rights Reserved
*/

#ifndef j9comp_h
#define j9comp_h

#include "j9cfg.h"
#ifdef VXWORKS
#undef ARM
#endif

/*
USE_PROTOTYPES:			Use full ANSI prototypes.

CLOCK_PRIMS:					We want the timer/clock prims to be used

LITTLE_ENDIAN:				This is for the intel machines or other
											little endian processors. Defaults to big endian.

NO_LVALUE_CASTING:	This is for compilers that don't like the left side
											of assigns to be cast.  It hacks around to do the
											right thing.

ATOMIC_FLOAT_ACCESS:	For the hp720 so that float operations will work.

LINKED_USER_PRIMITIVES:	Indicates that user primitives are statically linked
													with the VM executeable.

OLD_SPACE_SIZE_DIFF:	The 68k uses a different amount of old space.
											This "legitimizes" the change.

SIMPLE_SIGNAL:		For machines that don't use real signals in C.
									(eg: PC, 68k)

OS_NAME_LOOKUP:		Use nlist to lookup user primitive addresses.

VMCALL:			Tag for all functions called by the VM.

VMAPICALL:		Tag for all functions called via the PlatformFunction
							callWith: mechanism.
			
SYS_FLOAT:	For the MPW C compiler on MACintosh. Most of the math functions 
						there return extended type which has 80 or 96 bits depending on 68881 option.
						On All other platforms it is double

FLOAT_EXTENDED: If defined, the type name for extended precision floats.

PLATFORM_IS_ASCII: Must be defined if the platform is ASCII

EXE_EXTENSION_CHAR: the executable has a delimiter that we want to stop at as part of argv[0].

*/

/* By default order doubles in the native (i.e. big/little endian) ordering. */
#define J9_PLATFORM_DOUBLE_ORDER

/* ARM Emulator */

#if defined(ARM) && !defined(J9WINCE) && !defined(BREW) && !defined(J9OSE)
typedef long long	I_64 ;
typedef unsigned long long	U_64 ;

#define NO_LVALUE_CASTING
#define SYS_FLOAT	double
#define PLATFORM_LINE_DELIMITER	"\015"
#define DIR_SEPARATOR ':'
#define DIR_SEPARATOR_STR ":"
#endif

/* ARMGNU */

#if defined(ARMGNU) && !defined(NEUTRINO)
#undef J9_PLATFORM_DOUBLE_ORDER
#endif

/* AS400 */

#ifdef AS400

#define DATA_TYPES_DEFINED
typedef unsigned int				UDATA ;		/* 64bits */
typedef unsigned int				U_64;
typedef unsigned __int32		U_32;
typedef unsigned short			U_16;
typedef unsigned char			U_8;
typedef signed int				IDATA;		/* 64bits */
typedef signed int				I_64;		
typedef __int32					I_32;
typedef signed short				I_16;
typedef signed char				I_8;			
typedef U_32						BOOLEAN;

typedef double					SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

#endif

/* CHORUS */

#ifdef CHORUS

typedef long long I_64;
typedef unsigned long long U_64;

#define DATA_TYPES_DEFINED
typedef unsigned int                    UDATA;
typedef unsigned int                    U_32;
typedef unsigned short          U_16;
typedef unsigned char           U_8;
typedef int                                             IDATA;
typedef int                                             I_32;
typedef short                                   I_16;
typedef signed char                     I_8;
typedef UDATA                           BOOLEAN;
typedef double                          SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER "\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#define h_errno errno

#define J9_DEFAULT_SCHED SCHED_RR
#define J9_PRIORITY_MAP {5,6,7,8,9,10,11,12,13,14,15, 16}

#endif

/* DECUNIX */

#ifdef DECUNIX

#define DATA_TYPES_DEFINED
typedef unsigned long int		UDATA;					/* 64bits */
typedef unsigned long int		U_64;						/* 64bits */
typedef unsigned int				U_32;
typedef unsigned short			U_16;
typedef unsigned char			U_8;
typedef signed long int			IDATA;					/* 64bits */
typedef signed long int			I_64;						/* 64bits */
typedef signed int				I_32;						
typedef signed short				I_16;			
typedef signed char				I_8;				
typedef U_32						BOOLEAN; 	
typedef double					SYS_FLOAT; 

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

#endif

/* DOS TTY, with Watcom C */

#ifdef DOS
#define NO_LVALUE_CASTING

#define SYS_FLOAT double
#define EXE_EXTENSION_CHAR	'.'
#define DIR_SEPARATOR '\\'
#define DIR_SEPARATOR_STR "\\"
#endif

/* HP720 ANSI compiler */

#ifdef HP720

typedef signed long long 		I_64;
typedef unsigned long long 	U_64;

#define NO_LVALUE_CASTING
#define ATOMIC_FLOAT_ACCESS
#define SYS_FLOAT double
#define FLOAT_EXTENDED	long double
#define PLATFORM_IS_ASCII
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#endif

/* ITRON */
#ifdef ITRON
#ifdef ITRONGNU
typedef long long I_64;
typedef unsigned long long U_64;
#else
typedef long I_64;									/* BOGUS -- Compiler does not provide an int64 type! */
typedef unsigned long U_64;						/* BOGUS -- Compiler does not provide an int64 type! */
#endif

#define DATA_TYPES_DEFINED
typedef unsigned int			UDATA;
typedef unsigned int			U_32;
typedef unsigned short		U_16;
typedef unsigned char		U_8;
typedef int						IDATA;
typedef int						I_32;
typedef short					I_16;
typedef signed char			I_8;
typedef UDATA				BOOLEAN;
typedef double				SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#define J9_SUPPRESS_MAIN

#define J9_DEFAULT_SCHED SCHED_RR
#define J9_PRIORITY_MAP {12,11,10,9,8,7,6,5,4,3,2,1}

#endif

/* J9EPOC32 */

#ifdef J9EPOC32

typedef long long	I_64 ;
typedef unsigned long long	U_64 ;

#define NO_LVALUE_CASTING
#define SYS_FLOAT double
#define EXE_EXTENSION_CHAR	'.'
#define DIR_SEPARATOR '\\'
#define DIR_SEPARATOR_STR "\\"

#endif
/* J9WINCE WinCE */

#ifdef J9WINCE

typedef __int64					I_64 ;
typedef unsigned __int64	U_64 ;
typedef double 					SYS_FLOAT;

/* TGD:  the following was added as WinCE #defines it in winnt.h.
	To not #define it by hand, one must do:
		#include <windows.h>
	but that isn't the right solution, so we do it by hand.
*/
#ifndef offsetof
#define offsetof(s,m) ((size_t)&(((s*)0)->m))
#endif

#define NO_LVALUE_CASTING

#define VMAPICALL _stdcall
#define VMCALL _cdecl
#define PLATFORM_IS_ASCII
#define EXE_EXTENSION_CHAR	'.'
#define DIR_SEPARATOR '\\'
#define DIR_SEPARATOR_STR "\\"

#ifdef SH4
#define J9_NO_DENORMAL_FLOAT_SUPPORT
#endif

#endif

/* Linux ANSI compiler (gcc) */

#ifdef LINUX

/* NOTE: Linux supports different processors -- do not assume 386 */

typedef double SYS_FLOAT;
typedef long long I_64;
typedef unsigned long long U_64;

#define NO_LVALUE_CASTING
#define FLOAT_EXTENDED	long double
#define PLATFORM_IS_ASCII
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

/* no priorities on Linux */
#define J9_PRIORITY_MAP {0,0,0,0,0,0,0,0,0,0,0,0}

#if defined(LINUXPPC) || defined(S390)
#define VA_PTR(valist) (&valist[0])
#endif

#endif

/* MIPS processors */

#ifdef MIPS
#define ATOMIC_FLOAT_ACCESS
#endif

/* IRIX ANSI compiler (gcc) */

#ifdef IRIX

typedef long long					I_64;
typedef unsigned long long	U_64;
typedef double					SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

#define J9_DEFAULT_SCHED SCHED_RR

#endif

/* MVS compiler */

#ifdef MVS

#define DATA_TYPES_DEFINED
typedef unsigned int				UDATA;
typedef unsigned long long	U_64;
typedef unsigned int				U_32;
typedef unsigned short			U_16;
typedef unsigned char			U_8;
typedef signed int				IDATA;
typedef signed long long		I_64;
typedef signed int				I_32;
typedef signed short				I_16;
typedef signed char				I_8;
typedef I_32						BOOLEAN;
typedef double 					SYS_FLOAT;
typedef long double				FLOAT_EXTENDED;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\025"
#define DIR_SEPARATOR '.'
#define DIR_SEPARATOR_STR "."

#include "esmap.h"

#endif

/* NEUTRINO */

#ifdef NEUTRINO

typedef long long I_64;
typedef unsigned long long U_64;

#define DATA_TYPES_DEFINED
typedef unsigned int			UDATA;
typedef unsigned int			U_32;
typedef unsigned short		U_16;
typedef unsigned char		U_8;
typedef int						IDATA;
typedef int						I_32;
typedef short					I_16;
typedef signed char			I_8;
typedef UDATA				BOOLEAN;
typedef double				SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

#define J9_DEFAULT_SCHED SCHED_RR
#define J9_PRIORITY_MAP {5,6,7,8,9,10,11,12,13,14,15, 16}

#ifdef MIPS
#define J9_NO_DENORMAL_FLOAT_SUPPORT
#endif

#ifdef PPC
#define VA_PTR(valist) (&valist[0])
#endif

#endif

/* NeXT Ansi Gnu compiler */

#ifdef NeXT

#define DATA_TYPES_DEFINED
typedef unsigned long int		UDATA;					/* 32bits */
typedef unsigned long long int	U_64;					/* 64bits */
typedef unsigned long int		U_32;
typedef unsigned short			U_16;
typedef unsigned char			U_8;
typedef signed long int			IDATA;					/* 32bits */
typedef signed long long int	I_64;					/* 64bits */
typedef signed long int			I_32;						
typedef signed short			I_16;			
typedef signed char				I_8;				
typedef U_32					BOOLEAN; 	
typedef double					SYS_FLOAT; 

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\015"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#define FLOAT_EXTENDED	long double

#endif

/* OS2 */

#ifdef OS2
#define NO_LVALUE_CASTING
#define EXE_EXTENSION_CHAR	'.'
#define DIR_SEPARATOR '\\'
#define DIR_SEPARATOR_STR "\\"

#define OS2PM
#include "headers.h"
#define	VMCALL _Optlink
#define VMAPICALL _System

typedef double	 		SYS_FLOAT;
typedef long double		FLOAT_EXTENDED;
typedef unsigned long long			U_64;
typedef long long			I_64;

#endif

/* OSX (mac) compiler (gcc) */

#ifdef OSX

#define DATA_TYPES_DEFINED
typedef unsigned long int		UDATA;					/* 32bits */
typedef unsigned long long int	U_64;					/* 64bits */
typedef unsigned long int		U_32;
typedef unsigned short			U_16;
typedef unsigned char			U_8;
typedef signed long int			IDATA;					/* 32bits */
typedef signed long long int	I_64;					/* 64bits */
typedef signed long int			I_32;						
typedef signed short			I_16;			
typedef signed char				I_8;				
typedef U_32					BOOLEAN; 	
typedef double					SYS_FLOAT; 

#define NO_LVALUE_CASTING
#define FLOAT_EXTENDED	long double
#define PLATFORM_IS_ASCII
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

#endif

/* J9OSE */

#ifdef J9OSE

typedef long long  I_64;
typedef unsigned long long U_64;

#define DATA_TYPES_DEFINED
typedef unsigned int			UDATA;
typedef unsigned int			U_32;
typedef unsigned short		U_16;
typedef unsigned char		U_8;
typedef int						IDATA;
typedef int						I_32;
typedef short					I_16;
typedef signed char			I_8;
typedef UDATA				BOOLEAN;
typedef double				SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

#define J9_DEFAULT_SCHED SCHED_FIFO
#define J9_PRIORITY_MAP {20,19,18,17,16,15,14,13,12,11,10,9}

#ifdef J9VM_ENV_LITTLE_ENDIAN
#ifndef LITTLE_ENDIAN
#define LITTLE_ENDIAN
#endif
#else
#define BIG_ENDIAN
#endif

#ifdef PPC
#define VA_PTR(valist) (&valist[0])
#endif

#ifdef ARM
#define VA_PTR(valist) (&valist[0])
#endif

#endif

/* BREW */

#ifdef BREW
/* resolve any issues about TRUE/FALSE definition */
#define TRUE   1   /* Boolean true value. */
#define FALSE  0   /* Boolean false value. */

#ifdef AEE_SIMULATOR
typedef __int64 I_64;
typedef  unsigned __int64 U_64;
#else
typedef long long I_64;
typedef unsigned long long U_64;
#endif

#define DATA_TYPES_DEFINED
typedef unsigned int			UDATA;
typedef unsigned int			U_32;
typedef unsigned short		U_16;
typedef unsigned char		U_8;
typedef int						IDATA;
typedef int						I_32;
typedef short					I_16;
typedef signed char			I_8;
typedef UDATA				BOOLEAN;
typedef double				SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"

#ifdef AEE_SIMULATOR
#define DIR_SEPARATOR  '\\'
#define DIR_SEPARATOR_STR   "\\"
#else
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#endif

#define J9_SUPPRESS_MAIN
#define LITTLE_ENDIAN

#ifdef ARM
#define VA_PTR(valist) (&valist[0])
#endif

#endif

/* PILOT Ansi Gnu compiler */

#ifdef PILOT

#define DATA_TYPES_DEFINED
typedef unsigned long int UDATA;	/* 32bits */
typedef unsigned long long int U_64;	/* 64bits */
typedef unsigned long int U_32;
typedef unsigned short U_16;
typedef unsigned char U_8;
typedef signed long int IDATA;	/* 32bits */
typedef signed long long int I_64;	/* 64bits */
typedef signed long int I_32;
typedef signed short I_16;
typedef signed char I_8;
typedef U_32 BOOLEAN;
typedef double SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\015"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#define FLOAT_EXTENDED	long double

extern void* getDataPtr(void* a5Relative);
#define GLOBAL_DATA(symbol) getDataPtr(&symbol)
#define GLOBAL_TABLE(symbol) GLOBAL_DATA(symbol)

#else

#define GLOBAL_DATA(symbol) ((void*)&(symbol))
#define GLOBAL_TABLE(symbol) GLOBAL_DATA(symbol)

#endif

/* RIM386 */

#ifdef RIM386

typedef __int64					I_64 ;
typedef unsigned __int64	U_64 ;

typedef double 					SYS_FLOAT;

#define NO_LVALUE_CASTING
#define VMAPICALL _stdcall
#define VMCALL _cdecl
#define EXE_EXTENSION_CHAR	'.'
#define DIR_SEPARATOR '\\'
#define DIR_SEPARATOR_STR "\\"

/* (on WinCE, there's another priority: THREAD_PRIORITY_ABOVE_IDLE) */
#define J9_PRIORITY_MAP {	\
	THREAD_PRIORITY_IDLE,							/* 0 */\
	THREAD_PRIORITY_LOWEST,					/* 1 */\
	THREAD_PRIORITY_BELOW_NORMAL,	/* 2 */\
	THREAD_PRIORITY_BELOW_NORMAL,	/* 3 */\
	THREAD_PRIORITY_BELOW_NORMAL,	/* 4 */\
	THREAD_PRIORITY_NORMAL,						/* 5 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 6 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 7 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 8 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 9 */\
	THREAD_PRIORITY_HIGHEST,					/*10 */\
	THREAD_PRIORITY_TIME_CRITICAL			/*11 */}

#endif

/* RS6000 */

/* The AIX platform has the define AIXPPC and RS6000,
	this means AIXPPC inherits from the RS6000.*/

#if defined(RS6000) || defined(OSOPEN)

#define DATA_TYPES_DEFINED
#if defined(PPC64)
typedef unsigned long long UDATA;
typedef long long IDATA;
#else
typedef unsigned int UDATA;
typedef signed int IDATA;
#endif

typedef unsigned long long U_64;
typedef unsigned int U_32;
typedef unsigned short U_16;
typedef unsigned char U_8;
typedef long long I_64;
typedef signed int I_32;
typedef signed short I_16;
typedef signed char I_8;
typedef U_32 BOOLEAN;
typedef double SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

/* no prioroties on AIX */
#define J9_PRIORITY_MAP {\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO,\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO,\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO,\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO}

#define TOC_UNWRAP_ADDRESS(wrappedPointer) ((void *) (wrappedPointer)[0])
#define TOC_STORE_TOC(dest,wrappedPointer) (dest = ((UDATA*)wrappedPointer)[1])

#endif
/*IA64 */

#if defined(J9IA64)
/* copied from AIX/RS6000 only UDATA changed */
#define DATA_TYPES_DEFINED
typedef unsigned long long				UDATA;
typedef unsigned long long	U_64;
typedef unsigned int				U_32;
typedef unsigned short			U_16;
typedef unsigned char			U_8;
typedef signed long long		IDATA;
typedef long long					I_64;
typedef signed int				I_32;
typedef signed short				I_16;
typedef signed char				I_8;	
typedef U_32						BOOLEAN;
typedef double					SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"

/* no prioroties on AIX */
#define J9_PRIORITY_MAP {\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO,\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO,\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO,\
	DEFAULT_PRIO, DEFAULT_PRIO, DEFAULT_PRIO}

#endif

/* SCO 386 ANSI compiler (gcc) */

#ifdef SCO

typedef double SYS_FLOAT;
typedef long long					I_64;
typedef unsigned long long	U_64;

#define NO_LVALUE_CASTING
#define FLOAT_EXTENDED	long double
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#endif

/* Solaris ANSI compiler */

#ifdef SOLARIS
#ifndef J9OSE
typedef long long					I_64;
typedef unsigned long long	U_64;
typedef double					SYS_FLOAT;

#define NO_LVALUE_CASTING
#define PLATFORM_LINE_DELIMITER	"\012"
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#endif /* J9OSE */
#endif

/* VXWORKS */

#ifdef VXWORKS

typedef long long I_64;
typedef unsigned long long U_64;

#define DATA_TYPES_DEFINED
typedef unsigned int			UDATA;
typedef unsigned int			U_32;
typedef unsigned short		U_16;
typedef unsigned char		U_8;
typedef int						IDATA;
typedef int						I_32;
typedef short					I_16;
typedef signed char			I_8;
typedef UDATA				BOOLEAN;
typedef double				SYS_FLOAT;

#define NO_LVALUE_CASTING
#ifndef PLATFORM_LINE_DELIMITER
#define PLATFORM_LINE_DELIMITER	"\012"
#endif
#ifndef DIR_SEPARATOR
#define DIR_SEPARATOR '/'
#define DIR_SEPARATOR_STR "/"
#endif
#define J9_DEFAULT_SCHED SCHED_RR
#define J9_PRIORITY_MAP { 16,15,14,13,12,11,10,9,8,7,6,5 }

#endif
/* Win32 - Windows 3.1 & NT using Win32 */

#ifdef WIN32
#ifndef BREW

#ifdef WIN32_IBMC
typedef long long I_64;
typedef unsigned long long	 U_64;
#else
typedef __int64					I_64 ;
typedef unsigned __int64	U_64 ;
#endif

typedef double 					SYS_FLOAT;

#define NO_LVALUE_CASTING
#define VMAPICALL _stdcall
#define VMCALL _cdecl
#define EXE_EXTENSION_CHAR	'.'
#define DIR_SEPARATOR '\\'
#define DIR_SEPARATOR_STR "\\"

/* Modifications for the Alpha running WIN-NT */
#ifdef _ALPHA_
#undef small  /* defined as char in rpcndr.h */
typedef double	FLOAT_EXTENDED;
#endif

/* (on WinCE, there's another priority: THREAD_PRIORITY_ABOVE_IDLE) */
#define J9_PRIORITY_MAP {	\
	THREAD_PRIORITY_IDLE,							/* 0 */\
	THREAD_PRIORITY_LOWEST,					/* 1 */\
	THREAD_PRIORITY_BELOW_NORMAL,	/* 2 */\
	THREAD_PRIORITY_BELOW_NORMAL,	/* 3 */\
	THREAD_PRIORITY_BELOW_NORMAL,	/* 4 */\
	THREAD_PRIORITY_NORMAL,						/* 5 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 6 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 7 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 8 */\
	THREAD_PRIORITY_ABOVE_NORMAL,		/* 9 */\
	THREAD_PRIORITY_HIGHEST,					/*10 */\
	THREAD_PRIORITY_TIME_CRITICAL			/*11 */}
#endif
#endif


#ifndef	VMCALL
#define	VMCALL
#define	VMAPICALL
#endif

/* Provide some reasonable defaults for the VM "types":

	UDATA			unsigned data, can be used as an integer or pointer storage.
	IDATA			signed data, can be used as an integer or pointer storage.
	U_64 / I_64	unsigned/signed 64 bits.
	U_32 / I_32	unsigned/signed 32 bits.
	U_16 / I_16	unsigned/signed 16 bits.
	U_8 / I_8		unsigned/signed 8 bits (bytes -- not to be confused with char)
	BOOLEAN	something that can be zero or non-zero.

*/

#ifndef	DATA_TYPES_DEFINED

typedef unsigned int			UDATA;
typedef unsigned int			U_32;
typedef unsigned short		U_16;
typedef unsigned char		U_8;
/* no generic U_64 or I_64 */

typedef int					IDATA;
typedef int					I_32;
typedef short				I_16;
typedef char				I_8;


/* temp hack -- don't typedef BOOLEAN since it's already def'ed on Win32 */
#define BOOLEAN UDATA


#endif

#ifndef J9_DEFAULT_SCHED
/* by default, pthreads platforms use the SCHED_OTHER thread scheduling policy */
#define J9_DEFAULT_SCHED SCHED_OTHER
#endif

#ifndef J9_PRIORITY_MAP
/* if no priority map if provided, priorities will be determined algorithmically */
#endif

#ifndef VXWORKS
#ifndef	FALSE
#define	FALSE		((BOOLEAN) 0)
#endif

#ifndef TRUE
#define	TRUE		((BOOLEAN) (!FALSE))
#endif
#endif

#ifndef NULL
#ifdef __cplusplus
#define NULL    (0)
#else
#define NULL    ((void *)0)
#endif
#endif

#define USE_PROTOTYPES
#ifdef	USE_PROTOTYPES
#define	PROTOTYPE(x)	x
#define	VARARGS		, ...
#else
#define	PROTOTYPE(x)	()
#define	VARARGS
#endif

/* Assign the default line delimiter if it was not set */
#ifndef PLATFORM_LINE_DELIMITER
#define PLATFORM_LINE_DELIMITER	"\015\012"
#endif

/* Set the max path length if it was not set */
#ifndef MAX_IMAGE_PATH_LENGTH
#define MAX_IMAGE_PATH_LENGTH	(2048)
#endif

typedef	double	ESDOUBLE;
typedef	float		ESSINGLE;

/* helpers for U_64s */
#define CLEAR_U64(u64)  (u64 = (U_64)0)

#ifdef	J9VM_ENV_LITTLE_ENDIAN
#define	LOW_LONG(l)	(*((U_32 *) &(l)))
#define	HIGH_LONG(l)	(*(((U_32 *) &(l)) + 1))
#else
#define	HIGH_LONG(l)	(*((U_32 *) &(l)))
#define	LOW_LONG(l)	(*(((U_32 *) &(l)) + 1))
#endif

#define	I8(x)			((I_8) (x))
#define	I8P(x)			((I_8 *) (x))
#ifndef ITRON									/* Was conflicting */
#define	U16(x)			((U_16) (x))
#define	I16(x)			((I_16) (x))
#endif
#define	I16P(x)			((I_16 *) (x))
#ifndef ITRON									/* Was conflicting */
#define	U32(x)			((U_32) (x))
#define	I32(x)			((I_32) (x))
#endif
#define	I32P(x)			((I_32 *) (x))
#define	U16P(x)			((U_16 *) (x))
#define	U32P(x)			((U_32 *) (x))
#define	OBJP(x)			((J9Object *) (x))
#define	OBJPP(x)		((J9Object **) (x))
#define	OBJPPP(x)		((J9Object ***) (x))
#define	CLASSP(x)		((Class *) (x))
#define	CLASSPP(x)		((Class **) (x))
#define	BYTEP(x)		((BYTE *) (x))

/* Test - was conflicting with OS2.h */
#define	ESCHAR(x)		((CHARACTER) (x))
#define	FLT(x)			((FLOAT) x)
#define	FLTP(x)			((FLOAT *) (x))

#ifdef	NO_LVALUE_CASTING
#define	LI8(x)			(*((I_8 *) &(x)))
#define	LI8P(x)			(*((I_8 **) &(x)))
#define	LU16(x)			(*((U_16 *) &(x)))
#define	LI16(x)			(*((I_16 *) &(x)))
#define	LU32(x)			(*((U_32 *) &(x)))
#define	LI32(x)			(*((I_32 *) &(x)))
#define	LI32P(x)		(*((I_32 **) &(x)))
#define	LU16P(x)		(*((U_16 **) &(x)))
#define	LU32P(x)		(*((U_32 **) &(x)))
#define	LOBJP(x)		(*((J9Object **) &(x)))
#define	LOBJPP(x)		(*((J9Object ***) &(x)))
#define	LOBJPPP(x)		(*((J9Object ****) &(x))
#define	LCLASSP(x)		(*((Class **) &(x)))
#define	LBYTEP(x)		(*((BYTE **) &(x)))
#define	LCHAR(x)		(*((CHARACTER) &(x)))
#define	LFLT(x)			(*((FLOAT) &x))
#define	LFLTP(x)		(*((FLOAT *) &(x)))
#else
#define	LI8(x)			I8((x))
#define	LI8P(x)			I8P((x))
#define	LU16(x)			U16((x))
#define	LI16(x)			I16((x))
#define	LU32(x)			U32((x))
#define	LI32(x)			I32((x))
#define	LI32P(x)		I32P((x))
#define	LU16P(x)		U16P((x))
#define	LU32P(x)		U32P((x))
#define	LOBJP(x)		OBJP((x))
#define	LOBJPP(x)		OBJPP((x))
#define	LOBJPPP(x)		OBJPPP((x))
#define	LIOBJP(x)		IOBJP((x))
#define	LCLASSP(x)		CLASSP((x))
#define	LBYTEP(x)		BYTEP((x))
#define	LCHAR(x)		CHAR((x))
#define	LFLT(x)			FLT((x))
#define	LFLTP(x)		FLTP((x))
#endif

/* Macros for converting between words and longs and accessing bits */

#define	HIGH_WORD(x)	U16(U32((x)) >> 16)
#define	LOW_WORD(x)		U16(U32((x)) & 0xFFFF)
#define	LOW_BIT(o)		(U32((o)) & 1)
#define	LOW_2_BITS(o)	(U32((o)) & 3)
#define	LOW_3_BITS(o)	(U32((o)) & 7)
#define	LOW_4_BITS(o)	(U32((o)) & 15)
#define	MAKE_32(h, l)	((U32((h)) << 16) | U32((l)))
#define	MAKE_64(h, l)	((((I_64)(h)) << 32) | (l))

#ifdef __cplusplus
#define J9_CFUNC "C"
#define J9_CDATA "C"
#else
#define J9_CFUNC 
#define J9_CDATA
#endif

/* Macros for tagging functions which read/write the vm thread */

#define READSVMTHREAD
#define WRITESVMTHREAD
#define REQUIRESSTACKFRAME

/* macro for tagging functions which never return */
#ifdef __GNUC__
/* on GCC, we can actually pass this information on to the compiler */
#define NORETURN __attribute__((noreturn))
#else
#define NORETURN
#endif

/* on some systems (e.g. LinuxPPC) va_list is an array type.  This is probably in
 * violation of the ANSI C spec, but it's not entirely clear.  Because of this, we end
 * up with an undesired extra level of indirection if we take the address of a
 * va_list argument. 
 *
 * To get it right ,always use the VA_PTR macro
 */
#ifndef VA_PTR
#define VA_PTR(valist) (&valist)
#endif

/* Macros used on RS6000 to manipulate wrapped function pointers */
#ifndef TOC_UNWRAP_ADDRESS
#define TOC_UNWRAP_ADDRESS(wrappedPointer) (wrappedPointer)
#endif
#ifndef TOC_STORE_TOC
#define TOC_STORE_TOC(dest,wrappedPointer)
#endif

#endif /* escomp_h */

