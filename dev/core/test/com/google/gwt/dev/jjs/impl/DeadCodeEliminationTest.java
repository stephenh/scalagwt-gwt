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
package com.google.gwt.dev.jjs.impl;

import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JProgram;

/**
 * Tests {@link DeadCodeElimination}.
 */
public class DeadCodeEliminationTest extends OptimizerTestBase {
  /*
   * TODO: this class needs more tests, and more sophisticated cases. Especially
   * to ensure we converge in a single pass.
   */

  @Override
  public void setUp() throws Exception {
    addSnippetClassDecl("static volatile boolean b;");
    addSnippetClassDecl("static volatile boolean b1;");
    addSnippetClassDecl("static volatile int i;");
    addSnippetClassDecl("static volatile long l;");
    addSnippetClassDecl("static volatile float f;");
    addSnippetClassDecl("static volatile double d;");
  }

  public void testConditionalOptimizations() throws Exception {
    optimize("int", "return true ? 3 : 4;").into("return 3;");
    optimize("int", "return false ? 3 : 4;").into("return 4;");

    optimize("boolean", "return b ? true : b1;").into("return b || b1;");
    optimize("boolean", "return b ? false : b1;").into("return !b && b1;");
    optimize("boolean", "return b ? b1 : true;").into("return !b || b1;");
    optimize("boolean", "return b ? b1 : false;").into("return b && b1;");
  }

  public void testIfOptimizations() throws Exception {
    optimize("int", "if (true) return 1; return 0;").into("return 1;");
    optimize("int", "if (false) return 1; return 0;").into("return 0;");
    optimize("int", "if (true) return 1; else return 2;").into("return 1;");
    optimize("int", "if (false) return 1; else return 2;").into("return 2;");

    optimize("int", "if (true) {} else return 4; return 0;").into("return 0;");

    addSnippetClassDecl("static boolean test() { return b; }");
    optimize("int", "if (test()) {} else {}; return 0;").into(
        "test(); return 0;");
  }

  public void testIfStatementToBoolean_NotOptimization() throws Exception {
    optimize("void", "if (!b) i = 1;").intoString(
        "EntryPoint.b || (EntryPoint.i = 1);");
    optimize("void", "if (!b) i = 1; else i = 2;").intoString(
        "EntryPoint.b ? (EntryPoint.i = 2) : (EntryPoint.i = 1);");
    optimize("int", "if (!b) { return 1;} else {return 2;}").into(
        "return b ? 2 : 1;");
  }

  public void testIfStatementToBoolean_ReturnLifting() throws Exception {
    optimize("int", "if (b) return 1; return 2;").into(
        "if (b) return 1; return 2;");
    optimize("int", "if (b) { return 1; }  return 2;").into(
        "if (b) { return 1; } return 2;");
    optimize("int", "if (b) { return 1;} else {return 2;}").into(
        "return b ? 1 : 2;");
    optimize("int", "if (b) return 1; else {return 2;}").into(
        "return b ? 1 : 2;");
    optimize("int", "if (b) return 1; else return 2;").into("return b ? 1 : 2;");
    optimize("void", "if (b) return; else return;").into(
        "if (b) return; else return;");
  }

  public void testIfStatementToBoolean_ThenElseOptimization() throws Exception {
    optimize("void", "if (b) i = 1; else i = 2;").intoString(
        "EntryPoint.b ? (EntryPoint.i = 1) : (EntryPoint.i = 2);");
    optimize("void", "if (b) {i = 1;} else {i = 2;}").intoString(
        "EntryPoint.b ? (EntryPoint.i = 1) : (EntryPoint.i = 2);");
  }

  public void testIfStatementToBoolean_ThenOptimization() throws Exception {
    optimize("void", "if (b) i = 1;").intoString(
        "EntryPoint.b && (EntryPoint.i = 1);");
    optimize("void", "if (b) {i = 1;}").intoString(
        "EntryPoint.b && (EntryPoint.i = 1);");
  }

  public void testDoOptimization() throws Exception {
    optimize("void", "do {} while (b);").intoString(
        "do {", 
        "} while (EntryPoint.b);");
    optimize("void", "do {} while (true);").intoString(
        "do {",
        "} while (true);");
    optimize("void", "do {} while (false);").intoString("");
    optimize("void", "do { i++; } while (false);").intoString("++EntryPoint.i;");
    optimize("void", "do { break; } while (false);").intoString(
        "do {",
        "  break;",
        "} while (false);");
  }

  public void testSubtractFromZero() throws Exception {
    optimize("int", "return 0 - i;").intoString("return -EntryPoint.i;");
    optimize("long", "return 0 - l;").intoString("return -EntryPoint.l;");
    // Verify that float/double subtracts from zero aren't replaced, since they
    // are needed for obscure IEEE754 functionality -- specifically, converting
    // 0.0 - v into -v means the sign of the result is the opposite of the input
    // rathe than always being positive.
    optimize("float", "return 0.0F - f;").intoString("return 0.0f - EntryPoint.f;");
    optimize("double", "return 0.0 - d;").intoString("return 0.0 - EntryPoint.d;");
  }

  @Override
  protected boolean optimizeMethod(JProgram program, JMethod method) {
    OptimizerStats result = DeadCodeElimination.exec(program, method);
    if (result.didChange()) {
      // Make sure we converge in one pass.
      assertFalse(DeadCodeElimination.exec(program, method).didChange());
    }
    return result.didChange();
  }
}
