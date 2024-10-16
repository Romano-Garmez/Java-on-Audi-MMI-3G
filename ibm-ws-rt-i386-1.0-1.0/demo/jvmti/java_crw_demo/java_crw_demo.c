/*
 * @(#)src/demo/jvmti/java_crw_demo/java_crw_demo.c, dsdev, dsdev, 20060202 1.3
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
 * @(#)java_crw_demo.c	1.20 04/07/27
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

/* Class reader writer (java_crw_demo) for instrumenting bytecodes */

/*
 * As long as the callbacks allow for it and the class number is unique,
 *     this code is completely re-entrant and any number of classfile
 *     injections can happen at the same time.
 *
 *     The current logic requires a unique number for this class instance
 *     or (jclass,jobject loader) pair, this is done via the ClassIndex
 *     in hprof, which is passed in as the 'unsigned cnum' to java_crw_demo().
 *     It's up to the user of this interface if it wants to use this
 *     feature.
 *
 * Example Usage: See file test_crw.c.
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Get Java and class file and bytecode information from javavm area. */

#include <jni.h>
#include <jvm.h>
#include "opcodes.h"

#define const static
#include "opcodes.length" /* Declared as const array, static is better */
#undef const

/* Include our own interface for cross check */

#include "java_crw_demo.h"

/* Macros over error functions to capture line numbers */

#define CRW_FATAL(ci, message) fatal_error(ci, message, __FILE__, __LINE__)

#if defined(DEBUG) || !defined(NDEBUG)

  #define CRW_ASSERT(ci, cond) \
        ((cond)?(void)0:assert_error(ci, #cond, __FILE__, __LINE__))

#else

  #define CRW_ASSERT(ci, cond)

#endif

#define CRW_ASSERT_MI(mi) CRW_ASSERT((mi)?(mi)->ci:NULL,(mi)!=NULL)

#define CRW_ASSERT_CI(ci) CRW_ASSERT(ci, ( (ci) != NULL && \
                         (ci)->input_position <= (ci)->input_len && \
                         (ci)->output_position <= (ci)->output_len) )

/* Typedefs for various integral numbers, just for code clarity */

typedef unsigned       ClassOpcode;             /* One opcode */
typedef unsigned char  ByteCode;                /* One byte from bytecodes */
typedef int            ByteOffset;              /* Byte offset */
typedef int            ClassConstant;           /* Constant pool kind */
typedef long           CrwPosition;             /* Position in class image */
typedef unsigned short CrwCpoolIndex;           /* Index into constant pool */

/* Misc support macros */

/* Given the position of an opcode, find the next 4byte boundary position */
#define NEXT_4BYTE_BOUNDARY(opcode_pos) (((opcode_pos)+4) & (~3))

#define LARGEST_INJECTION               (12*3) /* 3 injections at same site */
#define MAXIMUM_NEW_CPOOL_ENTRIES       64 /* don't add more than 32 entries */

/* Constant Pool Entry (internal table that mirrors pool in file image) */

typedef struct {
    const char *        ptr;            /* Pointer to any string */
    unsigned short      len;            /* Length of string */
    unsigned int        index1;         /* 1st 16 bit index or 32bit value. */
    unsigned int        index2;         /* 2nd 16 bit index or 32bit value. */
    ClassConstant       tag;            /* Tag or kind of entry. */
} CrwConstantPoolEntry;

struct MethodImage;

/* Class file image storage structure */

typedef struct CrwClassImage {

    /* Unique class number for this class */
    unsigned                    number;

    /* Name of class, given or gotten out of class image */
    const char *                name;

    /* Input and Output class images tracking */
    const unsigned char *       input;
    unsigned char *             output;
    CrwPosition                 input_len;
    CrwPosition                 output_len;
    CrwPosition                 input_position;
    CrwPosition                 output_position;

    /* Mirrored constant pool */
    CrwConstantPoolEntry *      cpool;
    CrwCpoolIndex               cpool_max_elements;             /* Max count */
    CrwCpoolIndex               cpool_count_plus_one;

    /* Input flags about class (e.g. is it a system class) */
    int                         system_class;

    /* Class access flags gotten from file. */
    unsigned                    access_flags;

    /* Names of classes and methods. */
    char* tclass_name;		/* Name of class that has tracker methods. */
    char* tclass_sig;		/* Signature of class */
    char* call_name;		/* Method name to call at offset 0 */
    char* call_sig;		/* Signature of this method */
    char* return_name;		/* Method name to call before any return */
    char* return_sig;		/* Signature of this method */
    char* obj_init_name;	/* Method name to call in Object <init> */
    char* obj_init_sig;		/* Signature of this method */
    char* newarray_name;	/* Method name to call after newarray opcodes */
    char* newarray_sig;		/* Signature of this method */

    /* Constant pool index values for new entries */
    CrwCpoolIndex               tracker_class_index;
    CrwCpoolIndex               object_init_tracker_index;
    CrwCpoolIndex               newarray_tracker_index;
    CrwCpoolIndex               call_tracker_index;
    CrwCpoolIndex               return_tracker_index;
    CrwCpoolIndex               class_number_index; /* Class number in pool */

    /* Count of injections made into this class */
    int                         injection_count;

    /* This class must be the java.lang.Object class */
    jboolean			is_object_class;

    /* This class must be the java.lang.Thread class */
    jboolean			is_thread_class;

    /* Callback functions */
    FatalErrorHandler           fatal_error_handler;
    MethodNumberRegister        mnum_callback;

    /* Table of method names and descr's */
    int                         method_count;
    const char **               method_name;
    const char **               method_descr;
    struct MethodImage *        current_mi;

} CrwClassImage;

/* Injection bytecodes (holds injected bytecodes for each code position) */

typedef struct {
    ByteCode *  code;
    ByteOffset  len;
} Injection;

/* Method transformation data (allocated/freed as each method is processed) */

typedef struct MethodImage {

    /* Back reference to Class image data. */
    CrwClassImage *     ci;

    /* Unique method number for this class. */
    unsigned            number;

    /* Method name and descr */
    const char *        name;
    const char *        descr;

    /* Map of input bytecode offsets to output bytecode offsets */
    ByteOffset *        map;

    /* Bytecode injections for each input bytecode offset */
    Injection *         injections;

    /* Widening setting for each input bytecode offset */
    signed char *       widening;

    /* Length of original input bytecodes. */
    ByteOffset          code_len;

    /* Location in input where bytecodes are located. */
    CrwPosition         start_of_input_bytecodes;

    /* Original max_stack and new max stack */
    unsigned            max_stack;
    unsigned            new_max_stack;

    jboolean            object_init_method;
    jboolean            skip_call_return_sites;

    /* Method access flags gotten from file. */
    unsigned            access_flags;

} MethodImage;

/* ----------------------------------------------------------------- */
/* General support functions (memory and error handling) */

static void
fatal_error(CrwClassImage *ci, const char *message, const char *file, int line)
{
    if ( ci != NULL && ci->fatal_error_handler != NULL ) {
        (*ci->fatal_error_handler)(message, file, line);
    } else {
        /* Normal operation should NEVER reach here */
        /* NO CRW FATAL ERROR HANDLER! */
        (void)fprintf(stderr, "CRW: %s [%s:%d]\n", message, file, line);
        abort();
    }
}

#if defined(DEBUG) || !defined(NDEBUG)
static void
assert_error(CrwClassImage *ci, const char *condition,
                 const char *file, int line)
{
    char buf[512];
    MethodImage *mi;
    ByteOffset byte_code_offset;

    mi = ci->current_mi;
    if ( mi != NULL ) {
        byte_code_offset = (ByteOffset)(mi->ci->input_position - mi->start_of_input_bytecodes);
    } else {
        byte_code_offset=-1;
    }

    (void)sprintf(buf,
                "CRW ASSERTION FAILURE: %s (%s:%s:%d)",
                condition,
                ci->name==0?"?":ci->name,
                mi->name==0?"?":mi->name,
                byte_code_offset);
    fatal_error(ci, buf, file, line);
}
#endif

static void *
allocate(CrwClassImage *ci, int nbytes)
{
    void * ptr;

    ptr = malloc(nbytes);
    return ptr;
}

static void *
allocate_clean(CrwClassImage *ci, int nbytes)
{
    void * ptr;

    ptr = allocate(ci, nbytes);
    (void)memset(ptr, 0, nbytes);
    return ptr;
}

static const char *
duplicate(CrwClassImage *ci, const char *str, int len)
{
    char *copy;

    copy = (char*)allocate(ci, len+1);
    (void)memcpy(copy, str, len);
    copy[len] = 0;
    return (const char *)copy;
}

static void
deallocate(CrwClassImage *ci, void *ptr)
{
    (void)free(ptr);
}

/* ----------------------------------------------------------------- */
/* Functions for reading/writing bytes to/from the class images */

static unsigned
readU1(CrwClassImage *ci)
{
    CRW_ASSERT_CI(ci);
    return ((unsigned)(ci->input[ci->input_position++])) & 0xFF;
}

static unsigned
readU2(CrwClassImage *ci)
{
    unsigned res;

    res = readU1(ci);
    return (res << 8) + readU1(ci);
}

static signed short
readS2(CrwClassImage *ci)
{
    unsigned res;

    res = readU1(ci);
    return ((res << 8) + readU1(ci)) & 0xFFFF;
}

static unsigned
readU4(CrwClassImage *ci)
{
    unsigned res;

    res = readU2(ci);
    return (res << 16) + readU2(ci);
}

static void
writeU1(CrwClassImage *ci, unsigned val)  /* Only writes out lower 8 bits */
{
    CRW_ASSERT_CI(ci);
    ci->output[ci->output_position++] = val & 0xFF;
}

static void
writeU2(CrwClassImage *ci, unsigned val)
{
    writeU1(ci, val >> 8);
    writeU1(ci, val);
}

static void
writeU4(CrwClassImage *ci, unsigned val)
{
    writeU2(ci, val >> 16);
    writeU2(ci, val);
}

static unsigned
copyU1(CrwClassImage *ci)
{
    unsigned value;

    value = readU1(ci);
    writeU1(ci, value);
    return value;
}

static unsigned
copyU2(CrwClassImage *ci)
{
    unsigned value;

    value = readU2(ci);
    writeU2(ci, value);
    return value;
}

static unsigned
copyU4(CrwClassImage *ci)
{
    unsigned value;

    value = readU4(ci);
    writeU4(ci, value);
    return value;
}

static void
copy(CrwClassImage *ci, unsigned count)
{
    CRW_ASSERT_CI(ci);
    (void)memcpy(ci->output+ci->output_position,
                 ci->input+ci->input_position, count);
    ci->output_position += count;
    ci->input_position += count;
    CRW_ASSERT_CI(ci);
}

static void
skip(CrwClassImage *ci, unsigned count)
{
    CRW_ASSERT_CI(ci);
    ci->input_position += count;
}

static void
read_bytes(CrwClassImage *ci, void *bytes, unsigned count)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, bytes!=NULL);
    (void)memcpy(bytes, ci->input+ci->input_position, count);
    ci->input_position += count;
}

static void
write_bytes(CrwClassImage *ci, void *bytes, unsigned count)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, bytes!=NULL);
    (void)memcpy(ci->output+ci->output_position, bytes, count);
    ci->output_position += count;
}

static void
random_writeU2(CrwClassImage *ci, CrwPosition pos, unsigned val)
{
    CrwPosition save_position;

    CRW_ASSERT_CI(ci);
    save_position = ci->output_position;
    ci->output_position = pos;
    writeU2(ci, val);
    ci->output_position = save_position;
}

static void
random_writeU4(CrwClassImage *ci, CrwPosition pos, unsigned val)
{
    CrwPosition save_position;

    CRW_ASSERT_CI(ci);
    save_position = ci->output_position;
    ci->output_position = pos;
    writeU4(ci, val);
    ci->output_position = save_position;
}

/* ----------------------------------------------------------------- */
/* Constant Pool handling functions. */

static void
fillin_cpool_entry(CrwClassImage *ci, CrwCpoolIndex i,
                   ClassConstant tag,
                   unsigned int index1, unsigned int index2,
                   const char *ptr, int len)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, i > 0 && i < ci->cpool_count_plus_one);
    ci->cpool[i].tag    = tag;
    ci->cpool[i].index1 = index1;
    ci->cpool[i].index2 = index2;
    ci->cpool[i].ptr    = ptr;
    ci->cpool[i].len    = (unsigned short)len;
}

static CrwCpoolIndex
add_new_cpool_entry(CrwClassImage *ci, ClassConstant tag,
                    unsigned int index1, unsigned int index2,
                    const char *str, int len)
{
    CrwCpoolIndex i;
    char *utf8 = NULL;

    CRW_ASSERT_CI(ci);
    i = ci->cpool_count_plus_one++;

    /* NOTE: This implementation does not automatically expand the
     *       constant pool table beyond the expected number needed
     *       to handle this particular CrwTrackerInterface injections.
     *       See MAXIMUM_NEW_CPOOL_ENTRIES
     */
    CRW_ASSERT(ci,  ci->cpool_count_plus_one < ci->cpool_max_elements );

    writeU1(ci, tag);
    switch (tag) {
        case JVM_CONSTANT_Class:
            writeU2(ci, index1);
            break;
        case JVM_CONSTANT_String:
            writeU2(ci, index1);
            break;
        case JVM_CONSTANT_Fieldref:
        case JVM_CONSTANT_Methodref:
        case JVM_CONSTANT_InterfaceMethodref:
        case JVM_CONSTANT_Integer:
        case JVM_CONSTANT_Float:
        case JVM_CONSTANT_NameAndType:
            writeU2(ci, index1);
            writeU2(ci, index2);
            break;
        case JVM_CONSTANT_Long:
        case JVM_CONSTANT_Double:
            writeU4(ci, index1);
            writeU4(ci, index2);
            ci->cpool_count_plus_one++;
            CRW_ASSERT(ci,  ci->cpool_count_plus_one < ci->cpool_max_elements );
            break;
        case JVM_CONSTANT_Utf8:
            CRW_ASSERT(ci, len==(len & 0xFFFF));
            writeU2(ci, len);
            write_bytes(ci, (void*)str, len);
            utf8 = (char*)duplicate(ci, str, len);
            break;
        default:
            CRW_FATAL(ci, "Unknown constant");
            break;
    }
    fillin_cpool_entry(ci, i, tag, index1, index2, (const char *)utf8, len);
    CRW_ASSERT(ci, i > 0 && i < ci->cpool_count_plus_one);
    return i;
}

static CrwCpoolIndex
add_new_class_cpool_entry(CrwClassImage *ci, const char *class_name)
{
    CrwCpoolIndex name_index;
    CrwCpoolIndex class_index;
    int           len;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, class_name!=NULL);

    len = (int)strlen(class_name);
    name_index = add_new_cpool_entry(ci, JVM_CONSTANT_Utf8, len, 0,
                        class_name, len);
    class_index = add_new_cpool_entry(ci, JVM_CONSTANT_Class, name_index, 0,
                        NULL, 0);
    return class_index;
}

