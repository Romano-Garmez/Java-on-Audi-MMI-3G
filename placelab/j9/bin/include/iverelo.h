/*
*	(c) Copyright IBM Corp. 1999, 2003 All Rights Reserved
*/

/*------------------------------------------------------------------
 * iverelo.h : J9 functions to relocate jxe/rom images
 *------------------------------------------------------------------*/
 
#ifndef IVERELO_H
#define IVERELO_H

#define IVERELO_LOGFILE "iverelo.log"

/*------------------------------------------------------------------
 * rom image signature, supported versions
 *------------------------------------------------------------------*/
#define IVE_RELO_ROMIMAGE_SIG   0x4A39394A

/*------------------------------------------------------------------
 * return codes
 *------------------------------------------------------------------*/
#define IVE_RELO_RC_OK                       0
#define IVE_RELO_RC_READ_FAILED              1
#define IVE_RELO_RC_WRITE_FAILED             2
#define IVE_RELO_RC_UNKNOWN_DATA             3
#define IVE_RELO_RC_FILE_NOT_FOUND           4
#define IVE_RELO_RC_FILE_ERROR               5
#define IVE_RELO_RC_OUT_OF_MEMORY            6
#define IVE_RELO_RC_INTERNAL_ERROR           7
#define IVE_RELO_RC_ROMIMAGE_ERROR	         8
#define IVE_RELO_RC_INVALID_JAR		           9
#define IVE_RELO_RC_ROMIMAGE_COMPRESSED     10
#define IVE_RELO_RC_ROMIMAGE_ENCRYPTED      11
#define IVE_RELO_RC_NO_INLINE_SIZE	        12
#define IVE_RELO_RC_ROMIMAGE_ALIGN          13
#define IVE_RELO_RC_READ_EOF                14
#define IVE_RELO_RC_WRITE_EOF               15
#define IVE_RELO_RC_NOT_SUPPORTED           16
#define IVE_RELO_RC_ROMIMAGE_WRONG_INT_SIZE	17
#define IVE_RELO_RC_ROMIMAGE_WRONG_ENDIAN   18
#define IVE_RELO_RC_ROMIMAGE_WRONG_VERSION  19
#define IVE_RELO_RC_ROMIMAGE_WRONG_AOT_PLATFORM 20
#define IVE_RELO_RC_ROMIMAGE_AOT_NOT_SUPPORTED 21

/*------------------------------------------------------------------
 * macros
 *------------------------------------------------------------------*/
#if !defined(max)
#define max(a,b) (((a) > (b)) ? (a) : (b))
#endif
#if !defined(min)
#define min(a,b) (((a) < (b)) ? (a) : (b))
#endif

/*------------------------------------------------------------------
 * logging
 *
 * To use logging, use the IVE_LOG macro in your code.  The first
 * parameter should be a file name, and the rest just like printf().
 * For example, 
 *    IVE_LOG(("my.log","My name is %s",myName));
 * Note the double parenthesis.
 * New lines will be added automatically.  Do NOT use iveLog().  IVE_LOG()
 * will become iveLog() via #define, as required.
 *
 * Two other conditions must hold for logging to run.  The symbol
 * IVE_LOGGING_AVAILABLE must be defined.  Also, the function
 * iveSetLogging(1) must have been run.  Using 0 will subsequently 
 * turn logging off.
 *------------------------------------------------------------------*/
 
#if defined(IVE_LOGGING_AVAILABLE)
#define IVE_LOG(x) iveLog x
#else
#define IVE_LOG(x)
#endif

/*------------------------------------------------------------------
 * do not use this function; use the IVE_LOG macro instead!
 *------------------------------------------------------------------*/
void iveLog(char *fileName, char *format, ...);

/*------------------------------------------------------------------
 * turn logging on or off
 *------------------------------------------------------------------*/
void iveSetLogging(int onOrOff);

/*------------------------------------------------------------------
 * buffer used by relocator
 *------------------------------------------------------------------*/
