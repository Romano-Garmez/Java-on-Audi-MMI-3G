/*
*	(c) Copyright IBM Corp. 1991, 2003 All Rights Reserved
*/
#ifndef j9static_h
#define j9static_h

typedef struct {
	char * name;
	void * funcOrTable;
} EsPrimitiveTableSlot;

typedef	EsPrimitiveTableSlot EsPrimitiveTable[];
#define	EsDefinePrimitiveTable(name)	EsPrimitiveTableSlot name [] = {
#define	EsSubTable(table)		EsPrimitiveTableEntry(0, (table))
#define	EsPrimitiveTableEntry(name, fn)	{ (char *) (name), (void *) (fn) },
#define	EsEndPrimitiveTable             { (char *) 0, (void *) 0} };

#endif     /* j9static_h */

