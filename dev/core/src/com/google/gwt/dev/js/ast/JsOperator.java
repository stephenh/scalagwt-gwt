/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.dev.js.ast;

/**
 * A JavaScript operator.
 */
public interface JsOperator {
  int INFIX = 0x02;
  int LEFT = 0x01;
  int POSTFIX = 0x04;
  int PREFIX = 0x08;

  int getPrecedence();

  String getSymbol();

  boolean isKeyword();

  boolean isLeftAssociative();

  boolean isPrecedenceLessThan(JsOperator other);

  boolean isValidInfix();

  boolean isValidPostfix();

  boolean isValidPrefix();

  String toString();
}
