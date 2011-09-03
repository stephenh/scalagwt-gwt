package com.google.gwt.dev.javac;

import com.google.jribble.DefParser;
import com.google.jribble.DefParserForJava;
import com.google.jribble.ast.DeclaredType;

import java.io.StringReader;

import scala.Either;

/**
 * Parses jribble ASTs from their on-disk format.
 *
 * TODO(stephenh) Either kill (we should in process anyway) or use protobuffers.
 * TODO(stephenh) Move next to other jribble stuff
 */
public class JribbleParser {

  private static final DefParser parser = new DefParserForJava();

  public static DeclaredType parse(String typeName, String source) {
    Either<DeclaredType, String> result = parser.parse(new StringReader(source), typeName);
    if (result.isRight()) {
      throw new RuntimeException(String.format(
        "Failed to parse %1s, parsing failed with a message:\n%2s",
        typeName,
        result.right().get()));
    } else {
      return result.left().get();
    }
  }

}