static CrwCpoolIndex
add_new_method_cpool_entry(CrwClassImage *ci, CrwCpoolIndex class_index,
                     const char *name, const char *descr)
{
    CrwCpoolIndex name_index;
    CrwCpoolIndex descr_index;
    CrwCpoolIndex name_type_index;
    int len;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, name!=NULL);
    CRW_ASSERT(ci, descr!=NULL);
    len = (int)strlen(name);
    name_index =
        add_new_cpool_entry(ci, JVM_CONSTANT_Utf8, len, 0, name, len);
    len = (int)strlen(descr);
    descr_index =
        add_new_cpool_entry(ci, JVM_CONSTANT_Utf8, len, 0, descr, len);
    name_type_index =
        add_new_cpool_entry(ci, JVM_CONSTANT_NameAndType,
                                name_index, descr_index, NULL, 0);
    return add_new_cpool_entry(ci, JVM_CONSTANT_Methodref,
                                class_index, name_type_index, NULL, 0);
}

static CrwConstantPoolEntry
cpool_entry(CrwClassImage *ci, CrwCpoolIndex c_index)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, c_index > 0 && c_index < ci->cpool_count_plus_one);
    return ci->cpool[c_index];
}

static void
cpool_setup(CrwClassImage *ci)
{
    CrwCpoolIndex i;
    CrwPosition cpool_output_position;
    int count_plus_one;

    CRW_ASSERT_CI(ci);
    cpool_output_position = ci->output_position;
    count_plus_one = copyU2(ci);
    CRW_ASSERT(ci, count_plus_one>1);
    ci->cpool_max_elements = count_plus_one+MAXIMUM_NEW_CPOOL_ENTRIES;
    ci->cpool = (CrwConstantPoolEntry*)allocate_clean(ci,
		(int)((ci->cpool_max_elements)*sizeof(CrwConstantPoolEntry)));
    ci->cpool_count_plus_one = (CrwCpoolIndex)count_plus_one;

    /* Index zero not in class file */
    for (i = 1; i < count_plus_one; ++i) {
        CrwCpoolIndex   ipos;
        ClassConstant   tag;
        unsigned int    index1;
        unsigned int    index2;
        unsigned        len;
        char *          utf8;

        ipos    = i;
        index1  = 0;
        index2  = 0;
        len     = 0;
        utf8    = NULL;

        tag = copyU1(ci);
        switch (tag) {
            case JVM_CONSTANT_Class:
                index1 = copyU2(ci);
                break;
            case JVM_CONSTANT_String:
                index1 = copyU2(ci);
                break;
            case JVM_CONSTANT_Fieldref:
            case JVM_CONSTANT_Methodref:
            case JVM_CONSTANT_InterfaceMethodref:
            case JVM_CONSTANT_Integer:
            case JVM_CONSTANT_Float:
            case JVM_CONSTANT_NameAndType:
                index1 = copyU2(ci);
                index2 = copyU2(ci);
                break;
            case JVM_CONSTANT_Long:
            case JVM_CONSTANT_Double:
                index1 = copyU4(ci);
                index2 = copyU4(ci);
                ++i;  /* // these take two CP entries - duh! */
                break;
            case JVM_CONSTANT_Utf8:
                len     = copyU2(ci);
                index1  = (unsigned short)len;
                utf8    = (char*)allocate(ci, len+1);
                read_bytes(ci, (void*)utf8, len);
                utf8[len] = 0;
                write_bytes(ci, (void*)utf8, len);
                break;
            default:
                CRW_FATAL(ci, "Unknown constant");
                break;
        }
        fillin_cpool_entry(ci, ipos, tag, index1, index2, (const char *)utf8, len);
    }

    if (ci->call_name != NULL || ci->return_name != NULL) {
	if ( ci->number != (ci->number & 0x7FFF) ) {
	    ci->class_number_index =
		add_new_cpool_entry(ci, JVM_CONSTANT_Integer,
		    (ci->number>>16) & 0xFFFF, ci->number & 0xFFFF, NULL, 0);
	}
    }

    ci->tracker_class_index =
                add_new_class_cpool_entry(ci, ci->tclass_name);
    if (ci->obj_init_name != NULL) {
        ci->object_init_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->obj_init_name,
                    ci->obj_init_sig);
    }
    if (ci->newarray_name != NULL) {
        ci->newarray_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->newarray_name,
                    ci->newarray_sig);
    }
    if (ci->call_name != NULL) {
        ci->call_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->call_name,
                    ci->call_sig);
    }
    if (ci->return_name != NULL) {
        ci->return_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->return_name,
                    ci->return_sig);
    }

    random_writeU2(ci, cpool_output_position, ci->cpool_count_plus_one);
}

/* ----------------------------------------------------------------- */
/* Functions that create the bytecodes to inject */

static ByteOffset
push_pool_constant_bytecodes(ByteCode *bytecodes, CrwCpoolIndex index)
{
    ByteOffset nbytes = 0;

    if ( index == (index&0x7F) ) {
        bytecodes[nbytes++] = (ByteCode)opc_ldc;
    } else {
        bytecodes[nbytes++] = (ByteCode)opc_ldc_w;
        bytecodes[nbytes++] = (ByteCode)((index >> 8) & 0xFF);
    }
    bytecodes[nbytes++] = (ByteCode)(index & 0xFF);
    return nbytes;
}

static ByteOffset
push_short_constant_bytecodes(ByteCode *bytecodes, unsigned number)
{
    ByteOffset nbytes = 0;

    if ( number <= 5 ) {
        bytecodes[nbytes++] = (ByteCode)(opc_iconst_0+number);
    } else if ( number == (number&0x7F) ) {
        bytecodes[nbytes++] = (ByteCode)opc_bipush;
        bytecodes[nbytes++] = (ByteCode)(number & 0xFF);
    } else {
        bytecodes[nbytes++] = (ByteCode)opc_sipush;
        bytecodes[nbytes++] = (ByteCode)((number >> 8) & 0xFF);
        bytecodes[nbytes++] = (ByteCode)(number & 0xFF);
    }
    return nbytes;
}

static ByteOffset
injection_template(MethodImage *mi, ByteCode *bytecodes, ByteOffset max_nbytes,
                        CrwCpoolIndex method_index)
{
    CrwClassImage *     ci;
    ByteOffset nbytes = 0;
    unsigned max_stack;
    int add_dup;
    int add_aload;
    int push_cnum;
    int push_mnum;

    ci = mi->ci;

    CRW_ASSERT(ci, bytecodes!=NULL);

    if ( method_index == 0 )  {
        return 0;
    }

    if ( method_index == ci->newarray_tracker_index) {
        max_stack       = mi->max_stack + 1;
        add_dup         = JNI_TRUE;
        add_aload       = JNI_FALSE;
        push_cnum       = JNI_FALSE;
        push_mnum       = JNI_FALSE;
    } else if ( method_index == ci->object_init_tracker_index) {
        max_stack       = mi->max_stack + 1;
        add_dup         = JNI_FALSE;
        add_aload       = JNI_TRUE;
        push_cnum       = JNI_FALSE;
        push_mnum       = JNI_FALSE;
    } else {
        max_stack       = mi->max_stack + 2;
        add_dup         = JNI_FALSE;
        add_aload       = JNI_FALSE;
        push_cnum       = JNI_TRUE;
        push_mnum       = JNI_TRUE;
    }

    if ( add_dup ) {
        bytecodes[nbytes++] = (ByteCode)opc_dup;
    }
    if ( add_aload ) {
        bytecodes[nbytes++] = (ByteCode)opc_aload_0;
    }
    if ( push_cnum ) {
        if ( ci->number == (ci->number & 0x7FFF) ) {
            nbytes += push_short_constant_bytecodes(bytecodes+nbytes,
                                                ci->number);
        } else {
            CRW_ASSERT(ci, ci->class_number_index!=0);
            nbytes += push_pool_constant_bytecodes(bytecodes+nbytes,
                                                ci->class_number_index);
        }
    }
    if ( push_mnum ) {
        nbytes += push_short_constant_bytecodes(bytecodes+nbytes,
                                            mi->number);
    }
    bytecodes[nbytes++] = (ByteCode)opc_invokestatic;
    bytecodes[nbytes++] = (ByteCode)(method_index >> 8);
    bytecodes[nbytes++] = (ByteCode)method_index;
    bytecodes[nbytes]   = 0;
    CRW_ASSERT(ci, nbytes<max_nbytes);

    /* Make sure the new max_stack is appropriate */
    if ( max_stack > mi->new_max_stack ) {
        mi->new_max_stack = max_stack;
    }
    return nbytes;
}

/* Called to create injection code at entry to a method */
static ByteOffset
entry_injection_code(MethodImage *mi, ByteCode *bytecodes, ByteOffset len)
{
    CrwClassImage *     ci;
    ByteOffset nbytes = 0;

    CRW_ASSERT_MI(mi);

    ci = mi->ci;

    if ( mi->object_init_method ) {
        nbytes = injection_template(mi,
                            bytecodes, len, ci->object_init_tracker_index);
    }
    if ( !mi->skip_call_return_sites ) {
        nbytes += injection_template(mi,
                    bytecodes+nbytes, len-nbytes, ci->call_tracker_index);
    }
    return nbytes;
}

/* Called to create injection code before an opcode */
static ByteOffset
before_injection_code(MethodImage *mi, ClassOpcode opcode,
                      ByteCode *bytecodes, ByteOffset len)
{
    ByteOffset nbytes = 0;


    CRW_ASSERT_MI(mi);
    switch ( opcode ) {
        case opc_return:
        case opc_ireturn:
        case opc_lreturn:
        case opc_freturn:
        case opc_dreturn:
        case opc_areturn:
	    if ( !mi->skip_call_return_sites ) {
		nbytes = injection_template(mi,
			    bytecodes, len, mi->ci->return_tracker_index);
            }
	    break;
        default:
            break;
    }
    return nbytes;
}

/* Called to create injection code after an opcode */
static ByteOffset
after_injection_code(MethodImage *mi, ClassOpcode opcode,
                     ByteCode *bytecodes, ByteOffset len)
{
    CrwClassImage* ci;
    ByteOffset nbytes;

    ci = mi->ci;
    nbytes = 0;

    CRW_ASSERT_MI(mi);
    switch ( opcode ) {
        case opc_new:
            /* Can't inject here cannot pass around uninitialized object */
            break;
        case opc_newarray:
        case opc_anewarray:
        case opc_multianewarray:
            nbytes = injection_template(mi,
                                bytecodes, len, ci->newarray_tracker_index);
            break;
        default:
            break;
    }
    return nbytes;
}

/* Actually inject the bytecodes */
static void
inject_bytecodes(MethodImage *mi, ByteOffset at,
                 ByteCode *bytecodes, ByteOffset len)
{
    Injection injection;
    CrwClassImage *ci;

    ci = mi->ci;
    CRW_ASSERT_MI(mi);
    CRW_ASSERT(ci, at <= mi->code_len);

    injection = mi->injections[at];

    CRW_ASSERT(ci, len <= LARGEST_INJECTION/2);
    CRW_ASSERT(ci, injection.len+len <= LARGEST_INJECTION);

    /* Either start an injection area or concatenate to what is there */
    if ( injection.code == NULL ) {
        CRW_ASSERT(ci, injection.len==0);
        injection.code = (ByteCode *)allocate_clean(ci, LARGEST_INJECTION+1);
    }

    (void)memcpy(injection.code+injection.len, bytecodes, len);
    injection.len += len;
    injection.code[injection.len] = 0;
    mi->injections[at] = injection;
    ci->injection_count++;
}

/* ----------------------------------------------------------------- */
/* Method handling functions */

static MethodImage *
method_init(CrwClassImage *ci, unsigned mnum, ByteOffset code_len)
{
    MethodImage *       mi;
    ByteOffset          i;

    mi                  = (MethodImage*)allocate_clean(ci, (int)sizeof(MethodImage));
    mi->ci              = ci;
    mi->name            = ci->method_name[mnum];
    mi->descr           = ci->method_descr[mnum];
    mi->code_len        = code_len;
    mi->map             = (ByteOffset*)allocate_clean(ci,
				(int)((code_len+1)*sizeof(ByteOffset)));
    for(i=0; i<=code_len; i++) {
        mi->map[i] = i;
    }
    mi->widening        = (signed char*)allocate_clean(ci, code_len+1);
    mi->injections      = (Injection *)allocate_clean(ci,
				(int)((code_len+1)*sizeof(Injection)));
    mi->number          = mnum;
    ci->current_mi      = mi;
    return mi;
}

static void
method_term(MethodImage *mi)
{
    CrwClassImage *ci;

    ci = mi->ci;
    CRW_ASSERT_MI(mi);
    if ( mi->map != NULL ) {
        deallocate(ci, (void*)mi->map);
        mi->map = NULL;
    }
    if ( mi->widening != NULL ) {
        deallocate(ci, (void*)mi->widening);
        mi->widening = NULL;
    }
    if ( mi->injections != NULL ) {
        ByteOffset i;
        for(i=0; i<= mi->code_len; i++) {
            if ( mi->injections[i].code != NULL ) {
                deallocate(ci, (void*)mi->injections[i].code);
                mi->injections[i].code = NULL;
            }
        }
        deallocate(ci, (void*)mi->injections);
        mi->injections = NULL;
    }
    ci->current_mi = NULL;
    deallocate(ci, (void*)mi);
}

static ByteOffset
input_code_offset(MethodImage *mi)
{
    CRW_ASSERT_MI(mi);
    return (ByteOffset)(mi->ci->input_position - mi->start_of_input_bytecodes);
}

static void
rewind_to_beginning_of_input_bytecodes(MethodImage *mi)
{
    CRW_ASSERT_MI(mi);
    mi->ci->input_position = mi->start_of_input_bytecodes;
}

/* Starting at original byte position 'at', add 'offset' to it's new
 *   location. This may be a negative value.
 *   NOTE: That this map is not the new bytecode location of the opcode
 *         but the new bytecode location that should be used when
 *         a goto or jump instruction was targeting the old bytecode
 *         location.
 */
static void
adjust_map(MethodImage *mi, ByteOffset at, ByteOffset offset)
{
    ByteOffset i;

    CRW_ASSERT_MI(mi);
    for (i = at; i <= mi->code_len; ++i) {
        mi->map[i] += offset;
    }
}

static void
widen(MethodImage *mi, ByteOffset at, ByteOffset len)
{
    int delta;

    CRW_ASSERT(mi->ci, at <= mi->code_len);
    delta = len - mi->widening[at];
    /* Adjust everything from the current input location by delta */
    adjust_map(mi, input_code_offset(mi), delta);
    /* Mark at beginning of instruction */
    mi->widening[at] = (signed char)len;
}

static void
verify_opc_wide(CrwClassImage *ci, ClassOpcode wopcode)
{
    switch (wopcode) {
        case opc_aload: case opc_astore:
        case opc_fload: case opc_fstore:
        case opc_iload: case opc_istore:
        case opc_lload: case opc_lstore:
        case opc_dload: case opc_dstore:
        case opc_ret:   case opc_iinc:
            break;
        default:
            CRW_FATAL(ci, "Invalid opcode supplied to opc_wide");
            break;
    }
}

