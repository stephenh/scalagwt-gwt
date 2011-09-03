package com.google.gwt.dev.javac;

/** Decouples {@code gwt-dev} from implements of {@link ExtraCompiler}s. */
public class ExtraCompilerDiscovery {

  /** By default we look for the jribble compiler--it may/may not be on the classpath. */
  private static final String defaultImpl = "com.google.gwt.dev.scalac.JribbleExtraCompiler";
  /** Allow non-jribble compilers to be specified by a system property. */
  private static final String overrideKey = "extra.compiler.impl";

  public static ExtraCompiler createOrNull() {
    String className = System.getProperty(overrideKey, defaultImpl);
    try {
      return (ExtraCompiler) Class.forName(className).newInstance();
    } catch (InstantiationException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

}
