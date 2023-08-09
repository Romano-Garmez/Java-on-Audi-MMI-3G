/*
*	(c) Copyright IBM Corp. 1991, 2003 All Rights Reserved
*/

#if defined(J9WINCE) && defined(_WIN32_WCE_EMULATION)
/* Required for exit() workaround in exitHook. */
#include <windows.h>
#endif

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "jni.h"
#include "j9port.h"
#include "j9lib.h"

#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
#include "iverelo.h"
#endif

#include "libhlp.h"

#ifdef NEUTRINO
#include <sys/mman.h>
#include <ctype.h>
#endif

extern void *j9Jar;

/* Used by exitHook(). */
static J9PortLibrary *initialPortLibrary = NULL;

static void dumpHelpText PROTOTYPE(( J9PortLibrary *portLib, int argc, char **argv, int *copyrightWritten));
void JNICALL NORETURN exitHook PROTOTYPE((jint exitCode));
int main PROTOTYPE((int argc, char ** argv, char ** envp));
static void dumpVersionInfo PROTOTYPE((J9PortLibrary * portLib, int argc, char **argv, int *copyrightWritten));
UDATA VMCALL gpProtectedMain PROTOTYPE((void *arg));
static I_32 initDefaultDefines PROTOTYPE(( J9PortLibrary *portLib, void **vmOptionsTable, int argc, char **argv, int jxeArg, int jarArg, J9StringBuffer **classPathInd, J9StringBuffer **javaHomeInd, J9StringBuffer **bootLibraryPathInd, J9StringBuffer **javaLibraryPathInd));
#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static int addJxeOptions PROTOTYPE((J9PortLibrary *portLib, J9IVERelocatorStruct* ive, void** vmOptionsTable));
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */



char * vmDetailString PROTOTYPE(( J9PortLibrary *portLib ));
static void dumpCopyrights PROTOTYPE(( J9PortLibrary *portLib, int argc, char **argv, int *copyrightWritten));
#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static J9IVERelocatorStruct* initializeIveRelocator PROTOTYPE((J9PortLibrary* portLib));
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */



U_32 parseStringToLong PROTOTYPE((char* strarg));
#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static int handleNeutrinoJxeSpaceArg PROTOTYPE((J9PortLibrary * portLib, J9IVERelocatorStruct * ive, int jxeSpaceArg, int jxeAddrArg, char **argv));
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */



#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static void shutdownIveRelocator PROTOTYPE((J9PortLibrary* portLib, J9IVERelocatorStruct* relocator));
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */



#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static int initializeJxe PROTOTYPE((J9PortLibrary* portLib, J9IVERelocatorStruct* ive, char** mainClass ));
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */





#ifdef NEUTRINO
/*  Added due to jxespace option.  Used to parse string values into hex long equivalents from the command line 
	Assumes a hex string of 8 characters or less and terminate on a space, comma, or end of string.
	Returns a long hex value.  
*/   
U_32 parseStringToLong(char* strarg)
{ 
	char value[16];		/* hopefully 16 bytes is enough on all platforms */

	if (isxdigit(strarg[0])) {
		int i;
		for (i = 0; i < 8; i++) {
			if (isxdigit(strarg[i])) {
				value[i] = strarg[i];

				if (strarg[i+1] == ',' || 	strarg[i+1] == 0x20 || strarg[i+1] == 0x0) {
					value[i+1] = 0x0;
					return atoh(&value[0]);
				}
			} else {
				return 0;
			}
		}
	} 
	return 0;
}

