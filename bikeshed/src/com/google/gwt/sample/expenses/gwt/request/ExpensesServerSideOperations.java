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
package com.google.gwt.sample.expenses.gwt.request;

import com.google.gwt.requestfactory.shared.RequestFactory.Config;
import com.google.gwt.requestfactory.shared.RequestFactory.RequestDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for
 * {@link com.google.gwt.requestfactory.server.RequestFactoryServlet
 * RequestFactoryServlet}
 */
public class ExpensesServerSideOperations implements Config {

  private static void putAll(RequestDefinition[] values,
      Map<String, RequestDefinition> newMap) {
    for (RequestDefinition def : values) {
      newMap.put(def.name(), def);
    }
  }

  private final Map<String, RequestDefinition> map;

  public ExpensesServerSideOperations() {
    Map<String, RequestDefinition> newMap = new HashMap<String, RequestDefinition>();
    putAll(EmployeeRequest.ServerOperations.values(), newMap);
    putAll(ReportRequest.ServerOperations.values(), newMap);
    map = Collections.unmodifiableMap(newMap);
  }

  public Map<String, RequestDefinition> requestDefinitions() {
    return map;
  }

}