/* Walk one instruction and inject instrumentation */
static void
inject_for_opcode(MethodImage *mi)
{
    CrwClassImage *  ci;
    ClassOpcode      opcode;
    int              pos;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    pos = input_code_offset(mi);
    opcode = readU1(ci);

    if (opcode == opc_wide) {
        ClassOpcode     wopcode;

	wopcode = readU1(ci);
        /* lvIndex not used */
        (void)readU2(ci);
        verify_opc_wide(ci, wopcode);
        if ( wopcode==opc_iinc ) {
            (void)readU1(ci);
            (void)readU1(ci);
        }
    } else {

        ByteCode        bytecodes[LARGEST_INJECTION+1];
        int             header;
        int             instr_len;
        int             low;
        int             high;
        int             npairs;
        ByteOffset      len;

	/* Get bytecodes to inject before this opcode */
        len = before_injection_code(mi, opcode, bytecodes, (int)sizeof(bytecodes));
        if ( len > 0 ) {
            inject_bytecodes(mi, pos, bytecodes, len);
	    /* Adjust map after processing this opcode */
        }

	/* Process this opcode */
        switch (opcode) {
            case opc_tableswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                skip(ci, header - (pos+1));
                (void)readU4(ci);
                low = readU4(ci);
                high = readU4(ci);
                skip(ci, (high+1-low) * 4);
                break;
            case opc_lookupswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                skip(ci, header - (pos+1));
                (void)readU4(ci);
                npairs = readU4(ci);
                skip(ci, npairs * 8);
                break;
            default:
                instr_len = opcode_length[opcode];
                skip(ci, instr_len-1);
                break;
        }

	/* Get position after this opcode is processed */
	pos = input_code_offset(mi);

	/* Adjust for any before_injection_code() */
        if ( len > 0 ) {
            /* Adjust everything past this opcode.
	     *   Why past it? Because we want any jumps to this bytecode loc
	     *   to go to the injected code, not where the opcode
	     *   was moved too.
	     *   Consider a 'return' opcode that is jumped too.
	     *   NOTE: This may not be correct in all cases, but will
	     *         when we are only dealing with non-variable opcodes
	     *         like the return opcodes. Be careful if the
	     *         before_injection_code() changes to include other
	     *         opcodes that have variable length.
	     */
            adjust_map(mi, pos, len);
        }

	/* Get bytecodes to inject after this opcode */
        len = after_injection_code(mi, opcode, bytecodes, (int)sizeof(bytecodes));
        if ( len > 0 ) {
            inject_bytecodes(mi, pos, bytecodes, len);

	    /* Adjust for any after_injection_code() */
            adjust_map(mi, pos, len);
        }

    }
}

/* Map original bytecode location to it's new location. (See adjust_map()). */
static ByteOffset
method_code_map(MethodImage *mi, ByteOffset pos)
{
    CRW_ASSERT_MI(mi);
    CRW_ASSERT(mi->ci, pos <= mi->code_len);
    return mi->map[pos];
}

static int
adjust_instruction(MethodImage *mi)
{
    CrwClassImage *     ci;
    ClassOpcode         opcode;
    int                 pos;
    int                 new_pos;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    pos = input_code_offset(mi);
    new_pos = method_code_map(mi,pos);

    opcode = readU1(ci);

    if (opcode == opc_wide) {
        ClassOpcode wopcode;

        wopcode = readU1(ci);
        /* lvIndex not used */
        (void)readU2(ci);
        verify_opc_wide(ci, wopcode);
        if ( wopcode==opc_iinc ) {
            (void)readU1(ci);
            (void)readU1(ci);
        }
    } else {

        int widened;
        int header;
        int newHeader;
        int low;
        int high;
        int new_pad;
        int old_pad;
        int delta;
        int new_delta;
        int delta_pad;
        int npairs;
        int instr_len;

        switch (opcode) {

        case opc_tableswitch:
            widened     = mi->widening[pos];
            header      = NEXT_4BYTE_BOUNDARY(pos);
            newHeader   = NEXT_4BYTE_BOUNDARY(new_pos);

            skip(ci, header - (pos+1));

            delta       = readU4(ci);
            low         = readU4(ci);
            high        = readU4(ci);
            skip(ci, (high+1-low) * 4);
            new_pad     = newHeader - new_pos;
            old_pad     = header - pos;
            delta_pad   = new_pad - old_pad;
            if (widened != delta_pad) {
                widen(mi, pos, delta_pad);
                return 0;
            }
            break;

        case opc_lookupswitch:
            widened     = mi->widening[pos];
            header      = NEXT_4BYTE_BOUNDARY(pos);
            newHeader   = NEXT_4BYTE_BOUNDARY(new_pos);

            skip(ci, header - (pos+1));

            delta       = readU4(ci);
            npairs      = readU4(ci);
            skip(ci, npairs * 8);
            new_pad     = newHeader - new_pos;
            old_pad     = header - pos;
            delta_pad   = new_pad - old_pad;
            if (widened != delta_pad) {
                widen(mi, pos, delta_pad);
                return 0;
            }
            break;

        case opc_jsr: case opc_goto:
        case opc_ifeq: case opc_ifge: case opc_ifgt:
        case opc_ifle: case opc_iflt: case opc_ifne:
        case opc_if_icmpeq: case opc_if_icmpne: case opc_if_icmpge:
        case opc_if_icmpgt: case opc_if_icmple: case opc_if_icmplt:
        case opc_if_acmpeq: case opc_if_acmpne:
        case opc_ifnull: case opc_ifnonnull:
            widened     = mi->widening[pos];
            delta       = readS2(ci);
            if (widened == 0) {
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                if ((new_delta < -32768) || (new_delta > 32767)) {
                    switch (opcode) {
                        case opc_jsr: case opc_goto:
                            widen(mi, pos, 2);
                            break;
                        default:
                            widen(mi, pos, 5);
                            break;
                    }
                    return 0;
                }
            }
            break;

        case opc_jsr_w:
        case opc_goto_w:
            (void)readU4(ci);
            break;

        default:
            instr_len = opcode_length[opcode];
            skip(ci, instr_len-1);
            break;
        }
    }
    return 1;
}