#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static int handleNeutrinoJxeSpaceArg(J9PortLibrary * portLib, J9IVERelocatorStruct * ive, int jxeSpaceArg, int jxeAddrArg,
									 char **argv)
{
	PORT_ACCESS_FROM_PORT(portLib);
	char *tmpargv = &argv[jxeSpaceArg][10];
	char *partitionStr = NULL;
	char *virtStr = NULL;
	int correctParms = 0;

	/* parse out the jxespace options, all parameters are expected to be hex */
	ive->physicalAddr = parseStringToLong(tmpargv);
	partitionStr = strchr(tmpargv, ',');
	if (partitionStr) {
		ive->jxeSpaceSize = parseStringToLong(partitionStr + 1);
		correctParms = 1;
		virtStr = strchr(partitionStr + 1, ',');
		if (virtStr) {
			ive->logicalAddr = (void *) parseStringToLong(virtStr + 1);
		}
	}

	/* Don't continue if parsing failed or requested size is 0 */
	if (!correctParms || !ive->jxeSpaceSize) {
		j9tty_printf(PORTLIB, "\nInvalid jxespace parameters.\n");
		return 8;
	}

	/* Get the logical mapped addr. */
	ive->mappedAddr =
		mmap_device_memory(ive->logicalAddr, ive->jxeSpaceSize, PROT_READ | PROT_NOCACHE, 0, ive->physicalAddr);
	if (ive->mappedAddr == MAP_FAILED) {
		j9tty_printf(PORTLIB, "\nError mapping jxe in flash\n");
		return 7;
	}

	if (jxeAddrArg) {
		tmpargv = &argv[jxeAddrArg][9];
		ive->jxeAddr = (void *) parseStringToLong(tmpargv);

		if (ive->jxeAddr >= ive->mappedAddr && ive->jxeAddr <= (ive->mappedAddr + ive->jxeSpaceSize)) {
			ive->jarPtr = ive->jxeAddr;
		} else {
			j9tty_printf(PORTLIB,
						 "\njxeaddr location 0x%08X outside of range 0x%08X to 0x%08X\n",
						 ive->jxeAddr, ive->mappedAddr, (ive->mappedAddr + ive->jxeSpaceSize));
			return 7;
		}
	} else {
		j9tty_printf(PORTLIB,
					 "\nLogical mapped addr=0x%X for physical address at 0x%X and size 0x%X\n",
					 ive->mappedAddr, ive->physicalAddr, ive->jxeSpaceSize);
		return 7;
	}

	return 0;
}

#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */


#endif


static void dumpHelpText( J9PortLibrary *portLib, int argc, char **argv, int *copyrightWritten)
{
	PORT_ACCESS_FROM_PORT(portLib);

	dumpCopyrights(portLib, argc, argv, copyrightWritten);

#ifdef J9VM_OPT_DYNAMIC_LOAD_SUPPORT
	j9tty_printf(PORTLIB, "Usage:\t%s [options] classname [args...]\n" , argv[0]);
#endif
#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
	j9tty_printf( PORTLIB, "Usage:\t%s [options] -jxe:<jxeFile> [args...]\n", argv[0]);
#endif

	j9tty_printf( PORTLIB, "\n[options]\n");

	j9tty_printf( PORTLIB, "  -classpath <path>\n");
	j9tty_printf( PORTLIB, "  -cp <path>       set classpath to <path>.\n");

#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
	j9tty_printf( PORTLIB, "  -jxe:<jxeFile>   run the named jxe file.\n");
#ifdef NEUTRINO
	j9tty_printf( PORTLIB, "  -jxespace:<physicalAddr>,<size>,<logicalAddr>\n");
	j9tty_printf( PORTLIB, "                   map memory region for jxes, (values are in hex).\n");
	j9tty_printf( PORTLIB, "  -jxeaddr:<logicalAddr>\n");
	j9tty_printf( PORTLIB, "                   run a jxe directly from memory, (address is in hex).\n");
#endif /* NEUTRINO */
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT */

	j9tty_printf( PORTLIB, "  -D<prop>=<val>   set the value of a system property.\n");
	j9tty_printf( PORTLIB, "  -debug:<port>    start a JDWP debug server on <port>.\n");
	j9tty_printf( PORTLIB, "  -jcl:midp         only the midp library is supported for j9midp\n");
	j9tty_printf( PORTLIB, "  -verbose[:class,gc,stack,sizes]\n");
	j9tty_printf( PORTLIB, "                   enable verbose output (default=class).\n");
	j9tty_printf( PORTLIB, "  -verify          enable class file verification.\n");
	j9tty_printf( PORTLIB, "  -X               print help on non-standard options.\n");

	return;
}  
 
/* Compute and return a string which is the nitty-gritty for the vm */

