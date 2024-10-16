/*
 * @(#)src/demo/jvmti/hprof/hprof_object.c, dsdev, dsdev, 20060202 1.3
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
 * @(#)hprof_object.c	1.21 04/07/27
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

/* Object table. */

/*
 * An Object is unique by it's allocation site (SiteIndex), it's size,
 *   it's kind, and it's serial number. Normally only the serial number
 *   would have been necessary for heap=dump, and these other items
 *   could have been moved to the ObjectInfo. An optimization left
 *   to the reader. Lookups are not normally done on ObjectIndex's
 *   anyway because we typically know when to create them.
 *   Objects that have been tagged, are tagged with an ObjectIndex,
 *   Objects that are not tagged need a ObjectIndex, a lookup when
 *     heap=sites, and a new one when heap=dump.
 *   Objects that are freed, need the tag converted to an ObjectIndex,
 *     so they can be freed, but only when heap=dump.
 *
 * This will likely be the largest table when using heap=dump, when
 *   there is one table entry per object.
 *
 * ObjectIndex entries differ between heap=dump and heap=sites.
 *   With heap=sites, each ObjectIndex represents a unique site, size,
 *   and kind of object, so many jobject's will map to a single ObjectIndex.
 *   With heap=dump, every ObjectIndex maps to a unique jobject.
 *
 * During processing of a heap dump, the references for the object
 *   this ObjectIndex represents is assigned to the references field
 *   of the ObjectInfo as a linked list. (see hprof_references.c).
 *   Once all the refernces are attached, they are processed into the
 *   appropriate hprof dump information.
 *
 * The references field is set and cleared as many times as the heap
 *   is dumped, as is the reference table.
 *
 */

#include "hprof.h"

typedef struct ObjectKey {
    SiteIndex    site_index;	/* Site of allocation */
    jint         size;          /* Size of object as reported by VM */
    ObjectKind   kind;	        /* Kind of object, most are OBJECT_NORMAL */
    SerialNumber serial_num;    /* For heap=dump, a unique number. */
} ObjectKey;

typedef struct ObjectInfo {
    RefIndex     references;        /* Linked list of refs in this object */
    SerialNumber thread_serial_num; /* Thread serial number for allocation */
} ObjectInfo;

/* Private internal functions. */

static ObjectKey*
get_pkey(ObjectIndex index)
{
    void *key_ptr;
    int   key_len;

    table_get_key(gdata->object_table, index, (void*)&key_ptr, &key_len);
    HPROF_ASSERT(key_len==(int)sizeof(ObjectKey));
    HPROF_ASSERT(key_ptr!=NULL);
    return (ObjectKey*)key_ptr;
}

static ObjectInfo *
get_info(ObjectIndex index)
{
    ObjectInfo *info;

    info = (ObjectInfo*)table_get_info(gdata->object_table, index);
    return info;
}

static void
list_item(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectKey  *pkey;
    ObjectInfo *info;

    HPROF_ASSERT(key_ptr!=NULL);
    HPROF_ASSERT(key_len!=0);
    HPROF_ASSERT(info_ptr!=NULL);

    info = (ObjectInfo*)info_ptr;

    pkey = (ObjectKey*)key_ptr;
    debug_message( "Object 0x%08x: site=0x%08x, SN=%u, "
			  " size=%d, kind=%d, refs=0x%x, threadSN=%u\n",
	 i, pkey->site_index, pkey->serial_num, pkey->size, pkey->kind,
	 info->references, info->thread_serial_num);
}

static void
clear_references(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectInfo *info;

    HPROF_ASSERT(info_ptr!=NULL);
    info = (ObjectInfo *)info_ptr;
    info->references = 0;
}

static void
dump_class_references(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectInfo *info;

    HPROF_ASSERT(info_ptr!=NULL);
    info = (ObjectInfo *)info_ptr;
    reference_dump_class((JNIEnv*)arg, i, info->references);
}

static void
dump_instance_references(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectInfo *info;

    HPROF_ASSERT(info_ptr!=NULL);
    info = (ObjectInfo *)info_ptr;
    reference_dump_instance((JNIEnv*)arg, i, info->references);
}

/* External interfaces. */

ObjectIndex
object_new(SiteIndex site_index, jint size, ObjectKind kind, SerialNumber thread_serial_num)
{
    ObjectIndex index;
    ObjectKey   key;
    static ObjectKey empty_key;

    key            = empty_key;
    key.site_index = site_index;
    key.size       = size;
    key.kind       = kind;
    if ( gdata->heap_dump ) {
	static ObjectInfo empty_info;
	ObjectInfo i;

	i = empty_info;
	i.thread_serial_num = thread_serial_num;
        key.serial_num = gdata->object_serial_number_counter++;
	index = table_create_entry(gdata->object_table,
			    &key, (int)sizeof(ObjectKey), &i);
    } else {
        key.serial_num =
	     class_get_serial_number(site_get_class_index(site_index));
	index = table_find_or_create_entry(gdata->object_table,
			    &key, (int)sizeof(ObjectKey), NULL, NULL);
    }
    site_update_stats(site_index, size, 1);
    return index;
}

