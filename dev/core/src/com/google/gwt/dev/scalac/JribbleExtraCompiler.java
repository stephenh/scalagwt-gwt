package com.google.gwt.dev.scalac;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link ExtraCompiler} for non-Java languages that can provide jribble ASTs.
 * 
 * This implementation is not the real, language-specific compiler, but instead an adapter than
 * defers to the language-specific compiler to produce bytecode + jribble ASTs, and then this class
 * transforms the jribble ASTs into GWT-specific ASTs.
 * 
 * TODO(stephenh) Some how generalize the scala file extension
 */
public class JribbleExtraCompiler implements ExtraCompiler {

  private final JribbleAstBuilder jribbleAstBuilder = new JribbleAstBuilder();

  /**
   * Removes the non-Java units from {@code builders} and populates their corresponding
   * {@link CompilationUnitBuilder} with bytecode + ASTs ourselves.
   */
  @Override
  public Collection<CompilationUnitBuilder> stealUnits(TreeLogger logger,
      Collection<CompilationUnitBuilder> builders, Collection<CompilationUnit> cachedUnits) {
    Collection<CompilationUnitBuilder> ourBuilders = extractScalaUnits(builders);
    if (ourBuilders.isEmpty()) {
      return ourBuilders;
    }

    // TODO(stephenh) Need to decouple for other non-Java languages
    JribbleCompiler compiler = new ScalaNscJribbleCompiler();

    addCachedUnitsAsClasspath(compiler, cachedUnits);
    addJavaSources(compiler, builders);
    addSources(compiler, ourBuilders);

    Map<String, CompilationUnitBuilder> buildersByName = mapByInternalName(ourBuilders);
    Collection<CompilationUnitBuilder> successfulBuilders = new ArrayList<CompilationUnitBuilder>();
    for (JribbleUnitResult unit : compiler.compile()) {
      // the compiler returns units in random order, map it back to its builder
      CompilationUnitBuilder cub = buildersByName.get(unit.internalName);
      if (cub == null) {
        System.out.println("No builder for " + unit.internalName);
      } else {
        updateBuilder(cub, unit);
        successfulBuilders.add(cub);
      }
    }
    return successfulBuilders;
  }

  /** Puts the data from the jribble {@code unit} into the GWT {@code cub}. */
  private void updateBuilder(CompilationUnitBuilder cub, JribbleUnitResult unit) {
    List<String> apiRefs = new ArrayList<String>();
    MethodArgNamesLookup methodArgNames = new MethodArgNamesLookup();
    List<CompiledClass> ccs = new ArrayList<CompiledClass>();
    List<JDeclaredType> asts = new ArrayList<JDeclaredType>();
    for (JribbleClassResult cr : unit.classes) {
      // TODO(stephenh) Remove when RedBlack is fixed, otherwise TypeOracle blows up
      if (cr.internalName.contains("RedBlack")) {
        continue;
      }
      // TODO(grek): This try...catch is a workaround for following issue:
      // https://github.com/scalagwt/scalagwt-scala/issues/14
      try {
        CompiledClass cc = new CompiledClass(cr.byteCode, null, false, cr.internalName);
        ccs.add(cc);
        if (cr.jribble != null) {
          JribbleAstBuilder.Result r =
              jribbleAstBuilder.process(JribbleParser.parse(cr.internalName, cr.jribble));
          asts.addAll(r.types);
          apiRefs.addAll(r.apiRefs);
          methodArgNames.mergeFrom(r.methodArgNames);
        }
      } catch (Exception e) {
        System.out.println("ERROR: " + e.getMessage());
        e.printStackTrace();
      } catch (AssertionError ae) {
        System.out.println("ERROR: " + ae.getMessage());
        ae.printStackTrace();
      }
    }
    cub.setTypes(asts);
    cub.setDependencies(Dependencies.buildFromApiRefs(InternalName
        .getPackageName(unit.internalName), Lists.newArrayList(apiRefs)));
    cub.setMethodArgs(methodArgNames);
    cub.setClasses(ccs);
    cub.setJsniMethods(new ArrayList<JsniMethod>());
  }

  /** @return a map of {@code builders} keyed by their internal name */
  private static Map<String, CompilationUnitBuilder> mapByInternalName(
      Collection<CompilationUnitBuilder> builders) {
    Map<String, CompilationUnitBuilder> m = new HashMap<String, CompilationUnitBuilder>();
    for (CompilationUnitBuilder builder : builders) {
      m.put(BinaryName.toInternalName(builder.getTypeName()), builder);
    }
    return m;
  }

  /** @return a list of scala units, removed from {@code builders} */
  private static Collection<CompilationUnitBuilder> extractScalaUnits(
      Collection<CompilationUnitBuilder> builders) {
    Collection<CompilationUnitBuilder> scalaBuilders = new ArrayList<CompilationUnitBuilder>();
    for (Iterator<CompilationUnitBuilder> i = builders.iterator(); i.hasNext();) {
      CompilationUnitBuilder cub = i.next();
      if (cub.getLocation().endsWith(".scala")) {
        scalaBuilders.add(cub);
        i.remove();
      }
    }
    return scalaBuilders;
  }

  /** Adds {@code cachedUnits} to the compiler. */
  private static void addCachedUnitsAsClasspath(JribbleCompiler compiler,
      Collection<CompilationUnit> cachedUnits) {
    for (CompilationUnit cachedUnit : cachedUnits) {
      for (CompiledClass cc : cachedUnit.getCompiledClasses()) {
        compiler.addClassBytes(cc.getInternalName(), cc.getBytes());
      }
    }
  }

  /** Adds {@code javaBuilders} to the compiler. */
  private static void addJavaSources(JribbleCompiler compiler,
      Collection<CompilationUnitBuilder> javaBuilders) {
    for (CompilationUnitBuilder javaUnit : javaBuilders) {
      compiler.addJavaSource(BinaryName.toInternalName(javaUnit.getTypeName()), javaUnit
          .getSource());
    }
  }

  /** Adds the non-Java {@code ourBuilders} to the compiler. */
  private static void addSources(JribbleCompiler compiler,
      Collection<CompilationUnitBuilder> ourBuilders) {
    for (CompilationUnitBuilder scalaUnit : ourBuilders) {
      compiler.addSource(BinaryName.toInternalName(scalaUnit.getTypeName()), scalaUnit.getSource());
    }
  }

}
