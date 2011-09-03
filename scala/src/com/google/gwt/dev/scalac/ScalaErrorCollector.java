package com.google.gwt.dev.scalac;

import java.util.HashMap;
import java.util.Map;

import scala.tools.nsc.reporters.Reporter;
import scala.tools.nsc.util.Position;

public class ScalaErrorCollector extends Reporter {

  // TODO(stephenh) Put back into each unit's CompilationUnitBuilders
  // TODO(stephenh) Should be keyed by internal name
  private final Map<String, String> m = new HashMap<String, String>();

  @Override
  public void info0(Position position, String message, Severity severity, boolean force) {
    String path;
    try {
      path = position.source().path();
    } catch (UnsupportedOperationException uoe) {
      path = "Unknown";
    }
    System.out.println("scalac error " + path + ": " + message);
    m.put(path, message);
  }

}
