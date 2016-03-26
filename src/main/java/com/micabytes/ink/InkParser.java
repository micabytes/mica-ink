package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings("UtilityClass")
public final class InkParser {
  @NonNls private static final String UTF_8 = "UTF-8";
  @NonNls private static final char WHITESPACE = ' ';
  @NonNls static final char HEADER = '=';
  @NonNls static final char GATHER_DASH = '-';
  @NonNls static final char CHOICE_DOT = '*';
  @NonNls static final char CHOICE_PLUS = '+';
  @NonNls private static final char VAR_DECL = 'V';
  @NonNls private static final char VAR_STAT = '~';
  @NonNls static final char DOT = '.';
  @NonNls public static final String DEFAULT_KNOT_NAME = "default";

  private InkParser() {
    // NOOP
  }

  public static Story parse(InputStream inputStream) throws InkParseException {
    InputStreamReader inputStreamReader = null;
    BufferedReader bufferedReader = null;
    Story story = new Story();
    try {
      inputStreamReader = new InputStreamReader(inputStream, UTF_8);
      bufferedReader = new BufferedReader(inputStreamReader);
      String line = bufferedReader.readLine();
      int lineNumber = 1;
      @Nullable Container current = null;
      while (line != null) {
        String trimmedLine = line.trim();
        char firstChar = trimmedLine.isEmpty() ? WHITESPACE : trimmedLine.charAt(0);
        boolean parsed = false;
        switch (firstChar) {
          case HEADER:
            if (Knot.isKnotHeader(trimmedLine)) {
              current = new Knot(lineNumber, trimmedLine);
              story.add(current);
              parsed = true;
            } else if (Stitch.isStitchHeader(trimmedLine)) {
              current = new Stitch(lineNumber, trimmedLine, current);
              story.add(current);
              parsed = true;
            }
            break;
          case CHOICE_DOT:
          case CHOICE_PLUS:
            if (Choice.isChoiceHeader(trimmedLine)) {
              current = new Choice(lineNumber, trimmedLine, current);
              story.add(current);
              parsed = true;
            }
            break;
          case GATHER_DASH:
            if (Gather.isGatherHeader(trimmedLine)) {
              current = new Gather(lineNumber, trimmedLine, current);
              story.add(current);
              parsed = true;
            }
            break;
          case VAR_DECL:
          case VAR_STAT:
            if (Variable.isVariableHeader(trimmedLine)) {
              if (current == null) {
                current = new Knot(lineNumber, DEFAULT_KNOT_NAME);
                story.add(current);
              }
              current.add(new Variable(lineNumber, trimmedLine));
              parsed = true;
            }
          default:
            break;
        }
        if (!parsed && !trimmedLine.isEmpty()) {
          Content text = new Content(lineNumber, trimmedLine);
          if (current == null) {
            current = new Knot(lineNumber, DEFAULT_KNOT_NAME);
            story.add(current);
          }
          current.add(text);
        }
        line = bufferedReader.readLine();
        lineNumber++;
      }
      story.initialize();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (inputStreamReader != null)
          inputStreamReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        if (bufferedReader != null)
          bufferedReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return story;
  }

}