char * vmDetailString( J9PortLibrary *portLib )
{
	char ostype[256];
	char osversion[256];
	char osarch[64];
	static char detailString[sizeof(ostype) + sizeof(osversion) + sizeof(osarch)];
	PORT_ACCESS_FROM_PORT( portLib );

	j9sysinfo_get_OS_type(ostype);
	j9sysinfo_get_OS_version(osversion);
	j9sysinfo_get_CPU_architecture(osarch);

	j9str_printf (PORTLIB, detailString, sizeof(detailString), "%s (%s %s %s)", EsBuildVersionString, ostype, osversion, osarch);
	return detailString;
}

/* note - this cannot be static, since it's called externally by WinMain, for instance */
UDATA VMCALL gpProtectedMain(void *arg)
{
	struct j9cmdlineOptions *startupOptions = (struct j9cmdlineOptions *) arg;
	int argc = startupOptions->argc;
	char **argv = startupOptions->argv;
	J9StringBuffer *javaHome = NULL, *classPath = NULL, *bootLibraryPath = NULL, *javaLibraryPath = NULL;
	J9PortLibrary *j9portLibrary = startupOptions->portLibrary;
	JavaVM *jvm = NULL;
	JNIEnv *env = NULL;
	JavaVMInitArgs vm_args;
	int rc = 1, i, javaRc = 0;
	UDATA classArg = 0;
	char *mainClass = NULL;
	void *vmOptionsTable = NULL;
	int jxeArg = 0;
	int isStandaloneJar = 0;
	int copyrightWritten = 0;
	int versionWritten = 0;
	int isNameUTF = 0;

#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
	J9IVERelocatorStruct *ive = NULL;
#ifdef NEUTRINO
	int jxeSpaceArg = 0;
	int jxeAddrArg = 0;
#endif
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT */

    J9StringBuffer *jclArg = NULL;
#ifdef J9VM_STATIC_LINKAGE
    extern const char jclOption[];
#endif

	/* 
	   we start by scanning the command line, looking for the first arg that does not have a - in front of it.  This is 
	   taken to be the class we are trying to load. NB: skip argv[0], the exe name. 
	   Include some lightweight integrity checking.  */

	initialPortLibrary = j9portLibrary;

	for (i = 1; i < argc; i++) {
		if ('-' != argv[i][0]) {
			classArg = i;
			mainClass = argv[i];
			break;
		} else if ((strcmp("-cp", argv[i]) == 0) || (strcmp("-classpath", argv[i]) == 0)) {
			if ((i + 1) >= argc) {
				j9portLibrary->tty_printf(j9portLibrary, "\n%s requires a parameter\n", argv[i]);
				dumpHelpText(j9portLibrary, argc, argv, &copyrightWritten);
				return 0;
			}
 			i++;			/* Skip the next arg */
		}

		if (0 == strcmp("-version", argv[i])) {
			if(!versionWritten) {
				dumpVersionInfo(j9portLibrary, argc, argv, &copyrightWritten);
				versionWritten = 1;
			}
			return 0;
		}

		if (0 == strcmp("-showversion", argv[i])) {
			if(!versionWritten) {
				dumpVersionInfo(j9portLibrary, argc, argv, &copyrightWritten);
				versionWritten = 1;
			}
		}

		if ((0 == strcmp("-help", argv[i]))||(0 == strcmp("-?", argv[i]))) {
			dumpHelpText(j9portLibrary, argc, argv, &copyrightWritten);
			return 0;
		}

		if (0 == strcmp("-X", argv[i])) {
			describeInternalOptions(j9portLibrary);
			return 0;
		}

		if (strcmp("-jar", argv[i]) == 0) {
			isStandaloneJar = 1;
		}

#if defined(JITNOCERT)
        /* j9midp does not allow jit */
        if (!strncmp("-jit", argv[i], 4))
        {
            rc = 11;
            goto cleanup;

        }
#endif 
        /* j9midp does not allow use of any other lib than midp */
        if (!strncmp("-jcl:", argv[i], 5))
        {
            /* If the -jcl option is used it MUST be -jcl:midp */
            if (strncmp("-jcl:midp:loadlibrary=swt-motif-2104", argv[i], 36))
            {
                rc = 12;
                goto cleanup;
            }
        }


#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
		else if (!strncmp("-jxe:", argv[i], 5)) {
			classArg = i;
			jxeArg = i;
			break;
		}
#ifdef NEUTRINO
		else if (!strncmp("-jxespace:", argv[i], 10)) {
			classArg = i;
			jxeSpaceArg = i;
			for (i = 1; i < argc; i++) {
				if (!strncmp("-jxeaddr:", argv[i], 9)) {
					classArg = i;
					jxeAddrArg = i;
				}
			}
			break;
		} else {
			/* option is not for the exe (may be for VM though). */
		}
#endif /* NEUTRINO */
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT */
	}

#ifdef J9VM_OPT_REMOTE_CONSOLE_SUPPORT
	remoteConsole_parseCmdLine(j9portLibrary, (classArg ? classArg : argc) - 1, argv);
#endif

#ifdef J9VM_OPT_MEMORY_CHECK_SUPPORT
	/* This should happen before anybody allocates memory!  Otherwise, shutdown will not work properly. */
	memoryCheck_parseCmdLine(j9portLibrary, (classArg ? classArg : argc) - 1, argv);
#endif /* J9VM_OPT_MEMORY_CHECK_SUPPORT */

#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
	/* jxe support */
	if (jxeArg) {
		ive = initializeIveRelocator(j9portLibrary);
		if (ive == NULL) {
			rc = 10;
			goto cleanup;
		}
		rc = ive->iveLoadJarFromFile(j9portLibrary, &argv[jxeArg][5], &ive->jarPtr, &ive->jarAllocPtr);
		if (0 != rc) {
			j9portLibrary->tty_printf(j9portLibrary, "\nError loading jxe: %s\n", ive->iveRelocateMessage(rc));
			rc = 10;
			goto cleanup;
		}
		ive->romImageInfo->filename = &argv[jxeArg][5];
		ive->romImageInfo->jxePointer = ive->jarPtr;
		ive->romImageInfo->flags = J9JXE_BOOT_CLASS_PATH;	
	}
#ifdef NEUTRINO
	else if (jxeSpaceArg) {
		ive = initializeIveRelocator(j9portLibrary);
		if (ive == NULL) {
			rc = 9;
			goto cleanup;
		}
		rc = handleNeutrinoJxeSpaceArg(j9portLibrary, ive, jxeSpaceArg, jxeAddrArg, argv);
		if (rc)
			goto cleanup;
	}
#endif /* NEUTRINO */

	if (ive) {
		rc = initializeJxe(j9portLibrary, ive, &mainClass);
		if (rc != 0)
			goto cleanup;
		isNameUTF = 1;
	}
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT */

	if (!mainClass || (isStandaloneJar && jxeArg)) {
		/* no class midpd.  fail before we get any farther */
		dumpHelpText(j9portLibrary, argc, argv, &copyrightWritten);
		rc = 4;
		goto cleanup;
	}

	vmOptionsTable = NULL;

	vmOptionsTableInit(j9portLibrary, &vmOptionsTable, 15);
	if (NULL == vmOptionsTable)
		goto cleanup;

#ifdef J9VM_STATIC_LINKAGE
	/* on static platforms add a default -Xjcl option for the library which we're linked against */
	if(NULL == (jclArg = strBufferCat(j9portLibrary, NULL, "-Xjcl:")))
		goto cleanup;
	if(NULL == (jclArg = strBufferCat(j9portLibrary, jclArg, jclOption)))
		goto cleanup;
	if (!vmOptionsTableAddOption(&vmOptionsTable, strBufferData(jclArg), (void *) NULL))
		goto cleanup;
#endif
	if (!vmOptionsTableAddExeName(&vmOptionsTable, argv[0]))
		goto cleanup;
	if (!vmOptionsTableAddOption(&vmOptionsTable, "_port_library", (void *) j9portLibrary))
		goto cleanup;
	if (!vmOptionsTableAddOption(&vmOptionsTable, "exit", (void *) (&exitHook)))
		goto cleanup;

#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
	if (ive && addJxeOptions(j9portLibrary, ive, &vmOptionsTable))
		goto cleanup;
#endif

	if(NULL == (jclArg = strBufferCat(j9portLibrary, NULL, "-Xjcl:")))
		goto cleanup;
	if(NULL == (jclArg = strBufferCat(j9portLibrary, jclArg, J9_MIDP_DLL_NAME)))
		goto cleanup;
	if(NULL == (jclArg = strBufferCat(j9portLibrary, jclArg, ":loadlibrary=swt-motif-2104")))
		goto cleanup;


    /* Only allow the CDC library to be used with the j9midp executable */
	if (!vmOptionsTableAddOption(&vmOptionsTable, strBufferData(jclArg), (void*) NULL))
		goto cleanup;


	if (!vmOptionsTableParseArgs(j9portLibrary, &vmOptionsTable, classArg - 1, &(argv[1])))
		goto cleanup;

	/* Check that the minimum required -D options have been included.  If not, calculate and add the defaults */
	initDefaultDefines(j9portLibrary, &vmOptionsTable, argc, argv, jxeArg, isStandaloneJar?classArg:0, &classPath, &javaHome, &bootLibraryPath, &javaLibraryPath);

	vm_args.version = JNI_VERSION_1_2;
	vm_args.nOptions = vmOptionsTableGetCount(&vmOptionsTable);
	vm_args.options = vmOptionsTableGetOptions(&vmOptionsTable);
	vm_args.ignoreUnrecognized = JNI_FALSE;

	if (JNI_CreateJavaVM(&jvm, &env, &vm_args)) {
		rc = 2;
		goto cleanup;
	}

	rc = 0;
	if (isStandaloneJar) {
		jclass jarRunner;

		mainClass = "com/ibm/oti/vm/JarRunner";
		jarRunner = (*env)->FindClass(env, mainClass);
		if (jarRunner) {
			(*env)->DeleteLocalRef(env, jarRunner);
		} else {
			(*env)->ExceptionClear(env);
			(*jvm)->DestroyJavaVM(jvm);
			j9portLibrary->tty_printf(j9portLibrary, "-jar option is not available for this class library\n");
			rc = 3;
			goto cleanup;
		}
		classArg -= 1;	/* The "mainClass" was really the jar name, which is supposed to be the first java arg. */
	}
	javaRc = main_runJavaMain(env, mainClass, isNameUTF, (argc - (classArg + 1)), &argv[classArg + 1], j9portLibrary);

	(*jvm)->DestroyJavaVM(jvm);

  cleanup:
	switch (rc) {
	case 1:
		j9portLibrary->tty_printf(j9portLibrary, "VM startup error: Out of memory\n");
		break;
	case 2:
		j9portLibrary->tty_printf(j9portLibrary, "Internal VM error: Failed to create Java VM\n");
		break;
		/* case 4: help text was displayed */
		/* case 3,7,8,9: various jxe msgs already printed */
#if defined(JITNOCERT)
        case 11: /* case 11: -jit not certified on j9midp executable and only allow -jcl:midp */
            j9portLibrary->tty_printf(j9portLibrary, "VM startup error: JIT not available with j9midp\n");
            break;
#endif
    case 12: /* case 12: midp executable will only allow -jcl:midp option */
        j9portLibrary->tty_printf(j9portLibrary, "VM startup error: j9midp must use -jcl:midp\n");
        break;

	}
#ifdef J9VM_STATIC_LINKAGE
	if (jclArg)
		j9portLibrary->mem_free_memory(j9portLibrary, jclArg);
#endif
	if (classPath)
		j9portLibrary->mem_free_memory(j9portLibrary, classPath);
	if (javaHome)
		j9portLibrary->mem_free_memory(j9portLibrary, javaHome);
	if (bootLibraryPath)
		j9portLibrary->mem_free_memory(j9portLibrary, bootLibraryPath);
	if (javaLibraryPath)
		j9portLibrary->mem_free_memory(j9portLibrary, javaLibraryPath);
	if (vmOptionsTable) {
		vmOptionsTableDestroy(&vmOptionsTable);
	}
#ifdef J9VM_OPT_JXE_LOAD_SUPPORT
	if (ive)
		shutdownIveRelocator(j9portLibrary, ive);
#endif
	initialPortLibrary = NULL;
#ifdef J9VM_OPT_MEMORY_CHECK_SUPPORT
	memoryCheck_shutdown(j9portLibrary);
#endif
	return (rc ? rc : javaRc);
}

