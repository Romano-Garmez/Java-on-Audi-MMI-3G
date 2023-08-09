#ifndef LIBHLP_H
#define LIBHLP_H

#ifdef __cplusplus
extern "C" {
#endif


/*
	(c) Copyright IBM Corp. 1998, 2003  All Rights Reserved
	Headers for exelib functions.

	File generated: (3/13/03 5:55:48 PM)*/

#include "j9comp.h"
#include "j9port.h"
#include "jni.h"


typedef struct J9StringBuffer {
    UDATA remaining;
    U_8 data[4];
} J9StringBuffer;

#define J9SIZEOF_J9StringBuffer 8

typedef struct J9IVERelocatorStruct {
    UDATA dllHandle;
    I_32  (VMCALL *iveLoadJarFromFile) (
	J9PortLibrary *portLib,
	char          *fileName,
	void         **jarPtr,
	void         **allocPtr
	) ;
    char*  (VMCALL *iveRelocateMessage) (int returnCode) ;
    I_32  (VMCALL *iveFindFileInJar) (
	void          *jarPtr,
	char          *fileName,
	I_32           fileNameLength,
	char         **fileContents,
	I_32          *fileContentsLength
	) ;
    void*  (VMCALL *iveGetJarInfoValues) (
	J9PortLibrary *portLib,
	void          *jarPtr,
	I_32           *_count,
	char        ***_keys,
	char        ***_vals
	) ;
    void  (VMCALL *iveFreeJarInfoValues) (
	J9PortLibrary *portLib,
	void          *jarInfoValues
	) ;
    void* romImage[2];
    struct J9JXEInfo* romImageInfo;
    void* jarPtr;
    void* jarAllocPtr;
    void* jarInfo;
    I_32 jarInfoCount;
    char** jarInfoKeys;
    char** jarInfoVals;
} J9IVERelocatorStruct;

#define J9SIZEOF_J9IVERelocatorStruct 60

typedef struct J9JXEInfo {
    struct J9ROMImageHeader* imageHeader;
    void* jxePointer;
    void* jxeAlloc;
    struct J9ClassLoader* classLoader;
    U_8* filename;
    UDATA flags;
    UDATA referenceCount;
} J9JXEInfo;

#define J9SIZEOF_J9JXEInfo 28

#define J9JXE_USER_CLASS_PATH  2
#define J9JXE_FLAGS_NAME_ALLOCATED  32
#define J9JXE_AOT_UNUSABLE  4
#define J9JXE_BOOT_CLASS_PATH  1
#define J9JXE_CLASS_PATH_MASK  3
#define J9JXE_UNUSABLE  8
#define J9JXE_FLAGS_ALLOCATED  16

struct j9cmdlineOptions {
	int argc;
	char** argv;
	char** envp;
	J9PortLibrary* portLibrary;
};

extern J9_CFUNC J9StringBuffer* strBufferCat PROTOTYPE((J9PortLibrary *portLib, J9StringBuffer* buffer, char* string));
extern J9_CFUNC char* strBufferData PROTOTYPE((J9StringBuffer* buffer));
extern J9_CFUNC J9StringBuffer* strBufferPrepend PROTOTYPE((J9PortLibrary *portLib, J9StringBuffer* buffer, char* string));
extern J9_CFUNC J9StringBuffer* strBufferEnsure PROTOTYPE((J9PortLibrary *portLib, J9StringBuffer* buffer, UDATA len));
extern J9_CFUNC I_32 main_prependToClassPath PROTOTYPE(( J9PortLibrary *portLib, U_16 sep, J9StringBuffer **classPath, char *toPrepend));
extern J9_CFUNC IDATA main_initializeJavaLibraryPath PROTOTYPE((J9PortLibrary * portLib, J9StringBuffer **finalJavaLibraryPath, char *argv0));
extern J9_CFUNC IDATA main_initializeBootLibraryPath PROTOTYPE((J9PortLibrary * portLib, J9StringBuffer **finalBootLibraryPath, char *argv0));
extern J9_CFUNC I_32 main_initializeClassPath PROTOTYPE(( J9PortLibrary *portLib, J9StringBuffer** classPath));
extern J9_CFUNC IDATA main_initializeJavaHome PROTOTYPE((J9PortLibrary * portLib, J9StringBuffer **finalJavaHome, int argc, char **argv));
extern J9_CFUNC I_32 main_findMainClassAndSystemProps PROTOTYPE(( J9PortLibrary *j9portLibrary, char **mainClass, int *jarSysProps, int jarInfoCount, char **jarInfoKeys, char **jarInfoVals));
extern J9_CFUNC int main_runJavaMain PROTOTYPE((JNIEnv * env, char *mainClassName, int nameIsUTF, int java_argc, char **java_argv, J9PortLibrary * j9portLibrary));
extern J9_CFUNC I_32 main_appendToClassPath PROTOTYPE(( J9PortLibrary *portLib, U_16 sep, J9StringBuffer **classPath, char *toAppend));
extern J9_CFUNC char * main_vmVersionString PROTOTYPE((void));
extern J9_CFUNC BOOLEAN vmOptionsTableAddExeName PROTOTYPE((
	void **vmOptionsTable, 
	char  *argv0
	));
extern J9_CFUNC void describeInternalOptions PROTOTYPE((J9PortLibrary *portLib));
extern J9_CFUNC int vmOptionsTableGetCount PROTOTYPE((
	void **vmOptionsTable
	));
extern J9_CFUNC BOOLEAN vmOptionsTableAddOption PROTOTYPE((
	void **vmOptionsTable, 
	char  *optionString,
	void  *extraInfo
	));
extern J9_CFUNC BOOLEAN vmOptionsTableParseArgs PROTOTYPE((
	J9PortLibrary *portLib,
	void **vmOptionsTable,
	int    argc, 
	char  *argv[]
	));
extern J9_CFUNC JavaVMOption *vmOptionsTableGetOptions PROTOTYPE((
	void **vmOptionsTable
	));
extern J9_CFUNC BOOLEAN vmOptionsTableAddXRunOption PROTOTYPE((
	void **vmOptionsTable, 
	char  *option
	));
extern J9_CFUNC void vmOptionsTableDestroy PROTOTYPE((
	void **vmOptionsTable
	));
extern J9_CFUNC BOOLEAN vmOptionsTableParseArgInternal PROTOTYPE((
	J9PortLibrary *portLib,
	void **vmOptionsTable,
	char  *argv
	));
extern J9_CFUNC BOOLEAN vmOptionsTableParseArg PROTOTYPE((
	J9PortLibrary *portLib,
	void **vmOptionsTable,
	int *argc,
	char  *argv[]
	));
extern J9_CFUNC BOOLEAN vmOptionsTableAddOptionWithCopy PROTOTYPE((
	void **vmOptionsTable, 
	char  *optionString,
	void  *extraInfo
	));
extern J9_CFUNC void vmOptionsTableInit PROTOTYPE((
	J9PortLibrary    *portLib,
	void            **vmOptionsTable,
	int              initialCount
	));
extern J9_CFUNC UDATA VMCALL memoryCheck_parseCmdLine PROTOTYPE(( J9PortLibrary *portLibrary, UDATA lastLegalArg , char **argv ));
extern J9_CFUNC IDATA VMCALL memoryCheck_initialize PROTOTYPE((J9PortLibrary * portLib, char const *modeStr));
extern J9_CFUNC void VMCALL memoryCheck_shutdown PROTOTYPE((J9PortLibrary * portLib));
extern J9_CFUNC BOOLEAN VMCALL remoteConsole_startup PROTOTYPE(( J9PortLibrary *portLibrary, char *hName, char *port, j9linger_t *lgrval ));
extern J9_CFUNC UDATA VMCALL remoteConsole_parseCmdLine PROTOTYPE(( J9PortLibrary *portLibrary, UDATA lastLegalArg , char **argv ));

#ifdef __cplusplus
}
#endif

#endif /* LIBHLP_H */
