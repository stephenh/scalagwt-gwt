package com.google.gwt.dev.scalac;

/** Holds the bytecode + AST for a jribble class. */
public class JribbleClassResult {

  public final String internalName;
  public final byte[] byteCode;
  public final String jribble;

  public JribbleClassResult(String internalName, byte[] byteCode, String jribble) {
    this.internalName = internalName;
    this.byteCode = byteCode;
    this.jribble = jribble;
  }

}