static void
write_instruction(MethodImage *mi)
{
    CrwClassImage *     ci;
    ClassOpcode         opcode;
    ByteOffset          new_code_len;
    int                 pos;
    int                 new_pos;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    pos = input_code_offset(mi);
    new_pos = method_code_map(mi,pos);
    new_code_len = mi->injections[pos].len;
    if (new_code_len > 0) {
        write_bytes(ci, (void*)mi->injections[pos].code, new_code_len);
    }

    opcode = readU1(ci);
    if (opcode == opc_wide) {
        ClassOpcode     wopcode;

        writeU1(ci, opcode);

        wopcode = copyU1(ci);
        /* lvIndex not used */
        (void)copyU2(ci);
        verify_opc_wide(ci, wopcode);
        if ( wopcode==opc_iinc ) {
            (void)copyU1(ci);
            (void)copyU1(ci);
        }
    } else {

        ClassOpcode new_opcode;
        int             header;
        int             newHeader;
        int             low;
        int             high;
        int             i;
        int             npairs;
        int             widened;
        int             instr_len;
        int             delta;
        int             new_delta;

        switch (opcode) {

            case opc_tableswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                newHeader = NEXT_4BYTE_BOUNDARY(new_pos);

                skip(ci, header - (pos+1));

                delta = readU4(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                low = readU4(ci);
                high = readU4(ci);

                writeU1(ci, opcode);
                for (i = new_pos+1; i < newHeader; ++i) {
                    writeU1(ci, 0);
                }
                writeU4(ci, new_delta);
                writeU4(ci, low);
                writeU4(ci, high);

                for (i = low; i <= high; ++i) {
                    delta = readU4(ci);
                    new_delta = method_code_map(mi,pos+delta) - new_pos;
                    writeU4(ci, new_delta);
                }
                break;

            case opc_lookupswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                newHeader = NEXT_4BYTE_BOUNDARY(new_pos);

                skip(ci, header - (pos+1));

                delta = readU4(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                npairs = readU4(ci);
                writeU1(ci, opcode);
                for (i = new_pos+1; i < newHeader; ++i) {
                    writeU1(ci, 0);
                }
                writeU4(ci, new_delta);
                writeU4(ci, npairs);
                for (i = 0; i< npairs; ++i) {
                    unsigned match = readU4(ci);
                    delta = readU4(ci);
                    new_delta = method_code_map(mi,pos+delta) - new_pos;
                    writeU4(ci, match);
                    writeU4(ci, new_delta);
                }
                break;

            case opc_jsr: case opc_goto:
            case opc_ifeq: case opc_ifge: case opc_ifgt:
            case opc_ifle: case opc_iflt: case opc_ifne:
            case opc_if_icmpeq: case opc_if_icmpne: case opc_if_icmpge:
            case opc_if_icmpgt: case opc_if_icmple: case opc_if_icmplt:
            case opc_if_acmpeq: case opc_if_acmpne:
            case opc_ifnull: case opc_ifnonnull:
                widened = mi->widening[pos];
                delta = readS2(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                new_opcode = opcode;
                if (widened == 0) {
                    writeU1(ci, opcode);
                    writeU2(ci, new_delta);
                } else if (widened == 2) {
                    switch (opcode) {
                        case opc_jsr:
                            new_opcode = opc_jsr_w;
                            break;
                        case opc_goto:
                            new_opcode = opc_jsr_w;
                            break;
                        default:
                            CRW_FATAL(ci, "unexpected opcode");
                            break;
                    }
                    writeU1(ci, new_opcode);
                    writeU4(ci, new_delta);
                } else if (widened == 5) {
                    switch (opcode) {
                        case opc_ifeq:
                            new_opcode = opc_ifne;
                            break;
                        case opc_ifge:
                            new_opcode = opc_iflt;
                            break;
                        case opc_ifgt:
                            new_opcode = opc_ifle;
                            break;
                        case opc_ifle:
                            new_opcode = opc_ifgt;
                            break;
                        case opc_iflt:
                            new_opcode = opc_ifge;
                            break;
                        case opc_ifne:
                            new_opcode = opc_ifeq;
                            break;
                        case opc_if_icmpeq:
                            new_opcode = opc_if_icmpne;
                            break;
                        case opc_if_icmpne:
                            new_opcode = opc_if_icmpeq;
                            break;
                        case opc_if_icmpge:
                            new_opcode = opc_if_icmplt;
                            break;
                        case opc_if_icmpgt:
                            new_opcode = opc_if_icmple;
                            break;
                        case opc_if_icmple:
                            new_opcode = opc_if_icmpgt;
                            break;
                        case opc_if_icmplt:
                            new_opcode = opc_if_icmpge;
                            break;
                        case opc_if_acmpeq:
                            new_opcode = opc_if_acmpne;
                            break;
                        case opc_if_acmpne:
                            new_opcode = opc_if_acmpeq;
                            break;
                        case opc_ifnull:
                            new_opcode = opc_ifnonnull;
                            break;
                        case opc_ifnonnull:
                            new_opcode = opc_ifnull;
                            break;
                        default:
                            CRW_FATAL(ci, "Unexpected opcode");
                        break;
                    }
                    writeU1(ci, new_opcode);    /* write inverse branch */
                    writeU2(ci, 3 + 5);         /* beyond if and goto_w */
                    writeU1(ci, opc_goto_w);    /* add a goto_w */
                    writeU4(ci, new_delta); /* write new and wide delta */
                } else {
                    CRW_FATAL(ci, "Unexpected widening");
                }
                break;

            case opc_jsr_w:
            case opc_goto_w:
                delta = readU4(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                writeU1(ci, opcode);
                writeU4(ci, new_delta);
                break;

            default:
                instr_len = opcode_length[opcode];
                writeU1(ci, opcode);
                copy(ci, instr_len-1);
                break;
        }
    }
}

static void
method_inject_and_write_code(MethodImage *mi)
{
    ByteCode bytecodes[LARGEST_INJECTION+1];
    ByteOffset   len;

    CRW_ASSERT_MI(mi);

    /* Do injections */
    rewind_to_beginning_of_input_bytecodes(mi);
    len = entry_injection_code(mi, bytecodes, (int)sizeof(bytecodes));
    if ( len > 0 ) {
        int pos;

        pos = 0;
        inject_bytecodes(mi, pos, bytecodes, len);
	/* Adjust pos 0 to map to new pos 0, you never want to
	 *  jump into this entry code injection. So the new pos 0
	 *  will be past this entry_injection_code().
	 */
        adjust_map(mi, pos, len); /* Inject before behavior */
    }
    while (input_code_offset(mi) < mi->code_len) {
        inject_for_opcode(mi);
    }

    /* Adjust instructions */
    rewind_to_beginning_of_input_bytecodes(mi);
    while (input_code_offset(mi) < mi->code_len) {
        if (!adjust_instruction(mi)) {
            rewind_to_beginning_of_input_bytecodes(mi);
        }
    }

    /* Write new instructions */
    rewind_to_beginning_of_input_bytecodes(mi);
    while (input_code_offset(mi) < mi->code_len) {
        write_instruction(mi);
    }
}

static void
copy_attribute(CrwClassImage *ci)
{
    int len;

    (void)copyU2(ci);
    len = copyU4(ci);
    copy(ci, len);
}

static void
copy_attributes(CrwClassImage *ci)
{
    unsigned i;
    unsigned count;

    count = copyU2(ci);
    for (i = 0; i < count; ++i) {
        copy_attribute(ci);
    }
}

static void
copy_all_fields(CrwClassImage *ci)
{
    unsigned i;
    unsigned count;

    count = copyU2(ci);
    for (i = 0; i < count; ++i) {
        /* access, name, descriptor */
        copy(ci, 6);
        copy_attributes(ci);
    }
}

static void
write_line_table(MethodImage *mi)
{
    unsigned             i;
    unsigned             count;
    CrwClassImage *      ci;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    (void)copyU4(ci);
    count = copyU2(ci);
    for(i=0; i<count; i++) {
        ByteOffset start_pc;
        ByteOffset new_start_pc;

        start_pc = readU2(ci);

        if ( start_pc == 0 ) {
	    new_start_pc = 0; /* Don't skip entry injection code. */
	} else {
	    new_start_pc = method_code_map(mi, start_pc);
	}

        writeU2(ci, new_start_pc);
        (void)copyU2(ci);
    }
}

/* Used for LocalVariableTable and LocalVariableTypeTable attributes */
static void
write_var_table(MethodImage *mi)
{
    unsigned             i;
    unsigned             count;
    CrwClassImage *      ci;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    (void)copyU4(ci);
    count = copyU2(ci);
    for(i=0; i<count; i++) {
        ByteOffset start_pc;
        ByteOffset new_start_pc;
        ByteOffset length;
        ByteOffset new_length;
        ByteOffset end_pc;
        ByteOffset new_end_pc;

        start_pc        = readU2(ci);
        length          = readU2(ci);

        if ( start_pc == 0 ) {
	    new_start_pc = 0; /* Don't skip entry injection code. */
	} else {
	    new_start_pc = method_code_map(mi, start_pc);
	}
        end_pc          = start_pc + length;
        new_end_pc      = method_code_map(mi, end_pc);
        new_length      = new_end_pc - new_start_pc;

        writeU2(ci, new_start_pc);
        writeU2(ci, new_length);
        (void)copyU2(ci);
        (void)copyU2(ci);
        (void)copyU2(ci);
    }
}

static void
method_write_exception_table(MethodImage *mi)
{
    unsigned            i;
    unsigned            count;
    CrwClassImage *     ci;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    count = copyU2(ci);
    for(i=0; i<count; i++) {
        ByteOffset start_pc;
        ByteOffset new_start_pc;
        ByteOffset end_pc;
        ByteOffset new_end_pc;
        ByteOffset handler_pc;
        ByteOffset new_handler_pc;

        start_pc        = readU2(ci);
        end_pc          = readU2(ci);
        handler_pc      = readU2(ci);

	new_start_pc    = method_code_map(mi, start_pc);
        new_end_pc      = method_code_map(mi, end_pc);
        new_handler_pc  = method_code_map(mi, handler_pc);

        writeU2(ci, new_start_pc);
        writeU2(ci, new_end_pc);
        writeU2(ci, new_handler_pc);
        (void)copyU2(ci);
    }
}

static int
attribute_match(CrwClassImage *ci, CrwCpoolIndex name_index, const char *name)
{
    CrwConstantPoolEntry cs;
    int                  len;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, name!=NULL);
    len = (int)strlen(name);
    cs = cpool_entry(ci, name_index);
    if ( cs.len==len && strncmp(cs.ptr, name, len)==0) {
       return 1;
    }
    return 0;
}

static void
method_write_code_attribute(MethodImage *mi)
{
    CrwClassImage *     ci;
    CrwCpoolIndex       name_index;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    name_index = copyU2(ci);
    if ( attribute_match(ci, name_index, "LineNumberTable") ) {
        write_line_table(mi);
    } else if ( attribute_match(ci, name_index, "LocalVariableTable") ) {
        write_var_table(mi);
    } else if ( attribute_match(ci, name_index, "LocalVariableTypeTable") ) {
        write_var_table(mi); /* Exact same format as the LocalVariableTable */
    } else {
        unsigned len;
        len = copyU4(ci);
        copy(ci, len);
    }
}

static int
is_init_method(const char *name)
{
    if ( name!=NULL && strcmp(name,"<init>")==0 ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static int
is_clinit_method(const char *name)
{
    if ( name!=NULL && strcmp(name,"<clinit>")==0 ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static int
is_finalize_method(const char *name)
{
    if ( name!=NULL && strcmp(name,"finalize")==0 ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static int
skip_method(CrwClassImage *ci, const char *name,
		unsigned access_flags, ByteOffset code_len,
		int system_class, jboolean *pskip_call_return_sites)
{
    *pskip_call_return_sites = JNI_FALSE;
    if ( system_class ) {
	if ( code_len == 1 && is_init_method(name) ) {
	    return JNI_TRUE;
	} else if ( code_len == 1 && is_finalize_method(name) ) {
	    return JNI_TRUE;
	} else if ( is_clinit_method(name) ) {
	    return JNI_TRUE;
	} else if ( ci->is_thread_class && strcmp(name,"currentThread")==0 ) {
	    return JNI_TRUE;
	}
	/*
	if ( access_flags & JVM_ACC_PRIVATE ) {
	    *pskip_call_return_sites = JNI_TRUE;
	}
	*/
    }
    return JNI_FALSE;
}

/* Process all code attributes */
static void
method_write_bytecodes(CrwClassImage *ci, unsigned mnum, unsigned access_flags)
{
    CrwPosition         output_attr_len_position;
    CrwPosition         output_max_stack_position;
    CrwPosition         output_code_len_position;
    CrwPosition         start_of_output_bytecodes;
    unsigned            i;
    unsigned            attr_len;
    unsigned            max_stack;
    ByteOffset          code_len;
    ByteOffset          new_code_len;
    unsigned            attr_count;
    unsigned            new_attr_len;
    MethodImage *       mi;
    jboolean            object_init_method;
    jboolean            skip_call_return_sites;

    CRW_ASSERT_CI(ci);

    /* Attribute Length */
    output_attr_len_position = ci->output_position;
    attr_len = copyU4(ci);

    /* Max Stack */
    output_max_stack_position = ci->output_position;
    max_stack = copyU2(ci);

    /* Max Locals */
    (void)copyU2(ci);

    /* Code Length */
    output_code_len_position = ci->output_position;
    code_len = copyU4(ci);
    start_of_output_bytecodes = ci->output_position;

    /* Some methods should not be instrumented */
    object_init_method = JNI_FALSE;
    skip_call_return_sites = JNI_FALSE;
    if ( ci->is_object_class &&
         is_init_method(ci->method_name[mnum]) &&
         strcmp(ci->method_descr[mnum],"()V")==0 ) {
        object_init_method = JNI_TRUE;
        skip_call_return_sites = JNI_TRUE;
    } else if ( skip_method(ci, ci->method_name[mnum], access_flags,
		code_len, ci->system_class, &skip_call_return_sites) ) {
        /* Copy remainder minus already copied, the U2 max_stack,
         *   U2 max_locals, and U4 code_length fields have already
         *   been processed.
         */
        copy(ci, attr_len - (2+2+4));
        return;
    }

    /* Start Injection */
    mi = method_init(ci, mnum, code_len);
    mi->object_init_method = object_init_method;
    mi->access_flags = access_flags;
    mi->skip_call_return_sites = skip_call_return_sites;

    /* Save the current position as the start of the input bytecodes */
    mi->start_of_input_bytecodes = ci->input_position;

    /* The max stack may increase */
    mi->max_stack = max_stack;
    mi->new_max_stack = max_stack;

    /* Adjust all code offsets */
    method_inject_and_write_code(mi);

    /* Fix up code length */
    new_code_len = (int)(ci->output_position - start_of_output_bytecodes);
    random_writeU4(ci, output_code_len_position, new_code_len);

    /* Fixup max stack */
    CRW_ASSERT(ci, mi->new_max_stack <= 0xFFFF);
    random_writeU2(ci, output_max_stack_position, mi->new_max_stack);

    /* Copy exception table */
    method_write_exception_table(mi);

    /* Copy code attributes */
    attr_count = copyU2(ci);
    for (i = 0; i < attr_count; ++i) {
        method_write_code_attribute(mi);
    }

    /* Fix up attribute length */
    new_attr_len = (int)(ci->output_position - (output_attr_len_position + 4));
    random_writeU4(ci, output_attr_len_position, new_attr_len);

    /* Free method data */
    method_term(mi);
    mi = NULL;

}

static void
method_write(CrwClassImage *ci, unsigned mnum)
{
    unsigned            i;
    unsigned            access_flags;
    CrwCpoolIndex       name_index;
    CrwCpoolIndex       descr_index;
    unsigned            attr_count;

    access_flags = copyU2(ci);
    name_index = copyU2(ci);
    ci->method_name[mnum] = cpool_entry(ci, name_index).ptr;
    descr_index = copyU2(ci);
    ci->method_descr[mnum] = cpool_entry(ci, descr_index).ptr;
    attr_count = copyU2(ci);

    for (i = 0; i < attr_count; ++i) {
        CrwCpoolIndex name_index;

        name_index = copyU2(ci);
        if ( attribute_match(ci, name_index, "Code") ) {
            method_write_bytecodes(ci, mnum, access_flags);
        } else {
            unsigned len;
            len = copyU4(ci);
            copy(ci, len);
        }
    }
}

static void
method_write_all(CrwClassImage *ci)
{
    unsigned i;
    unsigned count;

    count = copyU2(ci);
    ci->method_count = count;
    if ( count > 0 ) {
        ci->method_name = (const char **)allocate_clean(ci, count*(int)sizeof(const char*));
        ci->method_descr = (const char **)allocate_clean(ci, count*(int)sizeof(const char*));
    }

    for (i = 0; i < count; ++i) {
        method_write(ci, i);
    }

    if ( ci->mnum_callback != NULL ) {
        (*(ci->mnum_callback))(ci->number, ci->method_name, ci->method_descr,
			 count);
    }
}

/* ------------------------------------------------------------------- */
/* Cleanup function. */

static void
cleanup(CrwClassImage *ci)
{
    CRW_ASSERT_CI(ci);
    if ( ci->name != NULL ) {
        deallocate(ci, (void*)ci->name);
        ci->name = NULL;
    }
    if ( ci->method_name != NULL ) {
        deallocate(ci, (void*)ci->method_name);
        ci->method_name = NULL;
    }
    if ( ci->method_descr != NULL ) {
        deallocate(ci, (void*)ci->method_descr);
        ci->method_descr = NULL;
    }
    if ( ci->cpool != NULL ) {
        CrwCpoolIndex i;
        for(i=0; i<ci->cpool_count_plus_one; i++) {
            if ( ci->cpool[i].ptr != NULL ) {
                deallocate(ci, (void*)(ci->cpool[i].ptr));
                ci->cpool[i].ptr = NULL;
            }
        }
        deallocate(ci, (void*)ci->cpool);
        ci->cpool = NULL;
    }
}

static jboolean
skip_class(unsigned access_flags)
{
    if ( access_flags & JVM_ACC_INTERFACE ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static long
inject_class(struct CrwClassImage *ci,
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
		 unsigned char *buf,
                 long buf_len)
{
    CrwConstantPoolEntry        cs;
    CrwCpoolIndex               this_class;
    CrwCpoolIndex               super_class;
    unsigned                    magic;
    unsigned                    interface_count;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, buf!=NULL);
    CRW_ASSERT(ci, buf_len!=0);

    CRW_ASSERT(ci, strchr(tclass_name,'.')==NULL); /* internal qualified name */

    ci->injection_count 	= 0;
    ci->system_class            = system_class;
    ci->tclass_name		= tclass_name;
    ci->tclass_sig		= tclass_sig;
    ci->call_name		= call_name;
    ci->call_sig		= call_sig;
    ci->return_name		= return_name;
    ci->return_sig		= return_sig;
    ci->obj_init_name		= obj_init_name;
    ci->obj_init_sig		= obj_init_sig;
    ci->newarray_name		= newarray_name;
    ci->newarray_sig		= newarray_sig;
    ci->output                  = buf;
    ci->output_len              = buf_len;

    magic = copyU4(ci);
    CRW_ASSERT(ci, magic==0xCAFEBABE);
    if ( magic != 0xCAFEBABE ) {
	return (long)0;
    }

    /* minor version number not used */
    (void)copyU2(ci);
    /* major version number not used */
    (void)copyU2(ci);

    cpool_setup(ci);

    ci->access_flags        = copyU2(ci);
    if ( skip_class(ci->access_flags) ) {
        return (long)0;
    }

    this_class          = copyU2(ci);

    cs = cpool_entry(ci, (CrwCpoolIndex)(cpool_entry(ci, this_class).index1));
    if ( ci->name == NULL ) {
        ci->name = duplicate(ci, cs.ptr, cs.len);
        CRW_ASSERT(ci, strchr(ci->name,'.')==NULL); /* internal qualified name */
    }
    CRW_ASSERT(ci, (int)strlen(ci->name)==cs.len && strncmp(ci->name, cs.ptr, cs.len)==0);

    super_class         = copyU2(ci);
    if ( super_class == 0 ) {
        ci->is_object_class = JNI_TRUE;
        CRW_ASSERT(ci, strcmp(ci->name,"java/lang/Object")==0);
    }

    interface_count     = copyU2(ci);
    copy(ci, interface_count * 2);

    copy_all_fields(ci);

    method_write_all(ci);

    if ( ci->injection_count == 0 ) {
        return (long)0;
    }

    copy_attributes(ci);

    return (long)ci->output_position;
}

/* ------------------------------------------------------------------- */
/* Exported interfaces */

JNIEXPORT void JNICALL
java_crw_demo(unsigned class_number,
	 const char *name,
	 const unsigned char *file_image,
	 long file_len,
	 int system_class,
         char* tclass_name,	/* Name of class that has tracker methods. */
         char* tclass_sig,	/* Signature of tclass */
         char* call_name,	/* Method name to call at offset 0 */
         char* call_sig,	/* Signature of this method */
         char* return_name,	/* Method name to call before any return */
         char* return_sig,	/* Signature of this method */
         char* obj_init_name,	/* Method name to call in Object <init> */
         char* obj_init_sig,	/* Signature of this method */
         char* newarray_name,	/* Method name to call after newarray opcodes */
         char* newarray_sig,	/* Signature of this method */
	 unsigned char **pnew_file_image,
	 long *pnew_file_len,
	 FatalErrorHandler fatal_error_handler,
	 MethodNumberRegister mnum_callback)
{
    CrwClassImage ci;
    long          max_length;
    long          new_length;
    void         *new_image;
    int           len;

    /* Initial setup of the CrwClassImage structure */
    (void)memset(&ci, 0, (int)sizeof(CrwClassImage));
    ci.fatal_error_handler = fatal_error_handler;
    ci.mnum_callback       = mnum_callback;

    /* Do some interface error checks */
    if ( pnew_file_image==NULL ) {
        CRW_FATAL(&ci, "pnew_file_image==NULL");
    }
    if ( pnew_file_len==NULL ) {
        CRW_FATAL(&ci, "pnew_file_len==NULL");
    }

    /* No file length means do nothing */
    *pnew_file_image = NULL;
    *pnew_file_len = 0;
    if ( file_len==0 ) {
	return;
    }

    /* Do some more interface error checks */
    if ( file_image == NULL ) {
        CRW_FATAL(&ci, "file_image == NULL");
    }
    if ( file_len < 0 ) {
        CRW_FATAL(&ci, "file_len < 0");
    }
    if ( system_class != 0 && system_class != 1 ) {
        CRW_FATAL(&ci, "system_class is not 0 or 1");
    }
    if ( tclass_name == NULL ) {
        CRW_FATAL(&ci, "tclass_name == NULL");
    }
    if ( tclass_sig == NULL || tclass_sig[0]!='L' ) {
        CRW_FATAL(&ci, "tclass_sig is not a valid class signature");
    }
    len = (int)strlen(tclass_sig);
    if ( tclass_sig[len-1]!=';' ) {
        CRW_FATAL(&ci, "tclass_sig is not a valid class signature");
    }
    if ( call_name != NULL ) {
	if ( call_sig == NULL || strcmp(call_sig, "(II)V") != 0 ) {
            CRW_FATAL(&ci, "call_sig is not (II)V");
	}
    }
    if ( return_name != NULL ) {
	if ( return_sig == NULL || strcmp(return_sig, "(II)V") != 0 ) {
            CRW_FATAL(&ci, "return_sig is not (II)V");
	}
    }
    if ( obj_init_name != NULL ) {
	if ( obj_init_sig == NULL || strcmp(obj_init_sig, "(Ljava/lang/Object;)V") != 0 ) {
            CRW_FATAL(&ci, "obj_init_sig is not (Ljava/lang/Object;)V");
	}
    }
    if ( newarray_name != NULL ) {
	if ( newarray_sig == NULL || strcmp(newarray_sig, "(Ljava/lang/Object;)V") != 0 ) {
            CRW_FATAL(&ci, "newarray_sig is not (Ljava/lang/Object;)V");
	}
    }

    /* Finish setup the CrwClassImage structure */
    ci.is_thread_class = JNI_FALSE;
    if ( name != NULL ) {
        CRW_ASSERT(&ci, strchr(name,'.')==NULL); /* internal qualified name */

        ci.name = duplicate(&ci, name, (int)strlen(name));
        if ( strcmp(name, "java/lang/Thread")==0 ) {
	    ci.is_thread_class = JNI_TRUE;
        }
    }
    ci.number = class_number;
    ci.input = file_image;
    ci.input_len = file_len;

    /* Do the injection */
    max_length = file_len*2 + 512; /* Twice as big + 512 */
    new_image = malloc((int)max_length);
    new_length = inject_class(&ci,
				 system_class,
				 tclass_name,
				 tclass_sig,
				 call_name,
				 call_sig,
				 return_name,
				 return_sig,
				 obj_init_name,
				 obj_init_sig,
				 newarray_name,
				 newarray_sig,
				 new_image,
				 max_length);

    /* Dispose or shrink the space to be returned. */
    if ( new_length == 0 ) {
	(void)free(new_image);
	new_image = NULL;
    } else {
        new_image = (void*)realloc((void*)new_image, (int)new_length);
    }

    /* Return the new class image */
    *pnew_file_image = (unsigned char *)new_image;
    *pnew_file_len = (long)new_length;

    /* Cleanup before we leave. */
    cleanup(&ci);
}

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

/* Class reader writer (java_crw_demo) for instrumenting bytecodes */

/*
 * As long as the callbacks allow for it and the class number is unique,
 *     this code is completely re-entrant and any number of classfile
 *     injections can happen at the same time.
 *
 *     The current logic requires a unique number for this class instance
 *     or (jclass,jobject loader) pair, this is done via the ClassIndex
 *     in hprof, which is passed in as the 'unsigned cnum' to java_crw_demo().
 *     It's up to the user of this interface if it wants to use this
 *     feature.
 *
 * Example Usage: See file test_crw.c.
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Get Java and class file and bytecode information from javavm area. */

#include <jni.h>
#include <jvm.h>
#include "opcodes.h"

#define const static
#include "opcodes.length" /* Declared as const array, static is better */
#undef const

/* Include our own interface for cross check */

#include "java_crw_demo.h"

/* Macros over error functions to capture line numbers */

#define CRW_FATAL(ci, message) fatal_error(ci, message, __FILE__, __LINE__)

#if defined(DEBUG) || !defined(NDEBUG)

  #define CRW_ASSERT(ci, cond) \
        ((cond)?(void)0:assert_error(ci, #cond, __FILE__, __LINE__))

#else

  #define CRW_ASSERT(ci, cond)

#endif

#define CRW_ASSERT_MI(mi) CRW_ASSERT((mi)?(mi)->ci:NULL,(mi)!=NULL)

#define CRW_ASSERT_CI(ci) CRW_ASSERT(ci, ( (ci) != NULL && \
                         (ci)->input_position <= (ci)->input_len && \
                         (ci)->output_position <= (ci)->output_len) )

/* Typedefs for various integral numbers, just for code clarity */

typedef unsigned       ClassOpcode;             /* One opcode */
typedef unsigned char  ByteCode;                /* One byte from bytecodes */
typedef int            ByteOffset;              /* Byte offset */
typedef int            ClassConstant;           /* Constant pool kind */
typedef long           CrwPosition;             /* Position in class image */
typedef unsigned short CrwCpoolIndex;           /* Index into constant pool */

/* Misc support macros */

/* Given the position of an opcode, find the next 4byte boundary position */
#define NEXT_4BYTE_BOUNDARY(opcode_pos) (((opcode_pos)+4) & (~3))

#define LARGEST_INJECTION               (12*3) /* 3 injections at same site */
#define MAXIMUM_NEW_CPOOL_ENTRIES       64 /* don't add more than 32 entries */

/* Constant Pool Entry (internal table that mirrors pool in file image) */

typedef struct {
    const char *        ptr;            /* Pointer to any string */
    unsigned short      len;            /* Length of string */
    unsigned int        index1;         /* 1st 16 bit index or 32bit value. */
    unsigned int        index2;         /* 2nd 16 bit index or 32bit value. */
    ClassConstant       tag;            /* Tag or kind of entry. */
} CrwConstantPoolEntry;

struct MethodImage;

/* Class file image storage structure */

typedef struct CrwClassImage {

    /* Unique class number for this class */
    unsigned                    number;

    /* Name of class, given or gotten out of class image */
    const char *                name;

    /* Input and Output class images tracking */
    const unsigned char *       input;
    unsigned char *             output;
    CrwPosition                 input_len;
    CrwPosition                 output_len;
    CrwPosition                 input_position;
    CrwPosition                 output_position;

    /* Mirrored constant pool */
    CrwConstantPoolEntry *      cpool;
    CrwCpoolIndex               cpool_max_elements;             /* Max count */
    CrwCpoolIndex               cpool_count_plus_one;

    /* Input flags about class (e.g. is it a system class) */
    int                         system_class;

    /* Class access flags gotten from file. */
    unsigned                    access_flags;

    /* Names of classes and methods. */
    char* tclass_name;		/* Name of class that has tracker methods. */
    char* tclass_sig;		/* Signature of class */
    char* call_name;		/* Method name to call at offset 0 */
    char* call_sig;		/* Signature of this method */
    char* return_name;		/* Method name to call before any return */
    char* return_sig;		/* Signature of this method */
    char* obj_init_name;	/* Method name to call in Object <init> */
    char* obj_init_sig;		/* Signature of this method */
    char* newarray_name;	/* Method name to call after newarray opcodes */
    char* newarray_sig;		/* Signature of this method */

    /* Constant pool index values for new entries */
    CrwCpoolIndex               tracker_class_index;
    CrwCpoolIndex               object_init_tracker_index;
    CrwCpoolIndex               newarray_tracker_index;
    CrwCpoolIndex               call_tracker_index;
    CrwCpoolIndex               return_tracker_index;
    CrwCpoolIndex               class_number_index; /* Class number in pool */

    /* Count of injections made into this class */
    int                         injection_count;

    /* This class must be the java.lang.Object class */
    jboolean			is_object_class;

    /* This class must be the java.lang.Thread class */
    jboolean			is_thread_class;

    /* Callback functions */
    FatalErrorHandler           fatal_error_handler;
    MethodNumberRegister        mnum_callback;

    /* Table of method names and descr's */
    int                         method_count;
    const char **               method_name;
    const char **               method_descr;
    struct MethodImage *        current_mi;

} CrwClassImage;

/* Injection bytecodes (holds injected bytecodes for each code position) */

typedef struct {
    ByteCode *  code;
    ByteOffset  len;
} Injection;

/* Method transformation data (allocated/freed as each method is processed) */

typedef struct MethodImage {

    /* Back reference to Class image data. */
    CrwClassImage *     ci;

    /* Unique method number for this class. */
    unsigned            number;

    /* Method name and descr */
    const char *        name;
    const char *        descr;

    /* Map of input bytecode offsets to output bytecode offsets */
    ByteOffset *        map;

    /* Bytecode injections for each input bytecode offset */
    Injection *         injections;

    /* Widening setting for each input bytecode offset */
    signed char *       widening;

    /* Length of original input bytecodes. */
    ByteOffset          code_len;

    /* Location in input where bytecodes are located. */
    CrwPosition         start_of_input_bytecodes;

    /* Original max_stack and new max stack */
    unsigned            max_stack;
    unsigned            new_max_stack;

    jboolean            object_init_method;
    jboolean            skip_call_return_sites;

    /* Method access flags gotten from file. */
    unsigned            access_flags;

} MethodImage;

/* ----------------------------------------------------------------- */
/* General support functions (memory and error handling) */

static void
fatal_error(CrwClassImage *ci, const char *message, const char *file, int line)
{
    if ( ci != NULL && ci->fatal_error_handler != NULL ) {
        (*ci->fatal_error_handler)(message, file, line);
    } else {
        /* Normal operation should NEVER reach here */
        /* NO CRW FATAL ERROR HANDLER! */
        (void)fprintf(stderr, "CRW: %s [%s:%d]\n", message, file, line);
        abort();
    }
}

#if defined(DEBUG) || !defined(NDEBUG)
static void
assert_error(CrwClassImage *ci, const char *condition,
                 const char *file, int line)
{
    char buf[512];
    MethodImage *mi;
    ByteOffset byte_code_offset;

    mi = ci->current_mi;
    if ( mi != NULL ) {
        byte_code_offset = (ByteOffset)(mi->ci->input_position - mi->start_of_input_bytecodes);
    } else {
        byte_code_offset=-1;
    }

    (void)sprintf(buf,
                "CRW ASSERTION FAILURE: %s (%s:%s:%d)",
                condition,
                ci->name==0?"?":ci->name,
                mi->name==0?"?":mi->name,
                byte_code_offset);
    fatal_error(ci, buf, file, line);
}
#endif

static void *
allocate(CrwClassImage *ci, int nbytes)
{
    void * ptr;

    ptr = malloc(nbytes);
    return ptr;
}

static void *
allocate_clean(CrwClassImage *ci, int nbytes)
{
    void * ptr;

    ptr = allocate(ci, nbytes);
    (void)memset(ptr, 0, nbytes);
    return ptr;
}

static const char *
duplicate(CrwClassImage *ci, const char *str, int len)
{
    char *copy;

    copy = (char*)allocate(ci, len+1);
    (void)memcpy(copy, str, len);
    copy[len] = 0;
    return (const char *)copy;
}

static void
deallocate(CrwClassImage *ci, void *ptr)
{
    (void)free(ptr);
}

/* ----------------------------------------------------------------- */
/* Functions for reading/writing bytes to/from the class images */

static unsigned
readU1(CrwClassImage *ci)
{
    CRW_ASSERT_CI(ci);
    return ((unsigned)(ci->input[ci->input_position++])) & 0xFF;
}

static unsigned
readU2(CrwClassImage *ci)
{
    unsigned res;

    res = readU1(ci);
    return (res << 8) + readU1(ci);
}

static signed short
readS2(CrwClassImage *ci)
{
    unsigned res;

    res = readU1(ci);
    return ((res << 8) + readU1(ci)) & 0xFFFF;
}

static unsigned
readU4(CrwClassImage *ci)
{
    unsigned res;

    res = readU2(ci);
    return (res << 16) + readU2(ci);
}

static void
writeU1(CrwClassImage *ci, unsigned val)  /* Only writes out lower 8 bits */
{
    CRW_ASSERT_CI(ci);
    ci->output[ci->output_position++] = val & 0xFF;
}

static void
writeU2(CrwClassImage *ci, unsigned val)
{
    writeU1(ci, val >> 8);
    writeU1(ci, val);
}

static void
writeU4(CrwClassImage *ci, unsigned val)
{
    writeU2(ci, val >> 16);
    writeU2(ci, val);
}

static unsigned
copyU1(CrwClassImage *ci)
{
    unsigned value;

    value = readU1(ci);
    writeU1(ci, value);
    return value;
}

static unsigned
copyU2(CrwClassImage *ci)
{
    unsigned value;

    value = readU2(ci);
    writeU2(ci, value);
    return value;
}

static unsigned
copyU4(CrwClassImage *ci)
{
    unsigned value;

    value = readU4(ci);
    writeU4(ci, value);
    return value;
}

static void
copy(CrwClassImage *ci, unsigned count)
{
    CRW_ASSERT_CI(ci);
    (void)memcpy(ci->output+ci->output_position,
                 ci->input+ci->input_position, count);
    ci->output_position += count;
    ci->input_position += count;
    CRW_ASSERT_CI(ci);
}

static void
skip(CrwClassImage *ci, unsigned count)
{
    CRW_ASSERT_CI(ci);
    ci->input_position += count;
}

static void
read_bytes(CrwClassImage *ci, void *bytes, unsigned count)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, bytes!=NULL);
    (void)memcpy(bytes, ci->input+ci->input_position, count);
    ci->input_position += count;
}

static void
write_bytes(CrwClassImage *ci, void *bytes, unsigned count)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, bytes!=NULL);
    (void)memcpy(ci->output+ci->output_position, bytes, count);
    ci->output_position += count;
}

static void
random_writeU2(CrwClassImage *ci, CrwPosition pos, unsigned val)
{
    CrwPosition save_position;

    CRW_ASSERT_CI(ci);
    save_position = ci->output_position;
    ci->output_position = pos;
    writeU2(ci, val);
    ci->output_position = save_position;
}

static void
random_writeU4(CrwClassImage *ci, CrwPosition pos, unsigned val)
{
    CrwPosition save_position;

    CRW_ASSERT_CI(ci);
    save_position = ci->output_position;
    ci->output_position = pos;
    writeU4(ci, val);
    ci->output_position = save_position;
}

/* ----------------------------------------------------------------- */
/* Constant Pool handling functions. */

static void
fillin_cpool_entry(CrwClassImage *ci, CrwCpoolIndex i,
                   ClassConstant tag,
                   unsigned int index1, unsigned int index2,
                   const char *ptr, int len)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, i > 0 && i < ci->cpool_count_plus_one);
    ci->cpool[i].tag    = tag;
    ci->cpool[i].index1 = index1;
    ci->cpool[i].index2 = index2;
    ci->cpool[i].ptr    = ptr;
    ci->cpool[i].len    = (unsigned short)len;
}

static CrwCpoolIndex
add_new_cpool_entry(CrwClassImage *ci, ClassConstant tag,
                    unsigned int index1, unsigned int index2,
                    const char *str, int len)
{
    CrwCpoolIndex i;
    char *utf8 = NULL;

    CRW_ASSERT_CI(ci);
    i = ci->cpool_count_plus_one++;

    /* NOTE: This implementation does not automatically expand the
     *       constant pool table beyond the expected number needed
     *       to handle this particular CrwTrackerInterface injections.
     *       See MAXIMUM_NEW_CPOOL_ENTRIES
     */
    CRW_ASSERT(ci,  ci->cpool_count_plus_one < ci->cpool_max_elements );

    writeU1(ci, tag);
    switch (tag) {
        case JVM_CONSTANT_Class:
            writeU2(ci, index1);
            break;
        case JVM_CONSTANT_String:
            writeU2(ci, index1);
            break;
        case JVM_CONSTANT_Fieldref:
        case JVM_CONSTANT_Methodref:
        case JVM_CONSTANT_InterfaceMethodref:
        case JVM_CONSTANT_Integer:
        case JVM_CONSTANT_Float:
        case JVM_CONSTANT_NameAndType:
            writeU2(ci, index1);
            writeU2(ci, index2);
            break;
        case JVM_CONSTANT_Long:
        case JVM_CONSTANT_Double:
            writeU4(ci, index1);
            writeU4(ci, index2);
            ci->cpool_count_plus_one++;
            CRW_ASSERT(ci,  ci->cpool_count_plus_one < ci->cpool_max_elements );
            break;
        case JVM_CONSTANT_Utf8:
            CRW_ASSERT(ci, len==(len & 0xFFFF));
            writeU2(ci, len);
            write_bytes(ci, (void*)str, len);
            utf8 = (char*)duplicate(ci, str, len);
            break;
        default:
            CRW_FATAL(ci, "Unknown constant");
            break;
    }
    fillin_cpool_entry(ci, i, tag, index1, index2, (const char *)utf8, len);
    CRW_ASSERT(ci, i > 0 && i < ci->cpool_count_plus_one);
    return i;
}

static CrwCpoolIndex
add_new_class_cpool_entry(CrwClassImage *ci, const char *class_name)
{
    CrwCpoolIndex name_index;
    CrwCpoolIndex class_index;
    int           len;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, class_name!=NULL);

    len = (int)strlen(class_name);
    name_index = add_new_cpool_entry(ci, JVM_CONSTANT_Utf8, len, 0,
                        class_name, len);
    class_index = add_new_cpool_entry(ci, JVM_CONSTANT_Class, name_index, 0,
                        NULL, 0);
    return class_index;
}

static CrwCpoolIndex
add_new_method_cpool_entry(CrwClassImage *ci, CrwCpoolIndex class_index,
                     const char *name, const char *descr)
{
    CrwCpoolIndex name_index;
    CrwCpoolIndex descr_index;
    CrwCpoolIndex name_type_index;
    int len;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, name!=NULL);
    CRW_ASSERT(ci, descr!=NULL);
    len = (int)strlen(name);
    name_index =
        add_new_cpool_entry(ci, JVM_CONSTANT_Utf8, len, 0, name, len);
    len = (int)strlen(descr);
    descr_index =
        add_new_cpool_entry(ci, JVM_CONSTANT_Utf8, len, 0, descr, len);
    name_type_index =
        add_new_cpool_entry(ci, JVM_CONSTANT_NameAndType,
                                name_index, descr_index, NULL, 0);
    return add_new_cpool_entry(ci, JVM_CONSTANT_Methodref,
                                class_index, name_type_index, NULL, 0);
}

static CrwConstantPoolEntry
cpool_entry(CrwClassImage *ci, CrwCpoolIndex c_index)
{
    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, c_index > 0 && c_index < ci->cpool_count_plus_one);
    return ci->cpool[c_index];
}

static void
cpool_setup(CrwClassImage *ci)
{
    CrwCpoolIndex i;
    CrwPosition cpool_output_position;
    int count_plus_one;

    CRW_ASSERT_CI(ci);
    cpool_output_position = ci->output_position;
    count_plus_one = copyU2(ci);
    CRW_ASSERT(ci, count_plus_one>1);
    ci->cpool_max_elements = count_plus_one+MAXIMUM_NEW_CPOOL_ENTRIES;
    ci->cpool = (CrwConstantPoolEntry*)allocate_clean(ci,
		(int)((ci->cpool_max_elements)*sizeof(CrwConstantPoolEntry)));
    ci->cpool_count_plus_one = (CrwCpoolIndex)count_plus_one;

    /* Index zero not in class file */
    for (i = 1; i < count_plus_one; ++i) {
        CrwCpoolIndex   ipos;
        ClassConstant   tag;
        unsigned int    index1;
        unsigned int    index2;
        unsigned        len;
        char *          utf8;

        ipos    = i;
        index1  = 0;
        index2  = 0;
        len     = 0;
        utf8    = NULL;

        tag = copyU1(ci);
        switch (tag) {
            case JVM_CONSTANT_Class:
                index1 = copyU2(ci);
                break;
            case JVM_CONSTANT_String:
                index1 = copyU2(ci);
                break;
            case JVM_CONSTANT_Fieldref:
            case JVM_CONSTANT_Methodref:
            case JVM_CONSTANT_InterfaceMethodref:
            case JVM_CONSTANT_Integer:
            case JVM_CONSTANT_Float:
            case JVM_CONSTANT_NameAndType:
                index1 = copyU2(ci);
                index2 = copyU2(ci);
                break;
            case JVM_CONSTANT_Long:
            case JVM_CONSTANT_Double:
                index1 = copyU4(ci);
                index2 = copyU4(ci);
                ++i;  /* // these take two CP entries - duh! */
                break;
            case JVM_CONSTANT_Utf8:
                len     = copyU2(ci);
                index1  = (unsigned short)len;
                utf8    = (char*)allocate(ci, len+1);
                read_bytes(ci, (void*)utf8, len);
                utf8[len] = 0;
                write_bytes(ci, (void*)utf8, len);
                break;
            default:
                CRW_FATAL(ci, "Unknown constant");
                break;
        }
        fillin_cpool_entry(ci, ipos, tag, index1, index2, (const char *)utf8, len);
    }

    if (ci->call_name != NULL || ci->return_name != NULL) {
	if ( ci->number != (ci->number & 0x7FFF) ) {
	    ci->class_number_index =
		add_new_cpool_entry(ci, JVM_CONSTANT_Integer,
		    (ci->number>>16) & 0xFFFF, ci->number & 0xFFFF, NULL, 0);
	}
    }

    ci->tracker_class_index =
                add_new_class_cpool_entry(ci, ci->tclass_name);
    if (ci->obj_init_name != NULL) {
        ci->object_init_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->obj_init_name,
                    ci->obj_init_sig);
    }
    if (ci->newarray_name != NULL) {
        ci->newarray_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->newarray_name,
                    ci->newarray_sig);
    }
    if (ci->call_name != NULL) {
        ci->call_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->call_name,
                    ci->call_sig);
    }
    if (ci->return_name != NULL) {
        ci->return_tracker_index = add_new_method_cpool_entry(ci,
                    ci->tracker_class_index,
                    ci->return_name,
                    ci->return_sig);
    }

    random_writeU2(ci, cpool_output_position, ci->cpool_count_plus_one);
}

