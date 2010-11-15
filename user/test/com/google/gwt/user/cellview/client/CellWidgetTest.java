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
package com.google.gwt.user.cellview.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.ProvidesKey;

/**
 * Tests for {@link CellWidget}.
 */
public class CellWidgetTest extends GWTTestCase {

  /**
   * A custom cell used for testing.
   */
  private static class CustomCell extends AbstractCell<String> {

    private String lastEventValue;
    private Object lastEventKey;

    public CustomCell() {
      super("change");
    }

    public void assertLastEventKey(String expected) {
      assertEquals(expected, lastEventKey);
      lastEventKey = null;
    }

    public void assertLastEventValue(String expected) {
      assertEquals(expected, lastEventValue);
      lastEventValue = null;
    }

    @Override
    public void onBrowserEvent(Element parent, String value, Object key,
        NativeEvent event, ValueUpdater<String> valueUpdater) {
      lastEventValue = value;
      lastEventKey = key;
      if (valueUpdater != null) {
        valueUpdater.update("newValue");
      }
    }

    @Override
    public void render(String value, Object key, SafeHtmlBuilder sb) {
      if (value != null) {
        sb.appendEscaped(value);
      }
    }
  }

  /**
   * A mock value change handler used for testing.
   * 
   * @param <C> the data type
   */
  private static class MockValueChangeHandler<C> implements
      ValueChangeHandler<C> {

    private boolean onValueChangeCalled = false;
    private C lastValue;

    public void assertOnValueChangeNotCalled() {
      assertFalse(onValueChangeCalled);
    }

    public void assertLastValue(C expected) {
      assertTrue(onValueChangeCalled);
      assertEquals(expected, lastValue);
      lastValue = null;
      onValueChangeCalled = false;
    }

    public void onValueChange(ValueChangeEvent<C> event) {
      assertFalse("ValueChangeEvent fired twice", onValueChangeCalled);
      onValueChangeCalled = true;
      lastValue = event.getValue();
    }
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.user.cellview.CellView";
  }

  public void testOnBrowserEvent() {
    CustomCell cell = new CustomCell();
    CellWidget<String> cw = new CellWidget<String>(cell, "test");
    assertEquals("test", cw.getValue());

    Event event = Document.get().createChangeEvent().cast();
    cw.onBrowserEvent(event);
    cell.assertLastEventKey("test");
    cell.assertLastEventValue("test");
  }

  public void testOnBrowserEventWithKeyProvider() {
    ProvidesKey<String> keyProvider = new ProvidesKey<String>() {
      public Object getKey(String item) {
        // Return the first character as the key.
        return (item == null) ? null : item.substring(0, 1);
      }
    };
    CustomCell cell = new CustomCell();
    final CellWidget<String> cw = new CellWidget<String>(cell, "test",
        keyProvider);
    assertEquals("test", cw.getValue());
    assertEquals(keyProvider, cw.getKeyProvider());

    Event event = Document.get().createChangeEvent().cast();
    cw.onBrowserEvent(event);
    cell.assertLastEventKey("t");
    cell.assertLastEventValue("test");
  }

  public void testOnBrowserEventWithValueChangeHandler() {
    CustomCell cell = new CustomCell();
    final CellWidget<String> cw = new CellWidget<String>(cell, "test");
    assertEquals("test", cw.getValue());

    // Add a ValueChangeHandler.
    MockValueChangeHandler<String> handler = new MockValueChangeHandler<String>();
    cw.addValueChangeHandler(handler);

    // Fire an native event that will trigger a value change event.
    Event event = Document.get().createChangeEvent().cast();
    cw.onBrowserEvent(event);
    cell.assertLastEventKey("test");
    cell.assertLastEventValue("test");
    handler.assertLastValue("newValue");
  }

  public void testRedraw() {
    CellWidget<String> cw = new CellWidget<String>(new CustomCell());

    // Set value without redrawing.
    cw.setValue("test", false, false);
    assertEquals("", cw.getElement().getInnerText());

    // Redraw.
    cw.redraw();
    assertEquals("test", cw.getElement().getInnerText());
  }

  public void testSetValue() {
    CustomCell cell = new CustomCell();
    CellWidget<String> cw = new CellWidget<String>(cell, "initial");
    MockValueChangeHandler<String> handler = new MockValueChangeHandler<String>();
    cw.addValueChangeHandler(handler);

    // Check the intial value.
    assertEquals("initial", cw.getValue());
    assertEquals("initial", cw.getElement().getInnerText());

    // Set value without firing events.
    cw.setValue("test0");
    assertEquals("test0", cw.getValue());
    assertEquals("test0", cw.getElement().getInnerText());
    handler.assertOnValueChangeNotCalled();

    // Set value and fire events.
    cw.setValue("test1", true);
    assertEquals("test1", cw.getValue());
    assertEquals("test1", cw.getElement().getInnerText());
    handler.assertLastValue("test1");

    // Set value, fire events, but do not redraw.
    cw.setValue("test no redraw", true, false);
    assertEquals("test no redraw", cw.getValue());
    assertEquals("test1", cw.getElement().getInnerText());
    handler.assertLastValue("test no redraw");
  }
}