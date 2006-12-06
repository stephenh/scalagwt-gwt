// Copyright 2006 Google Inc. All Rights Reserved.
package com.google.gwt.dev.util.xml;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandlerClassInfo {
  private static final HandlerMethod[] EMPTY_ARRAY_HANDLERMETHOD = new HandlerMethod[0];
  private static Map sClassInfoMap = new HashMap();

  public static synchronized void registerClass(Class c) {
    if (sClassInfoMap.containsKey(c))
      return;

    // Put a guard null in so that recursive registration of the same
    // class won't die.
    //
    sClassInfoMap.put(c, null);
    HandlerClassInfo classInfo = createClassInfo(c);
    sClassInfoMap.put(c, classInfo);
  }

  public static synchronized HandlerClassInfo getClassInfo(Class c) {
    if (sClassInfoMap.containsKey(c))
      return (HandlerClassInfo) sClassInfoMap.get(c);
    else
      throw new RuntimeException("The schema class '" + c.getName()
        + "' should have been registered prior to parsing");
  }

  private static HandlerClassInfo createClassInfo(Class c) {
    Map namedHandlerMethods = new HashMap();
    try {
      loadClassInfoRecursive(namedHandlerMethods, c);
    } catch (Exception e) {
      throw new RuntimeException("Unable to use class '" + c.getName()
        + "' as a handler", e);
    }
    HandlerClassInfo classInfo = new HandlerClassInfo(namedHandlerMethods);
    return classInfo;
  }

  private static void loadClassInfoRecursive(Map namedHandlerMethods, Class c) {
    if (!Schema.class.isAssignableFrom(c)) {
      // Have gone up as far as we can go.
      //
      return;
    }

    Method[] methods = c.getDeclaredMethods();
    for (int i = 0, n = methods.length; i < n; ++i) {
      Method method = methods[i];
      HandlerMethod handlerMethod = HandlerMethod.tryCreate(method);
      if (handlerMethod != null) {
        // Put in the map, but only if that method isn't already there.
        // (Allows inheritance where most-derived class wins).
        //
        String name = method.getName();
        if (!namedHandlerMethods.containsKey(name))
          namedHandlerMethods.put(name, handlerMethod);
      }
    }

    // Recurse into superclass.
    //
    Class superclass = c.getSuperclass();
    if (superclass != null)
      loadClassInfoRecursive(namedHandlerMethods, superclass);
  }

  // Nobody else can create one.
  private HandlerClassInfo(Map namedHandlerMethods) {
    fNamedHandlerMethods = namedHandlerMethods;
  }

  public HandlerMethod getStartMethod(String localName) {
    String methodName = "__" + localName.replace('-', '_');
    return (HandlerMethod) fNamedHandlerMethods.get(methodName + "_begin");
  }

  public HandlerMethod getEndMethod(String localName) {
    String methodName = "__" + localName.replace('-', '_');
    return (HandlerMethod) fNamedHandlerMethods.get(methodName + "_end");
  }

  private final Map fNamedHandlerMethods;

  public HandlerMethod[] getHandlerMethods() {
    return (HandlerMethod[]) fNamedHandlerMethods.values().toArray(
      EMPTY_ARRAY_HANDLERMETHOD);
  }

  public HandlerMethod getTextMethod() {
    return (HandlerMethod) fNamedHandlerMethods.get("__text");
  }
}
