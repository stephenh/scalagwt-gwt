package com.google.gwt.dev.scalac;

public class ScalacClassResult {

  public final String internalName;
  public final byte[] byteCode;
  public final String jribble;

  public ScalacClassResult(String internalName, byte[] byteCode, String jribble) {
    this.internalName = internalName;
    this.byteCode = byteCode;
    this.jribble = jribble;
  }

}
