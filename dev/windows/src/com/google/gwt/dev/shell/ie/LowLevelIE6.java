/*
 * Copyright 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.shell.ie;

import com.google.gwt.dev.shell.LowLevel;

/**
 * Various low-level helper methods for dealing with COM and such.
 */
class LowLevelIE6 {

  /**
   * Not instantiable.
   */
  private LowLevelIE6() {
  }

  public static synchronized void init() {
    // Force LowLevel initialization to load gwt-ll
    LowLevel.init();
  }

  /**
   * Does an HTTP GET that works with Windows proxy settings. Set the system
   * property <code>gwt.debugLowLevelHttpGet</code> to print failure status
   * codes to stderr.
   * 
   * @param userAgent the user-agent to specify for the GET
   * @param url the absolute URL to GET
   * @return the bytes of the full response (including headers), or
   *         <code>null</code> if there's a problem
   */
  public static byte[] httpGet(String userAgent, String url) {
    init();
    byte[][] out = new byte[1][];
    int status = _httpGet(userAgent, url, out);
    if (status == 0) {
      return out[0];
    } else {
      if (System.getProperty("gwt.debugLowLevelHttpGet") != null) {
        System.err.println("GET failed with status " + status + " for " + url);
      }
      return null;
    }
  }

  // CHECKSTYLE_OFF
  // out must be an array of size 1 to receive the array answer
  private static native int _httpGet(String userAgent, String url, byte[][] out);
  // CHECKSTYLE_ON
}