void JNICALL NORETURN exitHook(jint exitCode) {
	/* Shut down our port library.  Calling System.exit() is hopeless in multi-vm anyway,
		so the dirty use of a global variable here may be overlooked. */
	if (initialPortLibrary)  {
#ifdef J9WINCE
		initialPortLibrary->exit_shutdown_and_exit(initialPortLibrary, (int)exitCode);
#else
		initialPortLibrary->port_shutdown_library(initialPortLibrary);
#endif
		initialPortLibrary = NULL;
	}

#ifndef J9WINCE
	exit((int)exitCode);
#endif
}
static void dumpVersionInfo(J9PortLibrary * portLib, int argc, char **argv, int *copyrightWritten)
{
	PORT_ACCESS_FROM_PORT(portLib);

	j9tty_err_printf(PORTLIB, "\njava version \"1.3.0\"\n");

	dumpCopyrights(portLib, argc, argv, copyrightWritten);
}

static void dumpCopyrights( J9PortLibrary *portLib, int argc, char **argv, int *copyrightWritten)
{
	if(!*copyrightWritten)
	{
		PORT_ACCESS_FROM_PORT( portLib );

		j9tty_printf( PORTLIB, "\nLicensed Materials - Property of IBM\n");
		j9tty_printf( PORTLIB, "\nJ9 - VM for the Java(TM) platform, Version " EsVersionString "\n");
		j9tty_printf( PORTLIB, J9_COPYRIGHT_STRING);
		j9tty_printf( PORTLIB, "\nTarget: %s\n", vmDetailString(portLib));
		j9tty_printf( PORTLIB, "\nIBM is a registered trademark of IBM Corp.");
		j9tty_printf( PORTLIB, "\nJava and all Java-based marks and logos are trademarks or registered");
		j9tty_printf( PORTLIB, "\ntrademarks of Sun Microsystems, Inc.\n\n");
		*copyrightWritten = TRUE;
	}
}