void
object_init(void)
{
    jint bucket_count;

    bucket_count = 511;
    if ( gdata->heap_dump ) {
	bucket_count = 0;
    }
    HPROF_ASSERT(gdata->object_table==NULL);
    gdata->object_table = table_initialize("Object", 4096,
			4096, bucket_count, (int)sizeof(ObjectInfo));
}

SiteIndex
object_get_site(ObjectIndex index)
{
    ObjectKey *pkey;

    pkey = get_pkey(index);
    return pkey->site_index;
}

jint
object_get_size(ObjectIndex index)
{
    ObjectKey *pkey;

    pkey = get_pkey(index);
    return pkey->size;
}

ObjectKind
object_get_kind(ObjectIndex index)
{
    ObjectKey *pkey;

    pkey = get_pkey(index);
    return pkey->kind;
}

ObjectKind
object_free(ObjectIndex index)
{
    ObjectKey *pkey;
    ObjectKind kind;

    pkey = get_pkey(index);
    kind = pkey->kind;

    /* Decrement allocations at this site. */
    site_update_stats(pkey->site_index, -(pkey->size), -1);

    if ( gdata->heap_dump ) {
	table_free_entry(gdata->object_table, index);
    }
    return kind;
}

void
object_list(void)
{
    debug_message(
        "--------------------- Object Table ------------------------\n");
    table_walk_items(gdata->object_table, &list_item, NULL);
    debug_message(
        "----------------------------------------------------------\n");
}

void
object_cleanup(void)
{
    table_cleanup(gdata->object_table, NULL, NULL);
    gdata->object_table = NULL;
}

SerialNumber
object_get_thread_serial_number(ObjectIndex index)
{
    ObjectInfo *info;

    info = get_info(index);
    return info->thread_serial_num;
}

RefIndex
object_get_references(ObjectIndex index)
{
    ObjectInfo *info;

    info = get_info(index);
    return info->references;
}

void
object_set_references(ObjectIndex index, RefIndex ref_index)
{
    ObjectInfo *info;

    info = get_info(index);
    info->references = ref_index;
}

void
object_clear_references(void)
{
    table_walk_items(gdata->object_table, &clear_references, NULL);
}

