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
 * Represents a JavaScript for..in statement.
 */
public class JsForIn extends JsStatement {

  public JsForIn() {
    this(null);
  }

  public JsForIn(JsName iterVarName) {
    this.iterVarName = iterVarName;
  }

  public JsStatement getBody() {
    return body;
  }

  public JsExpression getIterExpr() {
    return iterExpr;
  }

  public JsName getIterVarName() {
    return iterVarName;
  }

  public JsExpression getObjExpr() {
    return objExpr;
  }

  public void setBody(JsStatement body) {
    this.body = body;
  }

  public void setIterExpr(JsExpression iterExpr) {
    this.iterExpr = iterExpr;
  }

  public void setObjExpr(JsExpression objExpr) {
    this.objExpr = objExpr;
  }

  public void traverse(JsVisitor v) {
    if (v.visit(this)) {
      if (iterExpr != null) {
        iterExpr.traverse(v);
      }
      objExpr.traverse(v);
      body.traverse(v);
    }
    v.endVisit(this);
  }

  private JsStatement body;
  private JsExpression iterExpr;
  // Optional: the name of a new iterator variable to introduce
  private final JsName iterVarName;
  private JsExpression objExpr;
}