#if (!defined(J9OSE) && !defined(ITRON) && !defined(BREW))
int main(int argc, char ** argv, char ** envp)
{
	J9PortLibrary j9portLibrary;
	struct j9cmdlineOptions options;
	int rc;

	j9port_init_library(&j9portLibrary);

	options.argc = argc;
	options.argv = argv;
	options.envp = envp;
	options.portLibrary = &j9portLibrary;

	rc = j9portLibrary.gp_protect(&j9portLibrary, gpProtectedMain, &options);

	j9portLibrary.port_shutdown_library(&j9portLibrary);
	return rc;
}

#endif

#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static J9IVERelocatorStruct* initializeIveRelocator(J9PortLibrary* portLib) {
	char errBuf[512];
	char* err = errBuf;
	PORT_ACCESS_FROM_PORT(portLib);

	J9IVERelocatorStruct *relocator = j9mem_allocate_memory(sizeof(J9IVERelocatorStruct) + sizeof(J9JXEInfo));
	if (relocator == NULL) goto fail1;

	memset(relocator, 0, sizeof(J9IVERelocatorStruct)  + sizeof(J9JXEInfo));

	relocator->romImageInfo = (J9JXEInfo*)((UDATA)relocator + sizeof(J9IVERelocatorStruct));

	if (j9sl_open_shared_library(J9_IVE_RELOC_DLL_NAME, &relocator->dllHandle, errBuf, sizeof(errBuf))) goto fail2;

	if (j9sl_lookup_name(relocator->dllHandle, "iveLoadJarFromFile", (UDATA*)&relocator->iveLoadJarFromFile, 4)) goto fail3;
	if (j9sl_lookup_name(relocator->dllHandle, "iveRelocateMessage", (UDATA*)&relocator->iveRelocateMessage, 1)) goto fail3;
	if (j9sl_lookup_name(relocator->dllHandle, "iveFindFileInJar", (UDATA*)&relocator->iveFindFileInJar, 5)) goto fail3;
	if (j9sl_lookup_name(relocator->dllHandle, "iveGetJarInfoValues", (UDATA*)&relocator->iveGetJarInfoValues, 5)) goto fail3;
	if (j9sl_lookup_name(relocator->dllHandle, "iveFreeJarInfoValues", (UDATA*)&relocator->iveFreeJarInfoValues, 2)) goto fail3;

	return relocator;

