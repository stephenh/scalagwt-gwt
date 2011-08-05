package com.google.gwt.dev.scalac;

import java.util.Collection;

public interface ScalaCompiler {

  void addClassBytes(String internalName, byte[] classBytes);

  void addJavaSource(String internalName, String content);

  void addScalaSource(String internalName, String content);

  Collection<ScalacUnitResult> compile();

}
