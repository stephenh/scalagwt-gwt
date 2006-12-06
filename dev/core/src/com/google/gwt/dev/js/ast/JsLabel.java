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
package com.google.gwt.dev.js.ast;

/**
 * Represents a JavaScript label statement.
 */
public class JsLabel extends JsStatement implements HasName {

  public JsLabel(JsName label) {
    this.label = label;
  }

  public JsName getName() {
    return label;
  }

  public void traverse(JsVisitor v) {
    if (v.visit(this)) {
      stmt.traverse(v);
    }
    v.endVisit(this);
  }

  public void setStmt(JsStatement stmt) {
    this.stmt = stmt;
  }

  public JsStatement getStmt() {
    return stmt;
  }

  private final JsName label;
  private JsStatement stmt;
}