fail3:	j9sl_close_shared_library(relocator->dllHandle);
			err = "Missing required export";
fail2:	j9mem_free_memory(relocator);
fail1:	j9tty_printf(PORTLIB, "\nError loading jxe relocator: " J9_IVE_RELOC_DLL_NAME " (%s)" , err);
			return NULL;
}
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */


#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static int initializeJxe(J9PortLibrary* portLib, J9IVERelocatorStruct* ive, char** mainClass ) {
	int i;
	PORT_ACCESS_FROM_PORT(portLib);

	if (!ive->iveFindFileInJar(ive->jarPtr, IVE_ROM_IMAGE_NAME, -1, (char **) &ive->romImage[0], NULL)) {
		j9tty_printf(PORTLIB, "Unable to load rom image from jxe.\n");
		return 3;
	}

	ive->jarInfo = ive->iveGetJarInfoValues(portLib, ive->jarPtr, &ive->jarInfoCount, &ive->jarInfoKeys, &ive->jarInfoVals);

#if 0
	j9tty_printf(PORTLIB, "\nJar Info Values\n");
	for (i = 0; i < ive->jarInfoCount; i++) {
		j9tty_printf(PORTLIB, "%s=%s\n", ive->jarInfoKeys[i], ive->jarInfoVals[i]);
	}
	j9tty_printf(PORTLIB, "\n");
