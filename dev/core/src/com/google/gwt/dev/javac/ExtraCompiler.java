package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.TreeLogger;

import java.util.Collection;

/** Provides a hook for non-Java compilers to compile their compilation units. */
public interface ExtraCompiler {

  /**
   * Compiles any non-Java compilation units in {@code builders}.
   * 
   * @param logger the logger
   * @param builders all Java + non-Java builders, this method should find and move the non-Java
   *          builders
   * @param cachedUnits all Java + non-Java cached units
   */
  Collection<CompilationUnitBuilder> stealUnits(TreeLogger logger,
      Collection<CompilationUnitBuilder> builders, Collection<CompilationUnit> cachedUnits);

}
