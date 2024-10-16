/*
 * @(#)src/demo/jvmti/hprof/hprof_blocks.c, dsdev, dsdev, 20060202 1.3
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
 * @(#)hprof_blocks.c	1.3 04/07/27
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

/* Allocations from large blocks, no individual free's */

#include "hprof.h"

/*
 * This file contains some allocation code that allows you
 *   to have space allocated via larger blocks of space.
 * The only free allowed is of all the blocks and all the elements.
 * Elements can be of different alignments and fixed or variable sized.
 * The space allocated never moves.
 *
 */

/* Get the real size allocated based on alignment and bytes needed */
static int
real_size(int alignment, int nbytes)
{
    if ( alignment > 1 ) {
	int wasted;

	wasted = alignment - ( nbytes % alignment );
	if ( wasted != alignment ) {
	    nbytes += wasted;
	}
    }
    return nbytes;
}

/* Add a new current_block to the Blocks* chain, adjust size if nbytes big. */
static void
add_block(Blocks *blocks, int nbytes)
{
    int header_size;
    int block_size;
    BlockHeader *block_header;

    HPROF_ASSERT(blocks!=NULL);
    HPROF_ASSERT(nbytes>0);

    header_size          = real_size(blocks->alignment, sizeof(BlockHeader));
    block_size           = blocks->elem_size*blocks->population;
    if ( nbytes > block_size ) {
	block_size = real_size(blocks->alignment, nbytes);
    }
    block_header         = (BlockHeader*)HPROF_MALLOC(block_size+header_size);
    block_header->next   = NULL;
    block_header->bytes_left = block_size;
    block_header->next_pos   = header_size;

    /* Link in new block */
    if ( blocks->current_block != NULL ) {
        blocks->current_block->next = block_header;
    }
    blocks->current_block = block_header;
    if ( blocks->first_block == NULL ) {
        blocks->first_block = block_header;
    }
}

/* Initialize a new Blocks */
Blocks *
blocks_init(int alignment, int elem_size, int population)
{
    Blocks *blocks;

    HPROF_ASSERT(alignment>0);
    HPROF_ASSERT(elem_size>0);
    HPROF_ASSERT(population>0);

    blocks                = (Blocks*)HPROF_MALLOC(sizeof(Blocks));
    blocks->alignment     = alignment;
    blocks->elem_size     = elem_size;
    blocks->population    = population;
    blocks->first_block   = NULL;
    blocks->current_block = NULL;
    return blocks;
}

/* Allocate bytes from a Blocks area. */
void *
blocks_alloc(Blocks *blocks, int nbytes)
{
    BlockHeader *block;
    int   pos;
    void *ptr;

    HPROF_ASSERT(blocks!=NULL);
    HPROF_ASSERT(nbytes>=0);
    if ( nbytes == 0 ) {
	return NULL;
    }

    block = blocks->current_block;
    if ( block == NULL || block->bytes_left < nbytes ) {
        add_block(blocks, nbytes);
        block = blocks->current_block;
    }
    pos = block->next_pos;
    nbytes = real_size(blocks->alignment, nbytes);
    ptr = (void*)(((char*)block)+pos);
    block->next_pos   += nbytes;
    block->bytes_left -= nbytes;
    return ptr;
}

/* Terminate the Blocks */
void
blocks_term(Blocks *blocks)
{
    BlockHeader *block;

    HPROF_ASSERT(blocks!=NULL);

    block = blocks->first_block;
    while ( block != NULL ) {
	BlockHeader *next_block;

	next_block = block->next;
	HPROF_FREE(block);
	block = next_block;
    }
    HPROF_FREE(blocks);
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

/* Allocations from large blocks, no individual free's */

#include "hprof.h"

/*
 * This file contains some allocation code that allows you
 *   to have space allocated via larger blocks of space.
 * The only free allowed is of all the blocks and all the elements.
 * Elements can be of different alignments and fixed or variable sized.
 * The space allocated never moves.
 *
 */

/* Get the real size allocated based on alignment and bytes needed */
static int
real_size(int alignment, int nbytes)
{
    if ( alignment > 1 ) {
	int wasted;

	wasted = alignment - ( nbytes % alignment );
	if ( wasted != alignment ) {
	    nbytes += wasted;
	}
    }
    return nbytes;
}

/* Add a new current_block to the Blocks* chain, adjust size if nbytes big. */
static void
add_block(Blocks *blocks, int nbytes)
{
    int header_size;
    int block_size;
    BlockHeader *block_header;

    HPROF_ASSERT(blocks!=NULL);
    HPROF_ASSERT(nbytes>0);

    header_size          = real_size(blocks->alignment, sizeof(BlockHeader));
    block_size           = blocks->elem_size*blocks->population;
    if ( nbytes > block_size ) {
	block_size = real_size(blocks->alignment, nbytes);
    }
    block_header         = (BlockHeader*)HPROF_MALLOC(block_size+header_size);
    block_header->next   = NULL;
    block_header->bytes_left = block_size;
    block_header->next_pos   = header_size;

    /* Link in new block */
    if ( blocks->current_block != NULL ) {
        blocks->current_block->next = block_header;
    }
    blocks->current_block = block_header;
    if ( blocks->first_block == NULL ) {
        blocks->first_block = block_header;
    }
}

/* Initialize a new Blocks */
Blocks *
blocks_init(int alignment, int elem_size, int population)
{
    Blocks *blocks;

    HPROF_ASSERT(alignment>0);
    HPROF_ASSERT(elem_size>0);
    HPROF_ASSERT(population>0);

    blocks                = (Blocks*)HPROF_MALLOC(sizeof(Blocks));
    blocks->alignment     = alignment;
    blocks->elem_size     = elem_size;
    blocks->population    = population;
    blocks->first_block   = NULL;
    blocks->current_block = NULL;
    return blocks;
}

/* Allocate bytes from a Blocks area. */
void *
blocks_alloc(Blocks *blocks, int nbytes)
{
    BlockHeader *block;
    int   pos;
    void *ptr;

    HPROF_ASSERT(blocks!=NULL);
    HPROF_ASSERT(nbytes>=0);
    if ( nbytes == 0 ) {
	return NULL;
    }

    block = blocks->current_block;
    if ( block == NULL || block->bytes_left < nbytes ) {
        add_block(blocks, nbytes);
        block = blocks->current_block;
    }
    pos = block->next_pos;
    nbytes = real_size(blocks->alignment, nbytes);
    ptr = (void*)(((char*)block)+pos);
    block->next_pos   += nbytes;
    block->bytes_left -= nbytes;
    return ptr;
}

/* Terminate the Blocks */
void
blocks_term(Blocks *blocks)
{
    BlockHeader *block;

    HPROF_ASSERT(blocks!=NULL);

    block = blocks->first_block;
    while ( block != NULL ) {
	BlockHeader *next_block;

	next_block = block->next;
	HPROF_FREE(block);
	block = next_block;
    }
    HPROF_FREE(blocks);
}