#endif

	for (i = 0; i < ive->jarInfoCount; i++) {
		if (!strcmp("startupClass", ive->jarInfoKeys[i])) {
			*mainClass = ive->jarInfoVals[i];
		}
	}

	return 0;
}
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */


#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static void shutdownIveRelocator(J9PortLibrary* portLib, J9IVERelocatorStruct* relocator) {
	PORT_ACCESS_FROM_PORT(portLib);

	if (relocator->jarInfo) {
		relocator->iveFreeJarInfoValues(portLib, relocator->jarInfo);
	}

	if (relocator->jarAllocPtr) {
		j9mem_free_memory(relocator->jarAllocPtr);
	}

	j9sl_close_shared_library(relocator->dllHandle);

	j9mem_free_memory(relocator);

}
#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */


#if (defined(J9VM_OPT_JXE_LOAD_SUPPORT)) /* priv. proto (autogen) */

static int addJxeOptions(J9PortLibrary *portLib, J9IVERelocatorStruct* ive, void** vmOptionsTable) {
	int i;

	if (!vmOptionsTableAddOption(vmOptionsTable, "_image_rom", ive->romImage)) return 1;
	vmOptionsTableAddOption(vmOptionsTable, "_image_rom_info", &ive->romImageInfo);

	if (ive->jarInfo) {
		for (i = 0; i < ive->jarInfoCount; i++) {
			if (!strcmp("systemProperty", ive->jarInfoKeys[i])) {
				if (!vmOptionsTableAddOption(vmOptionsTable, ive->jarInfoVals[i], NULL))
					return -1;
			} else if (!strcmp("vmOption", ive->jarInfoKeys[i])) {
				if (!vmOptionsTableParseArgInternal(portLib, vmOptionsTable, ive->jarInfoVals[i]))
					return -1;
			}
		}
	}

	return 0;
}

#endif /* J9VM_OPT_JXE_LOAD_SUPPORT (autogen) */


