package com.google.gwt.dev.scalac;

import java.util.Collection;

/** Provides a simple Scala compiler API for Scala+GWT to depend on */
public interface ScalaCompiler {

  /** Adds already-compiled/cached bytecode to the compilation classpath. */
  void addClassBytes(String internalName, byte[] classBytes);

  /** Adds a changed java source file to the compilation source path. */
  void addJavaSource(String internalName, String content);

  /** Adds a changed scala source file to the compilation source path. */
  void addScalaSource(String internalName, String content);

  /** Invokes the compiler and returns the new units. */
  Collection<ScalacUnitResult> compile();

}