void
object_reference_dump(JNIEnv *env)
{
    table_walk_items(gdata->object_table, &dump_instance_references, (void*)env);
    table_walk_items(gdata->object_table, &dump_class_references, (void*)env);
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

/* Object table. */

/*
 * An Object is unique by it's allocation site (SiteIndex), it's size,
 *   it's kind, and it's serial number. Normally only the serial number
 *   would have been necessary for heap=dump, and these other items
 *   could have been moved to the ObjectInfo. An optimization left
 *   to the reader. Lookups are not normally done on ObjectIndex's
 *   anyway because we typically know when to create them.
 *   Objects that have been tagged, are tagged with an ObjectIndex,
 *   Objects that are not tagged need a ObjectIndex, a lookup when
 *     heap=sites, and a new one when heap=dump.
 *   Objects that are freed, need the tag converted to an ObjectIndex,
 *     so they can be freed, but only when heap=dump.
 *
 * This will likely be the largest table when using heap=dump, when
 *   there is one table entry per object.
 *
 * ObjectIndex entries differ between heap=dump and heap=sites.
 *   With heap=sites, each ObjectIndex represents a unique site, size,
 *   and kind of object, so many jobject's will map to a single ObjectIndex.
 *   With heap=dump, every ObjectIndex maps to a unique jobject.
 *
 * During processing of a heap dump, the references for the object
 *   this ObjectIndex represents is assigned to the references field
 *   of the ObjectInfo as a linked list. (see hprof_references.c).
 *   Once all the refernces are attached, they are processed into the
 *   appropriate hprof dump information.
 *
 * The references field is set and cleared as many times as the heap
 *   is dumped, as is the reference table.
 *
 */

#include "hprof.h"

typedef struct ObjectKey {
    SiteIndex    site_index;	/* Site of allocation */
    jint         size;          /* Size of object as reported by VM */
    ObjectKind   kind;	        /* Kind of object, most are OBJECT_NORMAL */
    SerialNumber serial_num;    /* For heap=dump, a unique number. */
} ObjectKey;

typedef struct ObjectInfo {
    RefIndex     references;        /* Linked list of refs in this object */
    SerialNumber thread_serial_num; /* Thread serial number for allocation */
} ObjectInfo;

/* Private internal functions. */

static ObjectKey*
get_pkey(ObjectIndex index)
{
    void *key_ptr;
    int   key_len;

    table_get_key(gdata->object_table, index, (void*)&key_ptr, &key_len);
    HPROF_ASSERT(key_len==(int)sizeof(ObjectKey));
    HPROF_ASSERT(key_ptr!=NULL);
    return (ObjectKey*)key_ptr;
}

static ObjectInfo *
get_info(ObjectIndex index)
{
    ObjectInfo *info;

    info = (ObjectInfo*)table_get_info(gdata->object_table, index);
    return info;
}

static void
list_item(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectKey  *pkey;
    ObjectInfo *info;

    HPROF_ASSERT(key_ptr!=NULL);
    HPROF_ASSERT(key_len!=0);
    HPROF_ASSERT(info_ptr!=NULL);

    info = (ObjectInfo*)info_ptr;

    pkey = (ObjectKey*)key_ptr;
    debug_message( "Object 0x%08x: site=0x%08x, SN=%u, "
			  " size=%d, kind=%d, refs=0x%x, threadSN=%u\n",
	 i, pkey->site_index, pkey->serial_num, pkey->size, pkey->kind,
	 info->references, info->thread_serial_num);
}

static void
clear_references(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectInfo *info;

    HPROF_ASSERT(info_ptr!=NULL);
    info = (ObjectInfo *)info_ptr;
    info->references = 0;
}

static void
dump_class_references(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectInfo *info;

    HPROF_ASSERT(info_ptr!=NULL);
    info = (ObjectInfo *)info_ptr;
    reference_dump_class((JNIEnv*)arg, i, info->references);
}

static void
dump_instance_references(TableIndex i, void *key_ptr, int key_len, void *info_ptr, void *arg)
{
    ObjectInfo *info;

    HPROF_ASSERT(info_ptr!=NULL);
    info = (ObjectInfo *)info_ptr;
    reference_dump_instance((JNIEnv*)arg, i, info->references);
}

/* External interfaces. */

ObjectIndex
object_new(SiteIndex site_index, jint size, ObjectKind kind, SerialNumber thread_serial_num)
{
    ObjectIndex index;
    ObjectKey   key;
    static ObjectKey empty_key;

    key            = empty_key;
    key.site_index = site_index;
    key.size       = size;
    key.kind       = kind;
    if ( gdata->heap_dump ) {
	static ObjectInfo empty_info;
	ObjectInfo i;

	i = empty_info;
	i.thread_serial_num = thread_serial_num;
        key.serial_num = gdata->object_serial_number_counter++;
	index = table_create_entry(gdata->object_table,
			    &key, (int)sizeof(ObjectKey), &i);
    } else {
        key.serial_num =
	     class_get_serial_number(site_get_class_index(site_index));
	index = table_find_or_create_entry(gdata->object_table,
			    &key, (int)sizeof(ObjectKey), NULL, NULL);
    }
    site_update_stats(site_index, size, 1);
    return index;
}

void
object_init(void)
{
    jint bucket_count;

    bucket_count = 511;
    if ( gdata->heap_dump ) {
	bucket_count = 0;
    }
    HPROF_ASSERT(gdata->object_table==NULL);
    gdata->object_table = table_initialize("Object", 4096,
			4096, bucket_count, (int)sizeof(ObjectInfo));
}

SiteIndex
object_get_site(ObjectIndex index)
{
    ObjectKey *pkey;

    pkey = get_pkey(index);
    return pkey->site_index;
}

jint
object_get_size(ObjectIndex index)
{
    ObjectKey *pkey;

    pkey = get_pkey(index);
    return pkey->size;
}

ObjectKind
object_get_kind(ObjectIndex index)
{
    ObjectKey *pkey;

    pkey = get_pkey(index);
    return pkey->kind;
}

ObjectKind
object_free(ObjectIndex index)
{
    ObjectKey *pkey;
    ObjectKind kind;

    pkey = get_pkey(index);
    kind = pkey->kind;

    /* Decrement allocations at this site. */
    site_update_stats(pkey->site_index, -(pkey->size), -1);

    if ( gdata->heap_dump ) {
	table_free_entry(gdata->object_table, index);
    }
    return kind;
}

void
object_list(void)
{
    debug_message(
        "--------------------- Object Table ------------------------\n");
    table_walk_items(gdata->object_table, &list_item, NULL);
    debug_message(
        "----------------------------------------------------------\n");
}

void
object_cleanup(void)
{
    table_cleanup(gdata->object_table, NULL, NULL);
    gdata->object_table = NULL;
}

SerialNumber
object_get_thread_serial_number(ObjectIndex index)
{
    ObjectInfo *info;

    info = get_info(index);
    return info->thread_serial_num;
}

RefIndex
object_get_references(ObjectIndex index)
{
    ObjectInfo *info;

    info = get_info(index);
    return info->references;
}

void
object_set_references(ObjectIndex index, RefIndex ref_index)
{
    ObjectInfo *info;

    info = get_info(index);
    info->references = ref_index;
}

void
object_clear_references(void)
{
    table_walk_items(gdata->object_table, &clear_references, NULL);
}

void
object_reference_dump(JNIEnv *env)
{
    table_walk_items(gdata->object_table, &dump_instance_references, (void*)env);
    table_walk_items(gdata->object_table, &dump_class_references, (void*)env);
}