static I_32 initDefaultDefines(  J9PortLibrary *portLib, void **vmOptionsTable, int argc, char **argv, int jxeArg, int jarArg, J9StringBuffer **classPathInd, J9StringBuffer **javaHomeInd, J9StringBuffer **bootLibraryPathInd, J9StringBuffer **javaLibraryPathInd)
{
	extern char* getDefineArgument(char*,char*);

	int optionCount, i;
	JavaVMOption *options;
	int hasJavaHome = 0;
	int hasBootLibraryPath = 0; 
	int hasJavaLibraryPath = 0;
	int hasClassPath = 0;
	J9StringBuffer *classPath = NULL;
	J9StringBuffer *javaHome = NULL;
	J9StringBuffer *bootLibraryPath = NULL;
	J9StringBuffer *javaLibraryPath = NULL;

	/* Cycle through the list of VM options and check that the minimum required defaults are there.
	 * Calculate and insert the missing ones
	 */

	optionCount = vmOptionsTableGetCount(vmOptionsTable);
	options = vmOptionsTableGetOptions(vmOptionsTable);

	for(i  = 0; i < optionCount; i++) {
		if(getDefineArgument(options[i].optionString, "java.home")) { hasJavaHome = 1; continue; }
		if(getDefineArgument(options[i].optionString, "com.ibm.oti.vm.bootstrap.library.path")) { hasBootLibraryPath = 1; continue; }
		if(getDefineArgument(options[i].optionString, "java.library.path")) { hasJavaLibraryPath = 1; continue; }
		if(getDefineArgument(options[i].optionString, "java.class.path")) { 
			/* Ignore classpath defines for -jar */
			if(!jarArg) {
				hasClassPath = 1; 
				continue; 
			}
		}
	}

	if (!hasJavaHome)  {
		if (0 != main_initializeJavaHome( portLib, &javaHome, argc, argv ))  {
			/* This might be a memory leak, but main() will fail anyway */
			return -1;
		}
		if(javaHome) {
			javaHome = strBufferPrepend(portLib, javaHome, "-Djava.home=");
			if(!javaHome) return -1;
			*javaHomeInd = javaHome;
			if(!vmOptionsTableAddOption(vmOptionsTable, strBufferData(javaHome), NULL)) return -1;
		}
	}

	if (!hasBootLibraryPath) {
		if (0 != main_initializeBootLibraryPath( portLib, &bootLibraryPath, argv[0] ))  {
			/* This might be a memory leak, but main() will fail anyway */
			return -1;
		}
		if(bootLibraryPath) {
			bootLibraryPath = strBufferPrepend(portLib, bootLibraryPath , "-Dcom.ibm.oti.vm.bootstrap.library.path=");
			if(!bootLibraryPath) return -1;
			*bootLibraryPathInd = bootLibraryPath;
			if(!vmOptionsTableAddOption(vmOptionsTable, strBufferData(bootLibraryPath), NULL)) return -1;
		}
	}

	if (!hasJavaLibraryPath) {
		if (0 != main_initializeJavaLibraryPath( portLib, &javaLibraryPath, argv[0] ))  {
			/* This might be a memory leak, but main() will fail anyway */
			return -1;
		}
		if(javaLibraryPath) {
			javaLibraryPath = strBufferPrepend(portLib, javaLibraryPath, "-Djava.library.path=");
			if(!javaLibraryPath) return -1;
			*javaLibraryPathInd = javaLibraryPath;
			if(!vmOptionsTableAddOption(vmOptionsTable, strBufferData(javaLibraryPath), NULL)) return -1;
		}
	}
	
	if(!hasClassPath) {
		/* no free classpath if there is a JXE or -jar */
		if(jarArg) {
			classPath = strBufferCat(portLib, classPath, argv[jarArg]);
			if (classPath == NULL) return -1;
		} else {
			if (!jxeArg) {
				if (0 != main_initializeClassPath( portLib, &classPath)) {
					/* This might be a memory leak, but main() will fail anyway */
					return -1;
				}
				if (classPath == NULL || classPath->data[0] == 0) {
					classPath = strBufferCat(portLib, classPath, ".");
					if (classPath == NULL) return -1;
				}
			}
		}

		classPath = strBufferPrepend(portLib, classPath, "-Djava.class.path=");
		if (classPath == NULL) return -1;
		*classPathInd = classPath;
		if(!vmOptionsTableAddOption(vmOptionsTable, strBufferData(classPath), NULL)) return -1;
	}

	return 0;
}
