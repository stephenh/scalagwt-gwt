package com.google.gwt.dev.scalac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.javac.CompilationUnit;
import com.google.gwt.dev.javac.CompilationUnitBuilder;
import com.google.gwt.dev.javac.CompiledClass;
import com.google.gwt.dev.javac.Dependencies;
import com.google.gwt.dev.javac.ExtraCompiler;
import com.google.gwt.dev.javac.JribbleParser;
import com.google.gwt.dev.javac.JsniMethod;
import com.google.gwt.dev.javac.MethodArgNamesLookup;
import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.impl.JribbleAstBuilder;
import com.google.gwt.dev.util.Name.BinaryName;
import com.google.gwt.dev.util.Name.InternalName;
import com.google.gwt.thirdparty.guava.common.collect.Lists;

public class ScalaGwtCompiler implements ExtraCompiler {

  private final JribbleAstBuilder jribbleAstBuilder = new JribbleAstBuilder();

  /**
   * Remove scala units from {@code builders} and fill in its {@CompilationUnitBuilder} ourselves.
   * 
   * Keeps scala CompilationUnitBuilders from getting to the JDT, but still puts them onto the
   * buildQueue for serialization.
   */
  @Override
  public Collection<CompilationUnitBuilder> stealUnits(TreeLogger logger, Collection<CompilationUnitBuilder> builders, Collection<CompilationUnit> cachedUnits) {
    Collection<CompilationUnitBuilder> scalaBuilders = extractScalaUnits(builders);
    if (scalaBuilders.isEmpty()) {
      return scalaBuilders;
    }

    ScalaCompiler scalac = new NscScalaCompiler();

    for (CompilationUnit cachedUnit : cachedUnits) {
      for (CompiledClass cc : cachedUnit.getCompiledClasses()) {
        scalac.addClassBytes(cc.getInternalName(), cc.getBytes());
      }
    }
    for (CompilationUnitBuilder javaUnit : builders) {
      scalac.addJavaSource(BinaryName.toInternalName(javaUnit.getTypeName()), javaUnit.getSource());
    }
    for (CompilationUnitBuilder scalaUnit: scalaBuilders) {
      scalac.addScalaSource(BinaryName.toInternalName(scalaUnit.getTypeName()), scalaUnit.getSource());
    }

    // ideally these would not be generated at all
    List<String> primitives = new ArrayList<String>();
    primitives.add("scala/Boolean");
    primitives.add("scala/Byte");
    primitives.add("scala/Char");
    primitives.add("scala/Double");
    primitives.add("scala/Float");
    primitives.add("scala/Int");
    primitives.add("scala/Long");
    primitives.add("scala/Short");
    primitives.add("scala/Unit");

    Map<String, CompilationUnitBuilder> buildersByName = mapByInternalName(scalaBuilders);
    Collection<CompilationUnitBuilder> successfulBuilders = new ArrayList<CompilationUnitBuilder>();
    for (ScalacUnitResult unit : scalac.compile()) {
      // get the builder back
      CompilationUnitBuilder cub = buildersByName.get(unit.internalName);
      if (cub == null) {
        System.out.println("No builder for " + unit.internalName);
        continue;
      }
      if (primitives.contains(unit.internalName)) {
        System.out.println("SKipping " + unit.internalName);
        continue;
      }
      List<String> apiRefs = new ArrayList<String>();
      MethodArgNamesLookup methodArgNames = new MethodArgNamesLookup();
      List<CompiledClass> ccs = new ArrayList<CompiledClass>();
      List<JDeclaredType> asts = new ArrayList<JDeclaredType>();
      for (ScalacClassResult cr : unit.classes) {
        CompiledClass cc = new CompiledClass(cr.byteCode, null, false, cr.internalName);
        //TODO(grek): This try...catch is a workaround for following issue: https://github.com/scalagwt/scalagwt-scala/issues/14
        try {
          JribbleAstBuilder.Result r = jribbleAstBuilder.process(JribbleParser.parse(cr.internalName, cr.jribble));
          ccs.add(cc);
          asts.addAll(r.types);
          apiRefs.addAll(r.apiRefs);
          methodArgNames.mergeFrom(r.methodArgNames);
        } catch (Exception e) {
          System.out.println("ERROR: " + e.getMessage());
          e.printStackTrace();
        } catch (AssertionError ae) {
          System.out.println("ERROR: " + ae.getMessage());
          ae.printStackTrace();
        }
      }
      cub.setTypes(asts);
      cub.setDependencies(Dependencies.buildFromApiRefs(InternalName.getPackageName(unit.internalName), Lists.newArrayList(apiRefs)));
      cub.setMethodArgs(methodArgNames);
      cub.setClasses(ccs);
      cub.setJsniMethods(new ArrayList<JsniMethod>());
      successfulBuilders.add(cub);
    }
    return successfulBuilders;
  }

  private static Map<String, CompilationUnitBuilder> mapByInternalName(Collection<CompilationUnitBuilder> builders) {
    Map<String, CompilationUnitBuilder> m = new HashMap<String, CompilationUnitBuilder>();
    for (CompilationUnitBuilder builder : builders) {
      m.put(BinaryName.toInternalName(builder.getTypeName()), builder);
    }
    return m;
  }

  private static Collection<CompilationUnitBuilder> extractScalaUnits(Collection<CompilationUnitBuilder> builders) {
    Collection<CompilationUnitBuilder> scalaBuilders = new ArrayList<CompilationUnitBuilder>();
    for (Iterator<CompilationUnitBuilder> i = builders.iterator(); i.hasNext(); ) {
      CompilationUnitBuilder cub = i.next();
      if (cub.getLocation().endsWith(".scala")) {
        scalaBuilders.add(cub);
        i.remove();
      }
    }
    return scalaBuilders;
  }

}
