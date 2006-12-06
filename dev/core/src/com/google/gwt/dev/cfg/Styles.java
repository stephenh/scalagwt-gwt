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
package com.google.gwt.dev.cfg;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Manages a list of stylesheet urls.
 */
public class Styles {

  /**
   * Append a script.
   * @param src a partial or full url to a script to inject
   */
  public void append(String src) {
    list.addLast(src);
  }

  /**
   * An iterator over stylesheet urls (each one is a String).
   */
  public Iterator iterator() {
    return list.iterator();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  private final LinkedList list = new LinkedList();
}
