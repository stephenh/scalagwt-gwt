package com.google.gwt.dev.jribble;

import java.util.Collection;

/**
 * Provides an API for {@code JribbleExtraCompiler} to other, language-specific compilers.
 */
public interface JribbleCompiler {

  /** Adds already-compiled/cached bytecode to the compilation classpath. */
  void addClassBytes(String internalName, byte[] classBytes);

  /** Adds a changed java source file to the compilation source path. */
  void addJavaSource(String internalName, String content);

  /**
   * Adds a changed source file to the compilation source path.
   * 
   * Note that this will be the original source language file, e.g. {@code foo.scala} and not the
   * AST version {@code foo.jribble}.
   */
  void addSource(String internalName, String content);

  /** Invokes the compiler and returns the new units. */
  Collection<JribbleUnitResult> compile();

}
