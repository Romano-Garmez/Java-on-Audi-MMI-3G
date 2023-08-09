/*
*	(c) Copyright IBM Corp. 1991, 2003 All Rights Reserved
*/
#include "j9comp.h"
#include "j9static.h"

#ifndef J9VM_SHARED_LIBRARIES_SUPPORTED

extern EsPrimitiveTable J9DYNPrimitiveTable;
extern EsPrimitiveTable J9BCVPrimitiveTable;
extern EsPrimitiveTable J9INTPrimitiveTable;
extern EsPrimitiveTable J9DBGPrimitiveTable;
extern EsPrimitiveTable J9IVEBCUPrimitiveTable;
extern EsPrimitiveTable J9IVEMSPPrimitiveTable;
extern EsPrimitiveTable J9IVEOSMPrimitiveTable;
extern EsPrimitiveTable J9IVERELPrimitiveTable;
extern EsPrimitiveTable J9JNICHKPrimitiveTable;
extern EsPrimitiveTable J9JPIPrimitiveTable;
extern EsPrimitiveTable J9JVMPITSTPrimitiveTable;
extern EsPrimitiveTable J9PRFPrimitiveTable;
extern EsPrimitiveTable J9PXYPrimitiveTable;
extern EsPrimitiveTable J9SLPROFPrimitiveTable;
extern EsPrimitiveTable J9FDMPrimitiveTable;
extern EsPrimitiveTable J9VRBPrimitiveTable;
extern EsPrimitiveTable J9HOOKPrimitiveTable;
extern EsPrimitiveTable J9ZLIBPrimitiveTable;
extern EsPrimitiveTable J9CDCPrimitiveTable;
extern EsPrimitiveTable J9CLDCPrimitiveTable;
extern EsPrimitiveTable J9CLNGPrimitiveTable;
extern EsPrimitiveTable J9COREPrimitiveTable;
extern EsPrimitiveTable J9FOUNPrimitiveTable;
extern EsPrimitiveTable J9GWPrimitiveTable;
extern EsPrimitiveTable J9RMPrimitiveTable;
extern EsPrimitiveTable J9GWPPrimitiveTable;
extern EsPrimitiveTable J9MAXPrimitiveTable;
extern EsPrimitiveTable J9MIDPPrimitiveTable;
extern EsPrimitiveTable J9MPNGPrimitiveTable;
extern EsPrimitiveTable J9RTPrimitiveTable;
extern EsPrimitiveTable J9XTRPrimitiveTable;
extern EsPrimitiveTable J9AOTRTPrimitiveTable;
extern EsPrimitiveTable J9JITPrimitiveTable;

EsDefinePrimitiveTable(J9LinkedNatives)
	EsPrimitiveTableEntry("j9dyn20", J9DYNPrimitiveTable)
	EsPrimitiveTableEntry("j9bcv20", J9BCVPrimitiveTable)
	/* EsPrimitiveTableEntry("j9int20", J9INTPrimitiveTable) */
	EsPrimitiveTableEntry("j9dbg20", J9DBGPrimitiveTable)
	EsPrimitiveTableEntry("ivebcu20", J9IVEBCUPrimitiveTable)
	EsPrimitiveTableEntry("ivemsp20", J9IVEMSPPrimitiveTable)
	EsPrimitiveTableEntry("iveosm20", J9IVEOSMPrimitiveTable)
	EsPrimitiveTableEntry("iverel20", J9IVERELPrimitiveTable)
	/* EsPrimitiveTableEntry("jnichk", J9JNICHKPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9jpi20", J9JPIPrimitiveTable) */
	/* EsPrimitiveTableEntry("jvmpitst", J9JVMPITSTPrimitiveTable) */
	EsPrimitiveTableEntry("j9prf20", J9PRFPrimitiveTable)
	/* EsPrimitiveTableEntry("j9pxy20", J9PXYPrimitiveTable) */
	/* EsPrimitiveTableEntry("slprof", J9SLPROFPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9fdm20", J9FDMPrimitiveTable) */
	EsPrimitiveTableEntry("j9vrb20", J9VRBPrimitiveTable)
	EsPrimitiveTableEntry("j9hook20", J9HOOKPrimitiveTable)
	EsPrimitiveTableEntry("j9zlib20", J9ZLIBPrimitiveTable)
	/* EsPrimitiveTableEntry("j9cdc20", J9CDCPrimitiveTable) */
	EsPrimitiveTableEntry("j9cldc20", J9CLDCPrimitiveTable)
	/* EsPrimitiveTableEntry("j9clng20", J9CLNGPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9core20", J9COREPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9foun20", J9FOUNPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9gw20", J9GWPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9rm20", J9RMPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9gwp20", J9GWPPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9max20", J9MAXPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9midp20", J9MIDPPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9mpng20", J9MPNGPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9rt20", J9RTPrimitiveTable) */
	/* EsPrimitiveTableEntry("j9xtr20", J9XTRPrimitiveTable) */
	EsPrimitiveTableEntry("j9aotrt20", J9AOTRTPrimitiveTable)
	EsPrimitiveTableEntry("j9jit20", J9JITPrimitiveTable)
EsEndPrimitiveTable

#endif