/* ----------------------------------------------------------------- */
/* Functions that create the bytecodes to inject */

static ByteOffset
push_pool_constant_bytecodes(ByteCode *bytecodes, CrwCpoolIndex index)
{
    ByteOffset nbytes = 0;

    if ( index == (index&0x7F) ) {
        bytecodes[nbytes++] = (ByteCode)opc_ldc;
    } else {
        bytecodes[nbytes++] = (ByteCode)opc_ldc_w;
        bytecodes[nbytes++] = (ByteCode)((index >> 8) & 0xFF);
    }
    bytecodes[nbytes++] = (ByteCode)(index & 0xFF);
    return nbytes;
}

static ByteOffset
push_short_constant_bytecodes(ByteCode *bytecodes, unsigned number)
{
    ByteOffset nbytes = 0;

    if ( number <= 5 ) {
        bytecodes[nbytes++] = (ByteCode)(opc_iconst_0+number);
    } else if ( number == (number&0x7F) ) {
        bytecodes[nbytes++] = (ByteCode)opc_bipush;
        bytecodes[nbytes++] = (ByteCode)(number & 0xFF);
    } else {
        bytecodes[nbytes++] = (ByteCode)opc_sipush;
        bytecodes[nbytes++] = (ByteCode)((number >> 8) & 0xFF);
        bytecodes[nbytes++] = (ByteCode)(number & 0xFF);
    }
    return nbytes;
}

