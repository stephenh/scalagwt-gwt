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
package com.google.gwt.requestfactory.shared;

import com.google.gwt.valuestore.shared.ValueStore;
import com.google.gwt.valuestore.shared.Values;

import java.util.List;

/**
 * Marker interface for the RequestFactory code generator.
 */
public interface RequestFactory {

  String URL = "/expenses/data";

  /*
   * eventually, this will become an enum of update operations. 
   */
  String UPDATE_STRING = "SYNC";

  /**
   * Implemented by the request objects created by this factory.
   */
  interface RequestObject {
    void fire();

    String getRequestData();

    void handleResponseText(String responseText);
  }

  /**
   * Implemented by the RPC service backing this factory.
   */
  interface Service {
    void fire(RequestObject request);
  }

  ValueStore getValueStore();

  // TODO actually a DeltaValueStore, List is an interim hack
  SyncRequest syncRequest(final List<Values<?>> deltaValueStore);

  /**
   * Implemented by Enums sent in the request.
   */
  interface RequestDefinition {
    /**
     * Returns the name.
     */
    String name();

    /**
     * Returns the name of the (domain) class that contains the method to be
     * invoked on the server.
     */
    String getDomainClassName();

    /**
     * Returns the name of the method to be invoked on the server.
     */
    String getDomainMethodName();

    /**
     * Returns the parameter types of the method to be invoked on the server.
     */
    Class<?>[] getParameterTypes();

    /**
     * Returns the return type of the method to be invoked on the server.
     */
    Class<? extends EntityKey<?>> getReturnType();
  }
}
