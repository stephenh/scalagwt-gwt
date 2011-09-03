package com.google.gwt.dev.scalac;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

import java.util.HashMap;
import java.util.Map;

import scala.tools.nsc.reporters.Reporter;
import scala.tools.nsc.util.Position;

public class ScalaErrorCollector extends Reporter  {

  // TODO(stephenh) Put back into each unit's CompilationUnitBuilders
  private final Map<String, CategorizedProblem> m = new HashMap<String, CategorizedProblem>();

  @Override
  public void info0(Position position, String message, Severity severity, boolean force) {
    String path;
    try {
      path = position.source().path();
    } catch (UnsupportedOperationException uoe) {
      path = "Unknown";
    }
    int line;
    try {
      line = position.line();
    } catch (UnsupportedOperationException uoe) {
      line = -1;
    }
    System.out.println("scalac error " + path + ": " + message);
    m.put(path, new DefaultProblem(
        path.toCharArray(),
        message,
        IProblem.ExternalProblemNotFixable,
        null,
        severity.id(),
        0,
        0,
        line,
        0));
  }

}

