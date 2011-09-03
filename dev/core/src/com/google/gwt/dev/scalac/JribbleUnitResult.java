package com.google.gwt.dev.scalac;

import java.util.ArrayList;
import java.util.List;

/** A jribble compilation unit with it's classes and any compilation errors. */
public class JribbleUnitResult {

  public final String internalName;
  public final List<JribbleClassResult> classes = new ArrayList<JribbleClassResult>();
  public final List<String> errors = new ArrayList<String>();

  public JribbleUnitResult(String internalName) {
    this.internalName = internalName;
  }

}
