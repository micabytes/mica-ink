package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

@SuppressWarnings("UtilityClass")
public final class InkParser {
  @NonNls private static final String UTF_8 = "UTF-8";
  @NonNls private static final char WHITESPACE = ' ';
  @NonNls static final char HEADER = '=';
  @NonNls private static final char GATHER_DASH = '-';
  @NonNls static final char CHOICE_DOT = '*';
  @NonNls static final char CHOICE_PLUS = '+';
  @NonNls private static final char VAR_DECL = 'V';
  @NonNls private static final char VAR_STAT = '~';
  @NonNls private static final char CONDITIONAL_HEADER = '{';
  @NonNls static final String CONDITIONAL_END = "}";
  @NonNls public static final char DOT = '.';
  @NonNls private static final String DEFAULT_KNOT_NAME = "default";
  private static final Pattern AT_SPLITTER = Pattern.compile("[@]");
  @NonNls private static final String INCLUDE = "INCLUDE";
  @NonNls private static final String IMG = "img(";

  private InkParser() {
    // NOOP
  }

  public static Story parse(String fileName, StoryWrapper provider) throws InkParseException {
    InputStream in = provider.getStream(fileName);
    Story story = parse(in, provider);
    story.fileName = fileName;
    return story;
  }

  @SuppressWarnings("OverlyComplexMethod")
  public static Story parse(InputStream inputStream, StoryWrapper provider) throws InkParseException {
    if (provider == null)
      throw new InkParseException("The StoryWrapper passed to Ink is null.");
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    Story story = new Story(provider);
    try {
      inputStreamReader = new InputStreamReader(inputStream, UTF_8);
      bufferedReader = new BufferedReader(inputStreamReader);
      String line = bufferedReader.readLine();
      int lineNumber = 1;
      Container current = new Knot(lineNumber, "=== " + DEFAULT_KNOT_NAME);
      story.add(current);
      @Nullable Conditional conditional = null;
      while (line != null) {
        String trimmedLine = line.trim();
        if (trimmedLine.contains("//")) {
          String comment = trimmedLine.substring(trimmedLine.indexOf("//")).trim();
          parseComment(lineNumber, comment, current);
          trimmedLine = trimmedLine.substring(0, trimmedLine.indexOf("//")).trim();
        }
        if (trimmedLine.startsWith(INCLUDE)) {
          String file = trimmedLine.replace(INCLUDE, "").trim();
          Story incl = parse(file, provider);
          story.addAll(incl);
        }
        else if (conditional != null) {
          Conditional cond = (Conditional) current.getContent(current.getContentSize()-1);
          cond.parseLine(lineNumber, trimmedLine);
          if (trimmedLine.endsWith(CONDITIONAL_END)) // This is a bug. Means conditions cannot have text line that ends with CBRACE_RIGHT
            conditional = null;
        }
        else {
          Content cont = parseLine(lineNumber, trimmedLine, current);
          if (cont != null) {
            if (cont.getId() == null)
              cont.generateId(current);
            story.add(cont);
            if (cont.isContainer()) {
              current = (Container) cont;
            }
          }
          if (cont != null && cont.isConditional()) {
            conditional = (Conditional) cont;
          }
        }
        line = bufferedReader.readLine();
        lineNumber++;
      }
      story.initialize();
    } catch (IOException e) {
      provider.logException(e);
    } finally {
      try {
        if (inputStreamReader != null)
          inputStreamReader.close();
      } catch (IOException e) {
        provider.logException(e);
      }
      try {
        if (bufferedReader != null)
          bufferedReader.close();
      } catch (IOException e) {
        provider.logException(e);
      }
    }
    return story;
  }

  @SuppressWarnings("OverlyComplexMethod")
  static Content parseLine(int lineNumber, String trimmedLine, Container current) throws InkParseException {
    char firstChar = trimmedLine.isEmpty() ? WHITESPACE : trimmedLine.charAt(0);
    switch (firstChar) {
      case HEADER:
        if (KnotFunction.isFunctionHeader(trimmedLine)) {
          return new KnotFunction(lineNumber, trimmedLine);
        }
        if (Knot.isKnotHeader(trimmedLine)) {
          return new Knot(lineNumber, trimmedLine);
        }
        if (Stitch.isStitchHeader(trimmedLine)) {
          return new Stitch(lineNumber, trimmedLine, current);
        }
        break;
      case CHOICE_DOT:
      case CHOICE_PLUS:
        if (Choice.isChoiceHeader(trimmedLine))
          return new Choice(lineNumber, trimmedLine, current);
        break;
      case GATHER_DASH:
        if (Gather.isGatherHeader(trimmedLine))
          return new Gather(lineNumber, trimmedLine, current);
        break;
      case VAR_DECL:
      case VAR_STAT:
        if (Variable.isVariableHeader(trimmedLine))
          return new Variable(lineNumber, trimmedLine, current);
        break;
      case CONDITIONAL_HEADER:
        if (Conditional.isConditionalHeader(trimmedLine))
          return new Conditional(lineNumber, trimmedLine, current);
        break;
      default:
        break;
    }
    if (!trimmedLine.isEmpty()) {
      return new Content(lineNumber, trimmedLine, current);
    }
    return null;
  }

  private static void parseComment(int lineNumber, String comment, Container current) {
    String[] token = AT_SPLITTER.split(comment);
    if (token.length < 2) return;
    for (int i=1; i<token.length; i++) {
      if (token[i].startsWith(IMG)) {
        String img = token[i].substring(token[i].indexOf(Symbol.BRACE_LEFT) + 1, token[i].indexOf(Symbol.BRACE_RIGHT)).trim();
        if (current != null) {
          current.setBackground(img);
        }
      }
      if (token[i].startsWith("*") || token[i].startsWith("+") ) {
        Content cont = new Comment(lineNumber, token[i]);
        if (current != null)
          current.add(cont);
      }
    }
  }

}
