package com.micabytes.ink;

import java.util.Random;

public class Content {
  static final String CBRACE_RIGHT = "}";
  static final String CBRACE_LEFT = "{";
  static final String SBRACE_LEFT = "[";
  static final String SBRACE_RIGHT = "]";

  int lineNumber;
  ContentType type = ContentType.TEXT;
  String text;
  int count;

  public Content() {
    // NOOP
  }

  public Content(int l, String str) {
    lineNumber = l;
    text = str;
  }

  public String getText(Story story) {
    String ret = text;
    while (ret.contains(CBRACE_LEFT)) {
      int start = ret.lastIndexOf(CBRACE_LEFT);
      int end = ret.indexOf(CBRACE_RIGHT, start);
      String s = ret.substring(start, end + 1);
      String res = evaluateText(s, story);
      ret = ret.replace(s, res);
    }
    return ret;
  }

  private String evaluateText(String str, Story story) {
    String s = str.replace(CBRACE_LEFT, "").replace(CBRACE_RIGHT, "");
    if (s.contains(":"))
      return evaluateConditionalText(s, story);
    if (s.startsWith("&"))
      return evaluateCycleText(s);
    if (s.startsWith("!"))
      return evaluateOnceOnlyText(s);
    if (s.startsWith("~"))
      return evaluateShuffleText(s);
    return evaluateSequenceText(s);
  }

  private String evaluateSequenceText(String str) {
    String[] tokens = str.split("[|]");
    int i = count < tokens.length ? count : tokens.length - 1;
    return tokens[i];
  }

  private static String evaluateShuffleText(String str) {
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    int i = new Random().nextInt(tokens.length);
    return tokens[i];
  }

  private String evaluateOnceOnlyText(String str) {
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    return count < tokens.length ? tokens[count] : "";
  }

  private String evaluateCycleText(String str) {
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    int i = count % tokens.length;
    return tokens[i];
  }

  private String evaluateConditionalText(String str, Story story) {
    // TODO: Not implemented yet
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    int i = count < tokens.length ? count : tokens.length - 1;
    return tokens[i];
  }

  public boolean isChoice() {
    return type == ContentType.CHOICE_ONCE || type == ContentType.CHOICE_REPEATABLE;
  }

  public boolean isDivert() {
    return text.contains(Story.DIVERT);
  }

  public int getCount() {
    return count;
  }

  public void increment() {
    count ++;
  }

}