typedef struct {
	U_8  *buffer;
	U_32  length;
	U_32  offset;
	U_32  offsetTotal;
} ReloBuffer;

/*------------------------------------------------------------------
 * read/write control block
 * - newAddress is the address where the new JAR should be relocated to
 * - length is the length of the JAR
 * - readUserData is a pointer to user data for reading
 * - writeUserData is a pointer to user data for writing
 * - romImagePtr will be set to the pointer to a j9 rom image, if found
 * - the rest are functions which must be defined by person doing relocation
 *------------------------------------------------------------------*/
struct ReloData_;
typedef struct ReloData_ ReloData;

struct ReloData_ {
	void  *newAddress;
	U_32   length;
	void  *readUserData;
	void  *writeUserData;
	J9PortLibrary *localPortLibrary;

	I_32 (JNICALL *ReloRead)(
		ReloData  *reloData,
		U_8      **buffer,
		U_32      *bufferLen
		);

	I_32 (JNICALL *ReloRead8)(
		ReloData  *reloData,
		U_8       *buffer,
		U_32      *jxeOffset
		);

	I_32 (JNICALL *ReloRead16)(
		ReloData  *reloData,
		U_16      *buffer,
		U_32      *jxeOffset
		);

	I_32 (JNICALL *ReloRead32)(
		ReloData  *reloData,
		U_32      *buffer,
		U_32      *jxeOffset
		);

	I_32 (JNICALL *ReloRead64)(
		ReloData  *reloData,
		U_64      *buffer,
		U_32      *jxeOffset
		);

	I_32 (JNICALL *ReloGetWriteBuffer)(
		ReloData  *reloData,
		U_8      **buffer,
		U_32      *bufferLen
		);

	I_32 (JNICALL *ReloWrite)(
		ReloData  *reloData,
		U_8       *buffer,
		U_32       bufferLen
		);
		
};

/*------------------------------------------------------------------
 * names
 *------------------------------------------------------------------*/
#define IVE_JAR_INFO_NAME   "META-INF/JXE.MF"
#define IVE_ROM_IMAGE_NAME  "rom.classes"

/*------------------------------------------------------------------
 * get next entry in the jar; returns handle to get next one; the
 * first time in, pass in the pointer to the jar.  The return value
 * should be passed in as the jarPtr the next call.  NULL will be
 * returned when there are no more entries.
 *------------------------------------------------------------------*/
void *VMCALL iveGetNextJarEntry(
	void  *jarPtr,
	char **fileName,
	I_32  *fileNameLength,
	void **fileContents,
	I_32  *fileContentsLength
	);

/*------------------------------------------------------------------
 * Given a file name, return a pointer to the file and it's length
 * You may pass -1 for fileNameLength to have strlen() run on it 
 * internally.
 * Answers 0 if not found, 1 if found
 *------------------------------------------------------------------*/
I_32 VMCALL iveFindFileInJar(
	void          *jarPtr,
	char          *fileName,
	I_32           fileNameLength,
	char         **fileContents,
	I_32          *fileContentsLength
	);

/*------------------------------------------------------------------
 * macro to find rom image
 *------------------------------------------------------------------*/
#define iveFindRomImageInJar(portLib,jarPtr,fileContents,fileContentsLength) \
	(iveFindFileInJar(jarPtr,IVE_ROM_IMAGE_NAME,-1,fileContents,fileContentsLength))

/*------------------------------------------------------------------
 * macro to find jar info 
 *------------------------------------------------------------------*/
#define iveFindJarInfoInJar(portLib,jarPtr,fileContents,fileContentsLength) \
	(iveFindFileInJar(jarPtr,IVE_JAR_INFO_NAME,-1,fileContents,fileContentsLength))

/*------------------------------------------------------------------
 * Get the keys and values from the jar info file. 
 * Returns a pointer to allocated memory which must be freed by
 * calling iveFreeJarInfoValues()
 *------------------------------------------------------------------*/
void * VMCALL iveGetJarInfoValues(
	J9PortLibrary *portLib,
	void          *jxePtr,
	I_32          *count,
	char        ***keys,
	char        ***vals
	);