static ByteOffset
injection_template(MethodImage *mi, ByteCode *bytecodes, ByteOffset max_nbytes,
                        CrwCpoolIndex method_index)
{
    CrwClassImage *     ci;
    ByteOffset nbytes = 0;
    unsigned max_stack;
    int add_dup;
    int add_aload;
    int push_cnum;
    int push_mnum;

    ci = mi->ci;

    CRW_ASSERT(ci, bytecodes!=NULL);

    if ( method_index == 0 )  {
        return 0;
    }

    if ( method_index == ci->newarray_tracker_index) {
        max_stack       = mi->max_stack + 1;
        add_dup         = JNI_TRUE;
        add_aload       = JNI_FALSE;
        push_cnum       = JNI_FALSE;
        push_mnum       = JNI_FALSE;
    } else if ( method_index == ci->object_init_tracker_index) {
        max_stack       = mi->max_stack + 1;
        add_dup         = JNI_FALSE;
        add_aload       = JNI_TRUE;
        push_cnum       = JNI_FALSE;
        push_mnum       = JNI_FALSE;
    } else {
        max_stack       = mi->max_stack + 2;
        add_dup         = JNI_FALSE;
        add_aload       = JNI_FALSE;
        push_cnum       = JNI_TRUE;
        push_mnum       = JNI_TRUE;
    }

    if ( add_dup ) {
        bytecodes[nbytes++] = (ByteCode)opc_dup;
    }
    if ( add_aload ) {
        bytecodes[nbytes++] = (ByteCode)opc_aload_0;
    }
    if ( push_cnum ) {
        if ( ci->number == (ci->number & 0x7FFF) ) {
            nbytes += push_short_constant_bytecodes(bytecodes+nbytes,
                                                ci->number);
        } else {
            CRW_ASSERT(ci, ci->class_number_index!=0);
            nbytes += push_pool_constant_bytecodes(bytecodes+nbytes,
                                                ci->class_number_index);
        }
    }
    if ( push_mnum ) {
        nbytes += push_short_constant_bytecodes(bytecodes+nbytes,
                                            mi->number);
    }
    bytecodes[nbytes++] = (ByteCode)opc_invokestatic;
    bytecodes[nbytes++] = (ByteCode)(method_index >> 8);
    bytecodes[nbytes++] = (ByteCode)method_index;
    bytecodes[nbytes]   = 0;
    CRW_ASSERT(ci, nbytes<max_nbytes);

    /* Make sure the new max_stack is appropriate */
    if ( max_stack > mi->new_max_stack ) {
        mi->new_max_stack = max_stack;
    }
    return nbytes;
}

/* Called to create injection code at entry to a method */
static ByteOffset
entry_injection_code(MethodImage *mi, ByteCode *bytecodes, ByteOffset len)
{
    CrwClassImage *     ci;
    ByteOffset nbytes = 0;

    CRW_ASSERT_MI(mi);

    ci = mi->ci;

    if ( mi->object_init_method ) {
        nbytes = injection_template(mi,
                            bytecodes, len, ci->object_init_tracker_index);
    }
    if ( !mi->skip_call_return_sites ) {
        nbytes += injection_template(mi,
                    bytecodes+nbytes, len-nbytes, ci->call_tracker_index);
    }
    return nbytes;
}

/* Called to create injection code before an opcode */
static ByteOffset
before_injection_code(MethodImage *mi, ClassOpcode opcode,
                      ByteCode *bytecodes, ByteOffset len)
{
    ByteOffset nbytes = 0;


    CRW_ASSERT_MI(mi);
    switch ( opcode ) {
        case opc_return:
        case opc_ireturn:
        case opc_lreturn:
        case opc_freturn:
        case opc_dreturn:
        case opc_areturn:
	    if ( !mi->skip_call_return_sites ) {
		nbytes = injection_template(mi,
			    bytecodes, len, mi->ci->return_tracker_index);
            }
	    break;
        default:
            break;
    }
    return nbytes;
}

/* Called to create injection code after an opcode */
static ByteOffset
after_injection_code(MethodImage *mi, ClassOpcode opcode,
                     ByteCode *bytecodes, ByteOffset len)
{
    CrwClassImage* ci;
    ByteOffset nbytes;

    ci = mi->ci;
    nbytes = 0;

    CRW_ASSERT_MI(mi);
    switch ( opcode ) {
        case opc_new:
            /* Can't inject here cannot pass around uninitialized object */
            break;
        case opc_newarray:
        case opc_anewarray:
        case opc_multianewarray:
            nbytes = injection_template(mi,
                                bytecodes, len, ci->newarray_tracker_index);
            break;
        default:
            break;
    }
    return nbytes;
}

/* Actually inject the bytecodes */
static void
inject_bytecodes(MethodImage *mi, ByteOffset at,
                 ByteCode *bytecodes, ByteOffset len)
{
    Injection injection;
    CrwClassImage *ci;

    ci = mi->ci;
    CRW_ASSERT_MI(mi);
    CRW_ASSERT(ci, at <= mi->code_len);

    injection = mi->injections[at];

    CRW_ASSERT(ci, len <= LARGEST_INJECTION/2);
    CRW_ASSERT(ci, injection.len+len <= LARGEST_INJECTION);

    /* Either start an injection area or concatenate to what is there */
    if ( injection.code == NULL ) {
        CRW_ASSERT(ci, injection.len==0);
        injection.code = (ByteCode *)allocate_clean(ci, LARGEST_INJECTION+1);
    }

    (void)memcpy(injection.code+injection.len, bytecodes, len);
    injection.len += len;
    injection.code[injection.len] = 0;
    mi->injections[at] = injection;
    ci->injection_count++;
}

/* ----------------------------------------------------------------- */
/* Method handling functions */

static MethodImage *
method_init(CrwClassImage *ci, unsigned mnum, ByteOffset code_len)
{
    MethodImage *       mi;
    ByteOffset          i;

    mi                  = (MethodImage*)allocate_clean(ci, (int)sizeof(MethodImage));
    mi->ci              = ci;
    mi->name            = ci->method_name[mnum];
    mi->descr           = ci->method_descr[mnum];
    mi->code_len        = code_len;
    mi->map             = (ByteOffset*)allocate_clean(ci,
				(int)((code_len+1)*sizeof(ByteOffset)));
    for(i=0; i<=code_len; i++) {
        mi->map[i] = i;
    }
    mi->widening        = (signed char*)allocate_clean(ci, code_len+1);
    mi->injections      = (Injection *)allocate_clean(ci,
				(int)((code_len+1)*sizeof(Injection)));
    mi->number          = mnum;
    ci->current_mi      = mi;
    return mi;
}

static void
method_term(MethodImage *mi)
{
    CrwClassImage *ci;

    ci = mi->ci;
    CRW_ASSERT_MI(mi);
    if ( mi->map != NULL ) {
        deallocate(ci, (void*)mi->map);
        mi->map = NULL;
    }
    if ( mi->widening != NULL ) {
        deallocate(ci, (void*)mi->widening);
        mi->widening = NULL;
    }
    if ( mi->injections != NULL ) {
        ByteOffset i;
        for(i=0; i<= mi->code_len; i++) {
            if ( mi->injections[i].code != NULL ) {
                deallocate(ci, (void*)mi->injections[i].code);
                mi->injections[i].code = NULL;
            }
        }
        deallocate(ci, (void*)mi->injections);
        mi->injections = NULL;
    }
    ci->current_mi = NULL;
    deallocate(ci, (void*)mi);
}

static ByteOffset
input_code_offset(MethodImage *mi)
{
    CRW_ASSERT_MI(mi);
    return (ByteOffset)(mi->ci->input_position - mi->start_of_input_bytecodes);
}

static void
rewind_to_beginning_of_input_bytecodes(MethodImage *mi)
{
    CRW_ASSERT_MI(mi);
    mi->ci->input_position = mi->start_of_input_bytecodes;
}

/* Starting at original byte position 'at', add 'offset' to it's new
 *   location. This may be a negative value.
 *   NOTE: That this map is not the new bytecode location of the opcode
 *         but the new bytecode location that should be used when
 *         a goto or jump instruction was targeting the old bytecode
 *         location.
 */
static void
adjust_map(MethodImage *mi, ByteOffset at, ByteOffset offset)
{
    ByteOffset i;

    CRW_ASSERT_MI(mi);
    for (i = at; i <= mi->code_len; ++i) {
        mi->map[i] += offset;
    }
}

static void
widen(MethodImage *mi, ByteOffset at, ByteOffset len)
{
    int delta;

    CRW_ASSERT(mi->ci, at <= mi->code_len);
    delta = len - mi->widening[at];
    /* Adjust everything from the current input location by delta */
    adjust_map(mi, input_code_offset(mi), delta);
    /* Mark at beginning of instruction */
    mi->widening[at] = (signed char)len;
}

static void
verify_opc_wide(CrwClassImage *ci, ClassOpcode wopcode)
{
    switch (wopcode) {
        case opc_aload: case opc_astore:
        case opc_fload: case opc_fstore:
        case opc_iload: case opc_istore:
        case opc_lload: case opc_lstore:
        case opc_dload: case opc_dstore:
        case opc_ret:   case opc_iinc:
            break;
        default:
            CRW_FATAL(ci, "Invalid opcode supplied to opc_wide");
            break;
    }
}

