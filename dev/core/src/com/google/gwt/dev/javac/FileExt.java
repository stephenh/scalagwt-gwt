package com.google.gwt.dev.javac;

/** Central place for decisions based on file extensions. */
public class FileExt {

  public static boolean isSource(String path) {
    return path.endsWith(".java") || path.endsWith(".scala");
  }

  public static boolean isSource(char[] path) {
    return (!endsWith(path, ".java") && !endsWith(path, ".scala"));
  }

  public static boolean isForJribble(String path) {
    return path.endsWith(".scala");
  }

  // char-specific implementation of endsWith
  private static boolean endsWith(char[] fileName, String extension) {
    int i = fileName.length - 1;
    int j = extension.length() - 1;
    if (i < j) {
      return false;
    }
    for (; j >= 0; j--, i--) {
      if (fileName[i] != extension.charAt(j)) {
        return false;
      }
    }
    return true;
  }

}
