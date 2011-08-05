package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.TreeLogger;

import java.util.Collection;

public interface ExtraCompiler {

  Collection<CompilationUnitBuilder> stealUnits(TreeLogger logger,
      Collection<CompilationUnitBuilder> builders, Collection<CompilationUnit> cachedUnits);

}
