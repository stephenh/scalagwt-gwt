/*
 * Copyright 2011 Google Inc.
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
package com.google.gwt.dom.builder.shared;

/**
 * HTML-based implementation of {@link SelectBuilder}.
 */
public class HtmlSelectBuilder extends HtmlElementBuilderBase<SelectBuilder> implements
    SelectBuilder {

  HtmlSelectBuilder(HtmlBuilderImpl delegate) {
    super(delegate);
  }

  @Override
  public SelectBuilder disabled() {
    return attribute("disabled", "disabled");
  }

  @Override
  public SelectBuilder multiple() {
    return attribute("multiple", "multiple");
  }

  @Override
  public SelectBuilder name(String name) {
    return attribute("name", name);
  }

  @Override
  public SelectBuilder selectedIndex(int index) {
    return attribute("index", index);
  }

  @Override
  public SelectBuilder size(int size) {
    return attribute("size", size);
  }

  @Override
  public SelectBuilder type(String type) {
    return attribute("type", type);
  }

  @Override
  public SelectBuilder value(String value) {
    return attribute("value", value);
  }
}