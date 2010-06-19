/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.cell.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;

/**
 * Tests for {@link ButtonCell}.
 */
public class ButtonCellTest extends CellTestBase<String> {

  public void testOnBrowserEvent() {
    NativeEvent event = Document.get().createMouseUpEvent(0, 0, 0, 0, 0, false,
        false, false, false, NativeEvent.BUTTON_LEFT);
    testOnBrowserEvent(getExpectedInnerHtml(), event, null, "clickme",
        "clickme");
  }

  @Override
  protected boolean consumesEvents() {
    return true;
  }

  @Override
  protected Cell<String> createCell() {
    return new ButtonCell();
  }

  @Override
  protected String createCellValue() {
    return "clickme";
  }

  @Override
  protected boolean dependsOnSelection() {
    return false;
  }

  @Override
  protected String getExpectedInnerHtml() {
    return "<button>clickme</button>";
  }

  @Override
  protected String getExpectedInnerHtmlNull() {
    return "<button></button>";
  }
}