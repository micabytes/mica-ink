package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class InkParser {
  @NonNls private static final String UTF_8 = "UTF-8";
  @NonNls private static final char WHITESPACE = ' ';
  @NonNls static final char HEADER = '=';
  @NonNls static final char GATHER_DASH = '-';
  @NonNls static final char CHOICE_DOT = '*';
  @NonNls static final char CHOICE_PLUS = '+';
  @NonNls static final char DOT = '.';
  @NonNls public static final String DEFAULT_KNOT_NAME = "BEGIN";
  @NonNls public static final String DEFAULT_STITCH_NAME = "DEFAULT";

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
            if (isEmptyKnotParent(current))
              current = createDefaultStitch(lineNumber, current, story);
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
          default:
            break;
        }
        if (!parsed && !trimmedLine.isEmpty()) {
          Content text = new Content(lineNumber, trimmedLine);
          if (current == null) {
            current = new Knot(lineNumber, DEFAULT_KNOT_NAME);
            story.add(current);
          }
          if (isEmptyKnotParent(current))
            current = createDefaultStitch(lineNumber, current, story);
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

  private static boolean isEmptyKnotParent(Container current) throws InkParseException {
    if (current == null)
      throw new InkParseException("Missing parent for stitch/choice/text");
    return current.type == ContentType.KNOT && current.getContentSize() == 0;
  }

  private static Stitch createDefaultStitch(int lineNumber, @Nullable Container current, Story story) throws InkParseException {
    if (current == null)
      throw new InkParseException("Missing parent for content in line number " + lineNumber);
    Stitch stitch = new Stitch(lineNumber, current.id + DOT + DEFAULT_STITCH_NAME, current);
    story.add(stitch);
    return stitch;
  }

}
