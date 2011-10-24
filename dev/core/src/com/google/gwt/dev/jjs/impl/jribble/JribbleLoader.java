package com.google.gwt.dev.jjs.impl.jribble;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.google.gwt.dev.javac.CompilationUnitBuilder;
import com.google.gwt.dev.javac.CompiledClass;
import com.google.gwt.dev.javac.Dependencies;
import com.google.gwt.dev.javac.JsniMethod;
import com.google.gwt.dev.jjs.InternalCompilerException;
import com.google.gwt.dev.protobuf.CodedInputStream;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.collect.HashMap;

/**
 * Loads Jribble classes into {@link CompilationUnitBuilder}s. It gets the ASTs
 * via protobufs saved in {@link Resource}s, and it gets TypeOracle information
 * by reading bytecode from a class loader.
 */
public class JribbleLoader {
  private final AbstractQueue<CompilationUnitBuilder> buildQueue;
  private final ClassLoader classLoader;

  public JribbleLoader(ClassLoader classLoader,
      AbstractQueue<CompilationUnitBuilder> buildQueue) {
    this.classLoader = classLoader;
    this.buildQueue = buildQueue;
  }

  /**
   * Load the class files and ASTs for the given builders. Submits the builders
   * to the build queue as they are completed.
   */
  public void load(Map<String, CompiledClass> allValidClasses, Collection<? extends CompilationUnitBuilder> builders) {
    loadCompiledClasses(allValidClasses, builders);
    loadAsts(builders);
  }

  private void loadAsts(Collection<? extends CompilationUnitBuilder> builders) {
    for (CompilationUnitBuilder builder : builders) {
      String typeName = builder.getTypeName();
      String myPackage = "";
      if (typeName.contains(".")) {
        myPackage = typeName.substring(0, typeName.lastIndexOf('.'));
      }

      JribbleProtos.DeclaredType proto;
      try {
        InputStream source = builder.readSourceBinary();
        CodedInputStream codedSource = CodedInputStream.newInstance(source);
        codedSource.setRecursionLimit(400);
        proto = JribbleProtos.DeclaredType.parseFrom(codedSource);
        source.close();
      } catch (IOException e) {
        throw new InternalCompilerException("Error loading Jribble for "
            + builder.getTypeName(), e);
      }
      JribbleAstBuilder.Result result = new JribbleAstBuilder().process(proto);

      builder.setTypes(result.types);
      builder.setDependencies(Dependencies.buildFromApiRefs(myPackage,
          new ArrayList<String>(result.apiRefs)));
      builder.setMethodArgs(result.methodArgNames);
      builder.setJsniMethods(Collections.<JsniMethod>emptyList());

      buildQueue.add(builder);
    }
  }

  private void loadCompiledClasses(Map<String, CompiledClass> allValidClasses, Collection<? extends CompilationUnitBuilder> builders) {
    Map<String, CompiledClass> compiledClasses = new HashMap<String, CompiledClass>();
    Map<String, String> enclosingClasses = new HashMap<String, String>();

    for (CompilationUnitBuilder builder : builders) {
      String typeName = builder.getTypeName();
      String internalName = typeName.replace('.', '/');

      byte[] classBytes = Util.readURLAsBytes(classLoader
          .getResource(internalName + ".class"));

      // Based on the comments, it's not clear that tracking whether
      // a class is "local" is important. Set it to false for now.
      CompiledClass compiledClass = new CompiledClass(classBytes, false,
          internalName, builder.getLastModified());
      compiledClasses.put(internalName, compiledClass);
      builder.setClasses(Collections.singletonList(compiledClass));

      if (compiledClass.getTypeData().getOuterClass() != null) {
        enclosingClasses.put(internalName, compiledClass.getTypeData()
            .getOuterClass());
      }

      if (internalName.contains("colon$colon")) {
    	System.out.println("Putting " + internalName + " in as " + compiledClass.getSourceName());
      }
      allValidClasses.put(compiledClass.getSourceName(), compiledClass);
    }

    // Patch up enclosing classes
    for (String typeName : enclosingClasses.keySet()) {
      CompiledClass inner = compiledClasses.get(typeName);
      CompiledClass outer = compiledClasses.get(enclosingClasses.get(typeName));
      if (outer == null) {
        throw new InternalCompilerException(
            "Outer class could not be found! (Inner class: " + typeName
                + "; outer class: " + enclosingClasses.get(typeName));
      }
      inner.setEnclosingClass(outer);
    }
  }
}