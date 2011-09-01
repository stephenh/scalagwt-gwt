package com.google.gwt.dev.scalac;

import java.util.ArrayList;
import java.util.List;

/** A dto for compiled scala units. */
public class ScalacUnitResult {

  public final String internalName;
  public final List<ScalacClassResult> classes = new ArrayList<ScalacClassResult>();
  public final List<String> errors = new ArrayList<String>();

  public ScalacUnitResult(String internalName) {
    this.internalName = internalName;
  }

}
