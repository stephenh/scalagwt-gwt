package com.google.gwt.dev.scalac;

import com.google.gwt.dev.asm.ClassReader;
import com.google.gwt.dev.asm.commons.EmptyVisitor;

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
import scala.tools.nsc.Global.Run;
import scala.tools.nsc.Settings;

public class NscScalaCompiler implements ScalaCompiler {
  
  // TODO(stephenh) Think about binary-only annotations, ecj loads them on demand, do we need to?

  private static final Function1<String, BoxedUnit> error = new AbstractFunction1<String, BoxedUnit>() {
    @Override
    public BoxedUnit apply(String error) {
      System.err.println("scalac error: "+ error);
      return null;
    }
  };
  
  private final File tmp = new File("/tmp/gwt-scalac");
  private final File source = new File(tmp, "source");
  private final File target = new File(tmp, "target");
  private final File bin = new File(tmp, "bin");
  private final List<String> files = new ArrayList<String>();
  private boolean hasJavaCoreFiles = false;

  public NscScalaCompiler() {
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
  public void addScalaSource(String internalName, String content) {
    try {
      File f = new File(source, internalName + ".scala");
      files.add(f.getAbsolutePath());
      FileUtils.writeStringToFile(f, content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<ScalacUnitResult> compile() {
    try {
      return doCompile();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeNsc(List<String> args) {
    Settings ss = new Settings(error);
    ErrorCollector r = new ErrorCollector();
    CompilerCommand command = new CompilerCommand(JavaConversions.asScalaBuffer(args).toList(), ss);
    Global compiler = new Global(command.settings(), r);
    Global.Run run = compiler.new Run();
    run.compile(JavaConversions.asScalaBuffer(files).toList());
  }

  @SuppressWarnings("unchecked")
  private Collection<ScalacUnitResult> doCompile() throws Exception{
    if (hasJavaCoreFiles) {
      System.out.println("Skipping scala files until java.* is compiled--rerun compile");
      return new ArrayList<ScalacUnitResult>();
    }
    System.out.println("Compiling " + files.size() + " scala + java files");

    System.out.println("Compiling bytecode");
    invokeNsc(makeArgs("jvm-1.5"));

    System.out.println("Compiling jribble");
    invokeNsc(makeArgs("jribble"));


    Map<String, ScalacUnitResult> byUnitName = new HashMap<String, ScalacUnitResult>();
    String targetPath = target.getAbsolutePath();
    // find all .class files and group by the original .scala compilation unit
    for (File classFile : (List<File>) FileUtils.listFiles(target, new String[] { "class" }, true)) {
      String internalName = extractInternalName(targetPath, classFile);
      byte[] byteCode = FileUtils.readFileToByteArray(classFile);
      String unitName = extractUnitName(internalName, byteCode);
      ScalacUnitResult unit = byUnitName.get(unitName);
      if (unit == null) {
        unit = new ScalacUnitResult(unitName);
        byUnitName.put(unitName, unit);
      }
      String jribblePath = dropExtension(classFile.getPath()) + ".jribble";
      unit.classes.add(new ScalacClassResult(
          internalName,
          byteCode,
          FileUtils.readFileToString(new File(jribblePath))));
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
    commands.add("-Xplugin:/home/stephen/other/scalagwt-scala/build/quick/misc/scala-devel/plugins/factorymanifests.jar");
    // commands.add("-usejavacp"); // keep?
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