/* Walk one instruction and inject instrumentation */
static void
inject_for_opcode(MethodImage *mi)
{
    CrwClassImage *  ci;
    ClassOpcode      opcode;
    int              pos;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    pos = input_code_offset(mi);
    opcode = readU1(ci);

    if (opcode == opc_wide) {
        ClassOpcode     wopcode;

	wopcode = readU1(ci);
        /* lvIndex not used */
        (void)readU2(ci);
        verify_opc_wide(ci, wopcode);
        if ( wopcode==opc_iinc ) {
            (void)readU1(ci);
            (void)readU1(ci);
        }
    } else {

        ByteCode        bytecodes[LARGEST_INJECTION+1];
        int             header;
        int             instr_len;
        int             low;
        int             high;
        int             npairs;
        ByteOffset      len;

	/* Get bytecodes to inject before this opcode */
        len = before_injection_code(mi, opcode, bytecodes, (int)sizeof(bytecodes));
        if ( len > 0 ) {
            inject_bytecodes(mi, pos, bytecodes, len);
	    /* Adjust map after processing this opcode */
        }

	/* Process this opcode */
        switch (opcode) {
            case opc_tableswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                skip(ci, header - (pos+1));
                (void)readU4(ci);
                low = readU4(ci);
                high = readU4(ci);
                skip(ci, (high+1-low) * 4);
                break;
            case opc_lookupswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                skip(ci, header - (pos+1));
                (void)readU4(ci);
                npairs = readU4(ci);
                skip(ci, npairs * 8);
                break;
            default:
                instr_len = opcode_length[opcode];
                skip(ci, instr_len-1);
                break;
        }

	/* Get position after this opcode is processed */
	pos = input_code_offset(mi);

	/* Adjust for any before_injection_code() */
        if ( len > 0 ) {
            /* Adjust everything past this opcode.
	     *   Why past it? Because we want any jumps to this bytecode loc
	     *   to go to the injected code, not where the opcode
	     *   was moved too.
	     *   Consider a 'return' opcode that is jumped too.
	     *   NOTE: This may not be correct in all cases, but will
	     *         when we are only dealing with non-variable opcodes
	     *         like the return opcodes. Be careful if the
	     *         before_injection_code() changes to include other
	     *         opcodes that have variable length.
	     */
            adjust_map(mi, pos, len);
        }

	/* Get bytecodes to inject after this opcode */
        len = after_injection_code(mi, opcode, bytecodes, (int)sizeof(bytecodes));
        if ( len > 0 ) {
            inject_bytecodes(mi, pos, bytecodes, len);

	    /* Adjust for any after_injection_code() */
            adjust_map(mi, pos, len);
        }

    }
}

/* Map original bytecode location to it's new location. (See adjust_map()). */
static ByteOffset
method_code_map(MethodImage *mi, ByteOffset pos)
{
    CRW_ASSERT_MI(mi);
    CRW_ASSERT(mi->ci, pos <= mi->code_len);
    return mi->map[pos];
}

static int
adjust_instruction(MethodImage *mi)
{
    CrwClassImage *     ci;
    ClassOpcode         opcode;
    int                 pos;
    int                 new_pos;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    pos = input_code_offset(mi);
    new_pos = method_code_map(mi,pos);

    opcode = readU1(ci);

    if (opcode == opc_wide) {
        ClassOpcode wopcode;

        wopcode = readU1(ci);
        /* lvIndex not used */
        (void)readU2(ci);
        verify_opc_wide(ci, wopcode);
        if ( wopcode==opc_iinc ) {
            (void)readU1(ci);
            (void)readU1(ci);
        }
    } else {

        int widened;
        int header;
        int newHeader;
        int low;
        int high;
        int new_pad;
        int old_pad;
        int delta;
        int new_delta;
        int delta_pad;
        int npairs;
        int instr_len;

        switch (opcode) {

        case opc_tableswitch:
            widened     = mi->widening[pos];
            header      = NEXT_4BYTE_BOUNDARY(pos);
            newHeader   = NEXT_4BYTE_BOUNDARY(new_pos);

            skip(ci, header - (pos+1));

            delta       = readU4(ci);
            low         = readU4(ci);
            high        = readU4(ci);
            skip(ci, (high+1-low) * 4);
            new_pad     = newHeader - new_pos;
            old_pad     = header - pos;
            delta_pad   = new_pad - old_pad;
            if (widened != delta_pad) {
                widen(mi, pos, delta_pad);
                return 0;
            }
            break;

        case opc_lookupswitch:
            widened     = mi->widening[pos];
            header      = NEXT_4BYTE_BOUNDARY(pos);
            newHeader   = NEXT_4BYTE_BOUNDARY(new_pos);

            skip(ci, header - (pos+1));

            delta       = readU4(ci);
            npairs      = readU4(ci);
            skip(ci, npairs * 8);
            new_pad     = newHeader - new_pos;
            old_pad     = header - pos;
            delta_pad   = new_pad - old_pad;
            if (widened != delta_pad) {
                widen(mi, pos, delta_pad);
                return 0;
            }
            break;

        case opc_jsr: case opc_goto:
        case opc_ifeq: case opc_ifge: case opc_ifgt:
        case opc_ifle: case opc_iflt: case opc_ifne:
        case opc_if_icmpeq: case opc_if_icmpne: case opc_if_icmpge:
        case opc_if_icmpgt: case opc_if_icmple: case opc_if_icmplt:
        case opc_if_acmpeq: case opc_if_acmpne:
        case opc_ifnull: case opc_ifnonnull:
            widened     = mi->widening[pos];
            delta       = readS2(ci);
            if (widened == 0) {
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                if ((new_delta < -32768) || (new_delta > 32767)) {
                    switch (opcode) {
                        case opc_jsr: case opc_goto:
                            widen(mi, pos, 2);
                            break;
                        default:
                            widen(mi, pos, 5);
                            break;
                    }
                    return 0;
                }
            }
            break;

        case opc_jsr_w:
        case opc_goto_w:
            (void)readU4(ci);
            break;

        default:
            instr_len = opcode_length[opcode];
            skip(ci, instr_len-1);
            break;
        }
    }
    return 1;
}

