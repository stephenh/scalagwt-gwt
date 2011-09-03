package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.TreeLogger;

import java.util.Collection;

/**
 * Provides a hook for non-Java languages to compile their compilation units.
 * 
 * GWT calls {@link #stealUnits(TreeLogger, Collection, Collection)} with all Java + non-Java
 * changes units before passing the units to the usual {@link JdtCompiler}.
 * 
 * This gives implementations of this method a chance to remove their units for the changed
 * collection, compile them, and return the resulting bytecode + ASTs.
 */
public interface ExtraCompiler {

  /**
   * Compiles any non-Java compilation units in {@code builders}.
   * 
   * @param logger the logger
   * @param builders all Java + non-Java builders, this method should find and move the non-Java
   *          builders
   * @param cachedUnits all Java + non-Java cached units, this forms the client-side classpath that
   *          should be used for compiling any client-sode code
   */
  Collection<CompilationUnitBuilder> stealUnits(TreeLogger logger,
      Collection<CompilationUnitBuilder> builders, Collection<CompilationUnit> cachedUnits);

}
