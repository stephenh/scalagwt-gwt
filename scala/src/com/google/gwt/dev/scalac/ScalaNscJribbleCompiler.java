package com.google.gwt.dev.scalac;

import com.google.gwt.dev.asm.ClassReader;
import com.google.gwt.dev.asm.commons.EmptyVisitor;
import com.google.gwt.dev.jribble.JribbleClassResult;
import com.google.gwt.dev.jribble.JribbleCompiler;
import com.google.gwt.dev.jribble.JribbleUnitResult;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.Function1;
import scala.collection.JavaConversions;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;
import scala.tools.nsc.CompilerCommand;
import scala.tools.nsc.Global;
import scala.tools.nsc.Settings;

/** Implements the {@link JribbleCompiler} API by calling nsc in-process. */
public class ScalaNscJribbleCompiler implements JribbleCompiler {

  // TODO(stephenh) Think about binary-only annotations, ecj loads them on demand, do we need to?

  private static final Function1<String, BoxedUnit> error =
      new AbstractFunction1<String, BoxedUnit>() {
        @Override
        public BoxedUnit apply(String error) {
          System.err.println("scalac error: " + error);
          return null;
        }
      };
  private static final List<String> primitives = new ArrayList<String>();

  static {
    primitives.add("scala/Boolean");
    primitives.add("scala/Byte");
    primitives.add("scala/Char");
    primitives.add("scala/Double");
    primitives.add("scala/Float");
    primitives.add("scala/Int");
    primitives.add("scala/Long");
    primitives.add("scala/Short");
    primitives.add("scala/Unit");
  }

  // TODO(stephenh) Use java.io.tmpdir + per-instance tmpdir?
  private final File tmp = new File("/tmp/gwt-scalac");
  private final File source = new File(tmp, "source");
  private final File target = new File(tmp, "target");
  private final File bin = new File(tmp, "bin");
  private final List<String> files = new ArrayList<String>();
  private boolean hasJavaCoreFiles = false;

  public ScalaNscJribbleCompiler() {
    try {
      FileUtils.deleteDirectory(tmp);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    source.mkdirs();
    target.mkdirs();
    bin.mkdirs();
  }

  @Override
  public void addClassBytes(String internalName, byte[] classBytes) {
    try {
      FileUtils.writeByteArrayToFile(new File(bin, internalName + ".class"), classBytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addJavaSource(String internalName, String content) {
    try {
      if (internalName.startsWith("java/") && !hasJavaCoreFiles) {
        hasJavaCoreFiles = true;
      }
      File f = new File(source, internalName + ".java");
      files.add(f.getAbsolutePath());
      FileUtils.writeStringToFile(f, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addSource(String internalName, String content) {
    try {
      File f = new File(source, internalName + ".scala");
      files.add(f.getAbsolutePath());
      FileUtils.writeStringToFile(f, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<JribbleUnitResult> compile() {
    try {
      return doCompile();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeNsc(List<String> args) {
    Settings ss = new Settings(error);
    ScalaErrorCollector r = new ScalaErrorCollector();
    CompilerCommand command = new CompilerCommand(JavaConversions.asScalaBuffer(args).toList(), ss);
    Global compiler = new Global(command.settings(), r);
    Global.Run run = compiler.new Run();
    run.compile(JavaConversions.asScalaBuffer(files).toList());
  }

  @SuppressWarnings("unchecked")
  private Collection<JribbleUnitResult> doCompile() throws Exception {
    if (hasJavaCoreFiles) {
      System.out.println("Skipping scala files until java.* is compiled--rerun compile");
      return new ArrayList<JribbleUnitResult>();
    }
    System.out.println("Compiling " + files.size() + " scala + java files");

    System.out.println("Compiling bytecode");
    invokeNsc(makeArgs("jvm-1.5"));

    System.out.println("Compiling jribble");
    invokeNsc(makeArgs("jribble"));

    Map<String, JribbleUnitResult> byUnitName = new HashMap<String, JribbleUnitResult>();
    String targetPath = target.getAbsolutePath();
    // find all .class files and group by the original .scala compilation unit
    for (File classFile : (List<File>) FileUtils.listFiles(target, new String[] {"class"}, true)) {
      String internalName = extractInternalName(targetPath, classFile);
      byte[] byteCode = FileUtils.readFileToByteArray(classFile);
      String unitName = extractUnitName(internalName, byteCode);
      JribbleUnitResult unit = byUnitName.get(unitName);
      if (unit == null) {
        unit = new JribbleUnitResult(unitName);
        byUnitName.put(unitName, unit);
      }
      String jribble = null;
      // the primitive ASTs are broken and empty anyway
      if (!primitives.contains(unit.internalName)) {
        String jribblePath = dropExtension(classFile.getPath()) + ".jribble";
        jribble = FileUtils.readFileToString(new File(jribblePath));
      }
      unit.classes.add(new JribbleClassResult(internalName, byteCode, jribble));
    }
    System.out.println("Compiled scala units " + byUnitName.size());
    return byUnitName.values();
  }

  private List<String> makeArgs(String backend) {
    List<String> commands = new ArrayList<String>();
    // commands.add("-Ydebug");
    // commands.add("-Ylog-classpath");
    commands.add("-g:notailcalls");
    commands.add("-sourcepath");
    commands.add(source.getAbsolutePath());
    commands.add("-d");
    commands.add(target.getAbsolutePath());
    commands.add("-target:" + backend);
    commands.add("-cp");
    commands.add(bin.getAbsolutePath());
    commands.add("-javabootclasspath");
    commands.add(bin.getAbsolutePath());
    commands.add("-bootclasspath");
    commands.add(bin.getAbsolutePath());
    commands.add("-Xplugin-classes:scala.tools.factorymanifests.FactoryManifestsPlugin");
    return commands;
  }

  /** @return {@code foo/Foo$Bar} for {@code /parentPath/foo/Foo$Bar.class} */
  private static String extractInternalName(String parentPath, File file) {
    String path = file.getAbsolutePath();
    return dropExtension(path.substring(parentPath.length() + 1));
  }

  /** @return {@code foo/Foo} for {@code foo/Foo$Bar} */
  private static String extractUnitName(String internalName, byte[] byteCode) {
    // glean the source file from the bytecode
    ClassReader reader = new ClassReader(byteCode);
    SourceFileVisitor v = new SourceFileVisitor();
    reader.accept(v, 0);
    assert v.sourceFile != null : "source file not available";
    int lastSlash = internalName.lastIndexOf('/');
    String packagePrefix = lastSlash == -1 ? "" : internalName.substring(0, lastSlash + 1);
    return packagePrefix + dropExtension(v.sourceFile);
  }

  private static String dropExtension(String s) {
    int lastDot = s.lastIndexOf('.');
    return s.substring(0, lastDot);
  }

  private static class SourceFileVisitor extends EmptyVisitor {
    private String sourceFile;

    public void visitSource(final String sourceFile, final String debug) {
      this.sourceFile = sourceFile;
    }
  }

}