static void
write_instruction(MethodImage *mi)
{
    CrwClassImage *     ci;
    ClassOpcode         opcode;
    ByteOffset          new_code_len;
    int                 pos;
    int                 new_pos;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    pos = input_code_offset(mi);
    new_pos = method_code_map(mi,pos);
    new_code_len = mi->injections[pos].len;
    if (new_code_len > 0) {
        write_bytes(ci, (void*)mi->injections[pos].code, new_code_len);
    }

    opcode = readU1(ci);
    if (opcode == opc_wide) {
        ClassOpcode     wopcode;

        writeU1(ci, opcode);

        wopcode = copyU1(ci);
        /* lvIndex not used */
        (void)copyU2(ci);
        verify_opc_wide(ci, wopcode);
        if ( wopcode==opc_iinc ) {
            (void)copyU1(ci);
            (void)copyU1(ci);
        }
    } else {

        ClassOpcode new_opcode;
        int             header;
        int             newHeader;
        int             low;
        int             high;
        int             i;
        int             npairs;
        int             widened;
        int             instr_len;
        int             delta;
        int             new_delta;

        switch (opcode) {

            case opc_tableswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                newHeader = NEXT_4BYTE_BOUNDARY(new_pos);

                skip(ci, header - (pos+1));

                delta = readU4(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                low = readU4(ci);
                high = readU4(ci);

                writeU1(ci, opcode);
                for (i = new_pos+1; i < newHeader; ++i) {
                    writeU1(ci, 0);
                }
                writeU4(ci, new_delta);
                writeU4(ci, low);
                writeU4(ci, high);

                for (i = low; i <= high; ++i) {
                    delta = readU4(ci);
                    new_delta = method_code_map(mi,pos+delta) - new_pos;
                    writeU4(ci, new_delta);
                }
                break;

            case opc_lookupswitch:
                header = NEXT_4BYTE_BOUNDARY(pos);
                newHeader = NEXT_4BYTE_BOUNDARY(new_pos);

                skip(ci, header - (pos+1));

                delta = readU4(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                npairs = readU4(ci);
                writeU1(ci, opcode);
                for (i = new_pos+1; i < newHeader; ++i) {
                    writeU1(ci, 0);
                }
                writeU4(ci, new_delta);
                writeU4(ci, npairs);
                for (i = 0; i< npairs; ++i) {
                    unsigned match = readU4(ci);
                    delta = readU4(ci);
                    new_delta = method_code_map(mi,pos+delta) - new_pos;
                    writeU4(ci, match);
                    writeU4(ci, new_delta);
                }
                break;

            case opc_jsr: case opc_goto:
            case opc_ifeq: case opc_ifge: case opc_ifgt:
            case opc_ifle: case opc_iflt: case opc_ifne:
            case opc_if_icmpeq: case opc_if_icmpne: case opc_if_icmpge:
            case opc_if_icmpgt: case opc_if_icmple: case opc_if_icmplt:
            case opc_if_acmpeq: case opc_if_acmpne:
            case opc_ifnull: case opc_ifnonnull:
                widened = mi->widening[pos];
                delta = readS2(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                new_opcode = opcode;
                if (widened == 0) {
                    writeU1(ci, opcode);
                    writeU2(ci, new_delta);
                } else if (widened == 2) {
                    switch (opcode) {
                        case opc_jsr:
                            new_opcode = opc_jsr_w;
                            break;
                        case opc_goto:
                            new_opcode = opc_jsr_w;
                            break;
                        default:
                            CRW_FATAL(ci, "unexpected opcode");
                            break;
                    }
                    writeU1(ci, new_opcode);
                    writeU4(ci, new_delta);
                } else if (widened == 5) {
                    switch (opcode) {
                        case opc_ifeq:
                            new_opcode = opc_ifne;
                            break;
                        case opc_ifge:
                            new_opcode = opc_iflt;
                            break;
                        case opc_ifgt:
                            new_opcode = opc_ifle;
                            break;
                        case opc_ifle:
                            new_opcode = opc_ifgt;
                            break;
                        case opc_iflt:
                            new_opcode = opc_ifge;
                            break;
                        case opc_ifne:
                            new_opcode = opc_ifeq;
                            break;
                        case opc_if_icmpeq:
                            new_opcode = opc_if_icmpne;
                            break;
                        case opc_if_icmpne:
                            new_opcode = opc_if_icmpeq;
                            break;
                        case opc_if_icmpge:
                            new_opcode = opc_if_icmplt;
                            break;
                        case opc_if_icmpgt:
                            new_opcode = opc_if_icmple;
                            break;
                        case opc_if_icmple:
                            new_opcode = opc_if_icmpgt;
                            break;
                        case opc_if_icmplt:
                            new_opcode = opc_if_icmpge;
                            break;
                        case opc_if_acmpeq:
                            new_opcode = opc_if_acmpne;
                            break;
                        case opc_if_acmpne:
                            new_opcode = opc_if_acmpeq;
                            break;
                        case opc_ifnull:
                            new_opcode = opc_ifnonnull;
                            break;
                        case opc_ifnonnull:
                            new_opcode = opc_ifnull;
                            break;
                        default:
                            CRW_FATAL(ci, "Unexpected opcode");
                        break;
                    }
                    writeU1(ci, new_opcode);    /* write inverse branch */
                    writeU2(ci, 3 + 5);         /* beyond if and goto_w */
                    writeU1(ci, opc_goto_w);    /* add a goto_w */
                    writeU4(ci, new_delta); /* write new and wide delta */
                } else {
                    CRW_FATAL(ci, "Unexpected widening");
                }
                break;

            case opc_jsr_w:
            case opc_goto_w:
                delta = readU4(ci);
                new_delta = method_code_map(mi,pos+delta) - new_pos;
                writeU1(ci, opcode);
                writeU4(ci, new_delta);
                break;

            default:
                instr_len = opcode_length[opcode];
                writeU1(ci, opcode);
                copy(ci, instr_len-1);
                break;
        }
    }
}

static void
method_inject_and_write_code(MethodImage *mi)
{
    ByteCode bytecodes[LARGEST_INJECTION+1];
    ByteOffset   len;

    CRW_ASSERT_MI(mi);

    /* Do injections */
    rewind_to_beginning_of_input_bytecodes(mi);
    len = entry_injection_code(mi, bytecodes, (int)sizeof(bytecodes));
    if ( len > 0 ) {
        int pos;

        pos = 0;
        inject_bytecodes(mi, pos, bytecodes, len);
	/* Adjust pos 0 to map to new pos 0, you never want to
	 *  jump into this entry code injection. So the new pos 0
	 *  will be past this entry_injection_code().
	 */
        adjust_map(mi, pos, len); /* Inject before behavior */
    }
    while (input_code_offset(mi) < mi->code_len) {
        inject_for_opcode(mi);
    }

    /* Adjust instructions */
    rewind_to_beginning_of_input_bytecodes(mi);
    while (input_code_offset(mi) < mi->code_len) {
        if (!adjust_instruction(mi)) {
            rewind_to_beginning_of_input_bytecodes(mi);
        }
    }

    /* Write new instructions */
    rewind_to_beginning_of_input_bytecodes(mi);
    while (input_code_offset(mi) < mi->code_len) {
        write_instruction(mi);
    }
}

static void
copy_attribute(CrwClassImage *ci)
{
    int len;

    (void)copyU2(ci);
    len = copyU4(ci);
    copy(ci, len);
}

static void
copy_attributes(CrwClassImage *ci)
{
    unsigned i;
    unsigned count;

    count = copyU2(ci);
    for (i = 0; i < count; ++i) {
        copy_attribute(ci);
    }
}

static void
copy_all_fields(CrwClassImage *ci)
{
    unsigned i;
    unsigned count;

    count = copyU2(ci);
    for (i = 0; i < count; ++i) {
        /* access, name, descriptor */
        copy(ci, 6);
        copy_attributes(ci);
    }
}

static void
write_line_table(MethodImage *mi)
{
    unsigned             i;
    unsigned             count;
    CrwClassImage *      ci;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    (void)copyU4(ci);
    count = copyU2(ci);
    for(i=0; i<count; i++) {
        ByteOffset start_pc;
        ByteOffset new_start_pc;

        start_pc = readU2(ci);

        if ( start_pc == 0 ) {
	    new_start_pc = 0; /* Don't skip entry injection code. */
	} else {
	    new_start_pc = method_code_map(mi, start_pc);
	}

        writeU2(ci, new_start_pc);
        (void)copyU2(ci);
    }
}

/* Used for LocalVariableTable and LocalVariableTypeTable attributes */
static void
write_var_table(MethodImage *mi)
{
    unsigned             i;
    unsigned             count;
    CrwClassImage *      ci;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    (void)copyU4(ci);
    count = copyU2(ci);
    for(i=0; i<count; i++) {
        ByteOffset start_pc;
        ByteOffset new_start_pc;
        ByteOffset length;
        ByteOffset new_length;
        ByteOffset end_pc;
        ByteOffset new_end_pc;

        start_pc        = readU2(ci);
        length          = readU2(ci);

        if ( start_pc == 0 ) {
	    new_start_pc = 0; /* Don't skip entry injection code. */
	} else {
	    new_start_pc = method_code_map(mi, start_pc);
	}
        end_pc          = start_pc + length;
        new_end_pc      = method_code_map(mi, end_pc);
        new_length      = new_end_pc - new_start_pc;

        writeU2(ci, new_start_pc);
        writeU2(ci, new_length);
        (void)copyU2(ci);
        (void)copyU2(ci);
        (void)copyU2(ci);
    }
}

static void
method_write_exception_table(MethodImage *mi)
{
    unsigned            i;
    unsigned            count;
    CrwClassImage *     ci;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    count = copyU2(ci);
    for(i=0; i<count; i++) {
        ByteOffset start_pc;
        ByteOffset new_start_pc;
        ByteOffset end_pc;
        ByteOffset new_end_pc;
        ByteOffset handler_pc;
        ByteOffset new_handler_pc;

        start_pc        = readU2(ci);
        end_pc          = readU2(ci);
        handler_pc      = readU2(ci);

	new_start_pc    = method_code_map(mi, start_pc);
        new_end_pc      = method_code_map(mi, end_pc);
        new_handler_pc  = method_code_map(mi, handler_pc);

        writeU2(ci, new_start_pc);
        writeU2(ci, new_end_pc);
        writeU2(ci, new_handler_pc);
        (void)copyU2(ci);
    }
}

static int
attribute_match(CrwClassImage *ci, CrwCpoolIndex name_index, const char *name)
{
    CrwConstantPoolEntry cs;
    int                  len;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, name!=NULL);
    len = (int)strlen(name);
    cs = cpool_entry(ci, name_index);
    if ( cs.len==len && strncmp(cs.ptr, name, len)==0) {
       return 1;
    }
    return 0;
}

static void
method_write_code_attribute(MethodImage *mi)
{
    CrwClassImage *     ci;
    CrwCpoolIndex       name_index;

    CRW_ASSERT_MI(mi);
    ci = mi->ci;
    name_index = copyU2(ci);
    if ( attribute_match(ci, name_index, "LineNumberTable") ) {
        write_line_table(mi);
    } else if ( attribute_match(ci, name_index, "LocalVariableTable") ) {
        write_var_table(mi);
    } else if ( attribute_match(ci, name_index, "LocalVariableTypeTable") ) {
        write_var_table(mi); /* Exact same format as the LocalVariableTable */
    } else {
        unsigned len;
        len = copyU4(ci);
        copy(ci, len);
    }
}

static int
is_init_method(const char *name)
{
    if ( name!=NULL && strcmp(name,"<init>")==0 ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static int
is_clinit_method(const char *name)
{
    if ( name!=NULL && strcmp(name,"<clinit>")==0 ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static int
is_finalize_method(const char *name)
{
    if ( name!=NULL && strcmp(name,"finalize")==0 ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static int
skip_method(CrwClassImage *ci, const char *name,
		unsigned access_flags, ByteOffset code_len,
		int system_class, jboolean *pskip_call_return_sites)
{
    *pskip_call_return_sites = JNI_FALSE;
    if ( system_class ) {
	if ( code_len == 1 && is_init_method(name) ) {
	    return JNI_TRUE;
	} else if ( code_len == 1 && is_finalize_method(name) ) {
	    return JNI_TRUE;
	} else if ( is_clinit_method(name) ) {
	    return JNI_TRUE;
	} else if ( ci->is_thread_class && strcmp(name,"currentThread")==0 ) {
	    return JNI_TRUE;
	}
	/*
	if ( access_flags & JVM_ACC_PRIVATE ) {
	    *pskip_call_return_sites = JNI_TRUE;
	}
	*/
    }
    return JNI_FALSE;
}

/* Process all code attributes */
static void
method_write_bytecodes(CrwClassImage *ci, unsigned mnum, unsigned access_flags)
{
    CrwPosition         output_attr_len_position;
    CrwPosition         output_max_stack_position;
    CrwPosition         output_code_len_position;
    CrwPosition         start_of_output_bytecodes;
    unsigned            i;
    unsigned            attr_len;
    unsigned            max_stack;
    ByteOffset          code_len;
    ByteOffset          new_code_len;
    unsigned            attr_count;
    unsigned            new_attr_len;
    MethodImage *       mi;
    jboolean            object_init_method;
    jboolean            skip_call_return_sites;

    CRW_ASSERT_CI(ci);

    /* Attribute Length */
    output_attr_len_position = ci->output_position;
    attr_len = copyU4(ci);

    /* Max Stack */
    output_max_stack_position = ci->output_position;
    max_stack = copyU2(ci);

    /* Max Locals */
    (void)copyU2(ci);

    /* Code Length */
    output_code_len_position = ci->output_position;
    code_len = copyU4(ci);
    start_of_output_bytecodes = ci->output_position;

    /* Some methods should not be instrumented */
    object_init_method = JNI_FALSE;
    skip_call_return_sites = JNI_FALSE;
    if ( ci->is_object_class &&
         is_init_method(ci->method_name[mnum]) &&
         strcmp(ci->method_descr[mnum],"()V")==0 ) {
        object_init_method = JNI_TRUE;
        skip_call_return_sites = JNI_TRUE;
    } else if ( skip_method(ci, ci->method_name[mnum], access_flags,
		code_len, ci->system_class, &skip_call_return_sites) ) {
        /* Copy remainder minus already copied, the U2 max_stack,
         *   U2 max_locals, and U4 code_length fields have already
         *   been processed.
         */
        copy(ci, attr_len - (2+2+4));
        return;
    }

    /* Start Injection */
    mi = method_init(ci, mnum, code_len);
    mi->object_init_method = object_init_method;
    mi->access_flags = access_flags;
    mi->skip_call_return_sites = skip_call_return_sites;

    /* Save the current position as the start of the input bytecodes */
    mi->start_of_input_bytecodes = ci->input_position;

    /* The max stack may increase */
    mi->max_stack = max_stack;
    mi->new_max_stack = max_stack;

    /* Adjust all code offsets */
    method_inject_and_write_code(mi);

    /* Fix up code length */
    new_code_len = (int)(ci->output_position - start_of_output_bytecodes);
    random_writeU4(ci, output_code_len_position, new_code_len);

    /* Fixup max stack */
    CRW_ASSERT(ci, mi->new_max_stack <= 0xFFFF);
    random_writeU2(ci, output_max_stack_position, mi->new_max_stack);

    /* Copy exception table */
    method_write_exception_table(mi);

    /* Copy code attributes */
    attr_count = copyU2(ci);
    for (i = 0; i < attr_count; ++i) {
        method_write_code_attribute(mi);
    }

    /* Fix up attribute length */
    new_attr_len = (int)(ci->output_position - (output_attr_len_position + 4));
    random_writeU4(ci, output_attr_len_position, new_attr_len);

    /* Free method data */
    method_term(mi);
    mi = NULL;

}

static void
method_write(CrwClassImage *ci, unsigned mnum)
{
    unsigned            i;
    unsigned            access_flags;
    CrwCpoolIndex       name_index;
    CrwCpoolIndex       descr_index;
    unsigned            attr_count;

    access_flags = copyU2(ci);
    name_index = copyU2(ci);
    ci->method_name[mnum] = cpool_entry(ci, name_index).ptr;
    descr_index = copyU2(ci);
    ci->method_descr[mnum] = cpool_entry(ci, descr_index).ptr;
    attr_count = copyU2(ci);

    for (i = 0; i < attr_count; ++i) {
        CrwCpoolIndex name_index;

        name_index = copyU2(ci);
        if ( attribute_match(ci, name_index, "Code") ) {
            method_write_bytecodes(ci, mnum, access_flags);
        } else {
            unsigned len;
            len = copyU4(ci);
            copy(ci, len);
        }
    }
}

static void
method_write_all(CrwClassImage *ci)
{
    unsigned i;
    unsigned count;

    count = copyU2(ci);
    ci->method_count = count;
    if ( count > 0 ) {
        ci->method_name = (const char **)allocate_clean(ci, count*(int)sizeof(const char*));
        ci->method_descr = (const char **)allocate_clean(ci, count*(int)sizeof(const char*));
    }

    for (i = 0; i < count; ++i) {
        method_write(ci, i);
    }

    if ( ci->mnum_callback != NULL ) {
        (*(ci->mnum_callback))(ci->number, ci->method_name, ci->method_descr,
			 count);
    }
}

/* ------------------------------------------------------------------- */
/* Cleanup function. */

static void
cleanup(CrwClassImage *ci)
{
    CRW_ASSERT_CI(ci);
    if ( ci->name != NULL ) {
        deallocate(ci, (void*)ci->name);
        ci->name = NULL;
    }
    if ( ci->method_name != NULL ) {
        deallocate(ci, (void*)ci->method_name);
        ci->method_name = NULL;
    }
    if ( ci->method_descr != NULL ) {
        deallocate(ci, (void*)ci->method_descr);
        ci->method_descr = NULL;
    }
    if ( ci->cpool != NULL ) {
        CrwCpoolIndex i;
        for(i=0; i<ci->cpool_count_plus_one; i++) {
            if ( ci->cpool[i].ptr != NULL ) {
                deallocate(ci, (void*)(ci->cpool[i].ptr));
                ci->cpool[i].ptr = NULL;
            }
        }
        deallocate(ci, (void*)ci->cpool);
        ci->cpool = NULL;
    }
}

static jboolean
skip_class(unsigned access_flags)
{
    if ( access_flags & JVM_ACC_INTERFACE ) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

static long
inject_class(struct CrwClassImage *ci,
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
		 unsigned char *buf,
                 long buf_len)
{
    CrwConstantPoolEntry        cs;
    CrwCpoolIndex               this_class;
    CrwCpoolIndex               super_class;
    unsigned                    magic;
    unsigned                    interface_count;

    CRW_ASSERT_CI(ci);
    CRW_ASSERT(ci, buf!=NULL);
    CRW_ASSERT(ci, buf_len!=0);

    CRW_ASSERT(ci, strchr(tclass_name,'.')==NULL); /* internal qualified name */

    ci->injection_count 	= 0;
    ci->system_class            = system_class;
    ci->tclass_name		= tclass_name;
    ci->tclass_sig		= tclass_sig;
    ci->call_name		= call_name;
    ci->call_sig		= call_sig;
    ci->return_name		= return_name;
    ci->return_sig		= return_sig;
    ci->obj_init_name		= obj_init_name;
    ci->obj_init_sig		= obj_init_sig;
    ci->newarray_name		= newarray_name;
    ci->newarray_sig		= newarray_sig;
    ci->output                  = buf;
    ci->output_len              = buf_len;

    magic = copyU4(ci);
    CRW_ASSERT(ci, magic==0xCAFEBABE);
    if ( magic != 0xCAFEBABE ) {
	return (long)0;
    }

    /* minor version number not used */
    (void)copyU2(ci);
    /* major version number not used */
    (void)copyU2(ci);

    cpool_setup(ci);

    ci->access_flags        = copyU2(ci);
    if ( skip_class(ci->access_flags) ) {
        return (long)0;
    }

    this_class          = copyU2(ci);

    cs = cpool_entry(ci, (CrwCpoolIndex)(cpool_entry(ci, this_class).index1));
    if ( ci->name == NULL ) {
        ci->name = duplicate(ci, cs.ptr, cs.len);
        CRW_ASSERT(ci, strchr(ci->name,'.')==NULL); /* internal qualified name */
    }
    CRW_ASSERT(ci, (int)strlen(ci->name)==cs.len && strncmp(ci->name, cs.ptr, cs.len)==0);

    super_class         = copyU2(ci);
    if ( super_class == 0 ) {
        ci->is_object_class = JNI_TRUE;
        CRW_ASSERT(ci, strcmp(ci->name,"java/lang/Object")==0);
    }

    interface_count     = copyU2(ci);
    copy(ci, interface_count * 2);

    copy_all_fields(ci);

    method_write_all(ci);

    if ( ci->injection_count == 0 ) {
        return (long)0;
    }

    copy_attributes(ci);

    return (long)ci->output_position;
}

/* ------------------------------------------------------------------- */
/* Exported interfaces */

JNIEXPORT void JNICALL
java_crw_demo(unsigned class_number,
	 const char *name,
	 const unsigned char *file_image,
	 long file_len,
	 int system_class,
         char* tclass_name,	/* Name of class that has tracker methods. */
         char* tclass_sig,	/* Signature of tclass */
         char* call_name,	/* Method name to call at offset 0 */
         char* call_sig,	/* Signature of this method */
         char* return_name,	/* Method name to call before any return */
         char* return_sig,	/* Signature of this method */
         char* obj_init_name,	/* Method name to call in Object <init> */
         char* obj_init_sig,	/* Signature of this method */
         char* newarray_name,	/* Method name to call after newarray opcodes */
         char* newarray_sig,	/* Signature of this method */
	 unsigned char **pnew_file_image,
	 long *pnew_file_len,
	 FatalErrorHandler fatal_error_handler,
	 MethodNumberRegister mnum_callback)
{
    CrwClassImage ci;
    long          max_length;
    long          new_length;
    void         *new_image;
    int           len;

    /* Initial setup of the CrwClassImage structure */
    (void)memset(&ci, 0, (int)sizeof(CrwClassImage));
    ci.fatal_error_handler = fatal_error_handler;
    ci.mnum_callback       = mnum_callback;

    /* Do some interface error checks */
    if ( pnew_file_image==NULL ) {
        CRW_FATAL(&ci, "pnew_file_image==NULL");
    }
    if ( pnew_file_len==NULL ) {
        CRW_FATAL(&ci, "pnew_file_len==NULL");
    }

    /* No file length means do nothing */
    *pnew_file_image = NULL;
    *pnew_file_len = 0;
    if ( file_len==0 ) {
	return;
    }

    /* Do some more interface error checks */
    if ( file_image == NULL ) {
        CRW_FATAL(&ci, "file_image == NULL");
    }
    if ( file_len < 0 ) {
        CRW_FATAL(&ci, "file_len < 0");
    }
    if ( system_class != 0 && system_class != 1 ) {
        CRW_FATAL(&ci, "system_class is not 0 or 1");
    }
    if ( tclass_name == NULL ) {
        CRW_FATAL(&ci, "tclass_name == NULL");
    }
    if ( tclass_sig == NULL || tclass_sig[0]!='L' ) {
        CRW_FATAL(&ci, "tclass_sig is not a valid class signature");
    }
    len = (int)strlen(tclass_sig);
    if ( tclass_sig[len-1]!=';' ) {
        CRW_FATAL(&ci, "tclass_sig is not a valid class signature");
    }
    if ( call_name != NULL ) {
	if ( call_sig == NULL || strcmp(call_sig, "(II)V") != 0 ) {
            CRW_FATAL(&ci, "call_sig is not (II)V");
	}
    }
    if ( return_name != NULL ) {
	if ( return_sig == NULL || strcmp(return_sig, "(II)V") != 0 ) {
            CRW_FATAL(&ci, "return_sig is not (II)V");
	}
    }
    if ( obj_init_name != NULL ) {
	if ( obj_init_sig == NULL || strcmp(obj_init_sig, "(Ljava/lang/Object;)V") != 0 ) {
            CRW_FATAL(&ci, "obj_init_sig is not (Ljava/lang/Object;)V");
	}
    }
    if ( newarray_name != NULL ) {
	if ( newarray_sig == NULL || strcmp(newarray_sig, "(Ljava/lang/Object;)V") != 0 ) {
            CRW_FATAL(&ci, "newarray_sig is not (Ljava/lang/Object;)V");
	}
    }

    /* Finish setup the CrwClassImage structure */
    ci.is_thread_class = JNI_FALSE;
    if ( name != NULL ) {
        CRW_ASSERT(&ci, strchr(name,'.')==NULL); /* internal qualified name */

        ci.name = duplicate(&ci, name, (int)strlen(name));
        if ( strcmp(name, "java/lang/Thread")==0 ) {
	    ci.is_thread_class = JNI_TRUE;
        }
    }
    ci.number = class_number;
    ci.input = file_image;
    ci.input_len = file_len;

    /* Do the injection */
    max_length = file_len*2 + 512; /* Twice as big + 512 */
    new_image = malloc((int)max_length);
    new_length = inject_class(&ci,
				 system_class,
				 tclass_name,
				 tclass_sig,
				 call_name,
				 call_sig,
				 return_name,
				 return_sig,
				 obj_init_name,
				 obj_init_sig,
				 newarray_name,
				 newarray_sig,
				 new_image,
				 max_length);

    /* Dispose or shrink the space to be returned. */
    if ( new_length == 0 ) {
	(void)free(new_image);
	new_image = NULL;
    } else {
        new_image = (void*)realloc((void*)new_image, (int)new_length);
    }

    /* Return the new class image */
    *pnew_file_image = (unsigned char *)new_image;
    *pnew_file_len = (long)new_length;

    /* Cleanup before we leave. */
    cleanup(&ci);
}