/*------------------------------------------------------------------
 * get a pointer to pass to iveGetJarInfoValue().  Note, this
 * pointer must be freed with iveFreeJarInfoValues()
 *------------------------------------------------------------------*/
void VMCALL iveFreeJarInfoValues(
	J9PortLibrary *portLib,
	void          *jarInfoValues
	);

/*------------------------------------------------------------------
 * Get the value associated with the first matching key found in 
 * the jar info file. 
 * Returns a pointer to the value or NULL if not found.  The pointer
 * points into the jxe, so does not need to be freed. 
 *------------------------------------------------------------------*/
char * VMCALL iveGetJarInfoValue(
	void          *jxePtr,
	char          *findKey
	);

/*------------------------------------------------------------------
 * Macro to get the name of the jxe
 *------------------------------------------------------------------*/
#define iveGetJxeName(jxePtr) \
	(iveGetJarInfoValue(jxePtr,"jxeName"))

/*------------------------------------------------------------------
 * Macro to get the uuid of the jxe, something like:
 *    35F22BA0-561D-11D3-802C-80D9B6989DBE
 *------------------------------------------------------------------*/
#define iveGetJxeUUID(jxePtr) \
	(iveGetJarInfoValue(jxePtr,"uuid"))

/*------------------------------------------------------------------
 * Macro to inquire if a jxe is big endian
 *------------------------------------------------------------------*/
#define iveIsJxeBigEndian(jxePtr) \
	(*iveGetJarInfoValue(jxePtr,"bigEndian")=='1')

/*------------------------------------------------------------------
 * Macro to inquire if a jxe is interpretable (no bytecode stripped)
 *------------------------------------------------------------------*/
#define iveIsJxeInterpretable(jxePtr) \
	(iveGetJarInfoValue(jxePtr,"interpretable")==NULL || \
	*iveGetJarInfoValue(jxePtr,"interpretable")=='1')

/*------------------------------------------------------------------
 * Macro to inquire if a jxe is position independent
 *------------------------------------------------------------------*/
#define iveIsJxePositionIndependent(jxePtr) \
	(iveGetJarInfoValue(jxePtr,"posIndependent")!=NULL && \
	*iveGetJarInfoValue(jxePtr,"posIndependent")=='1')

/*------------------------------------------------------------------
 * answer a rom image pointer from a jxe pointer
 *------------------------------------------------------------------*/
void * VMCALL iveRomImagePointerFromJxePointer(
	void *jxePointer
	);

/*------------------------------------------------------------------
 * answer a jxe pointer from a rom image pointer
 *------------------------------------------------------------------*/
void *VMCALL iveJxePointerFromRomImagePointer(
	void *romImagePointer
	);

/*------------------------------------------------------------------
 * generic streaming relocation function
 *------------------------------------------------------------------*/
I_32 VMCALL iveRelocate(
	J9PortLibrary *portLib,
	ReloData      *reloData
	);

/*------------------------------------------------------------------
 * return the message associated with a return code
 *------------------------------------------------------------------*/
char *VMCALL iveRelocateMessage(int returnCode);

/*------------------------------------------------------------------
 * load an image from a file
 *------------------------------------------------------------------*/
I_32 VMCALL iveLoadJarFromFile(
	J9PortLibrary *portLib,
	char          *fileName,
	void         **jarPtr,
	void         **allocPtr
	);

/*------------------------------------------------------------------
 * relocate a jxe in place
 *------------------------------------------------------------------*/
I_32 VMCALL iveRelocateInPlace(
	J9PortLibrary    *portLib,
	void             *jxePointer
	);

/* PalmOS APIs */
void** VMCALL iveGetImageList(void* info);
void** VMCALL iveGetInfoList(void* info);
I_32 VMCALL iveLoadJXEs(J9PortLibrary* portLibrary, void** info, void* ref);
void VMCALL iveUnloadJXEs(void* infoPtr);

#endif

