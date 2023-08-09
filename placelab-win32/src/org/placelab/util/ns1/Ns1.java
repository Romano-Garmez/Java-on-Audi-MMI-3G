// -*- Mode: Java; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*-
// vim:ts=2:sw=2:tw=80:et

// $Id: Ns1.java,v 1.2 2004/06/03 18:13:30 jscott Exp $
/* 
 * Copyright (c) 2003, Hugh Kennedy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the WiGLE.net nor Mimezine nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.placelab.util.ns1;

import java.util.*;

/**
 * data object for NS1 files.
 * holds APs
 */
public class Ns1 {
  
  /** the file version */
  public final int version;
  
  
  /** the APs in this file */
  private Vector aps = new Vector(0);

  public Ns1( int v ) {
    version = v;
  }
  
  public Ns1( int v, Vector ap_list ) {
    version = v;
    aps = new Vector( ap_list );
  }
  
  /**
   * @return the file version
   */
  public int getVersion() {
    return version;
  }

  /**
   * @return a read-only list of the APs in this Ns1
   */
  public Vector getAPs() {
  	// should be unmodifiable... 
    return aps;
  }
  
  public void addAP( AP ap ) {
    aps.add( ap );
  }
  
}
