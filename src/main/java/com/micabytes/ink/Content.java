package com.micabytes.ink;

import java.math.BigDecimal;
import java.util.Random;

public class Content {
  static final String BRACE_RIGHT = ")";
  static final String BRACE_LEFT = "(";
  static final String CBRACE_RIGHT = "}";
  static final String CBRACE_LEFT = "{";
  static final String SBRACE_LEFT = "[";
  static final String SBRACE_RIGHT = "]";

  String id;
  int lineNumber;
  ContentType type = ContentType.TEXT;
  String text = "";
  int count;

  public Content() {
    // NOOP
  }

  public Content(int l, String str, Container current) {
    lineNumber = l;
    text = str;
    current.add(this);
  }

  public String getId() {
    return id;
  }

  void setId(String s) {
    id = s;
  }

  public String generateId(Container p) {
    int i = p.getContentIndex(this);
    id = p.getId() + InkParser.DOT + Integer.toString(i);
    return id;
  }

  public String getText(Story story) throws InkRunTimeException {
    String ret = text;
    while (ret.contains(CBRACE_LEFT)) {
      int start = ret.lastIndexOf(CBRACE_LEFT);
      int end = ret.indexOf(CBRACE_RIGHT, start);
      if (end < 0)
        throw new InkRunTimeException("Mismatched curly braces in line " + lineNumber);
      String s = ret.substring(start, end + 1);
      String res = evaluateText(s, story);
      ret = ret.replace(s, res);
    }
    return ret;
  }

  private String evaluateText(String str, Story story) throws InkRunTimeException {
    String s = str.replace(CBRACE_LEFT, "").replace(CBRACE_RIGHT, "");
    if (s.contains(":"))
      return evaluateConditionalText(s, story);
    if (s.startsWith("&"))
      return evaluateCycleText(s);
    if (s.startsWith("!"))
      return evaluateOnceOnlyText(s);
    if (s.startsWith("~"))
      return evaluateShuffleText(s);
    if (s.contains("|"))
      return evaluateSequenceText(s);
    return evaluteTextVariable(s, story);
  }

  private static String evaluteTextVariable(String s, Story story) {
    try {
      Object obj = Variable.evaluate(s, story);
      if (obj instanceof BigDecimal) // We don't want BigDecimal canonical form
        return ((BigDecimal) obj).toPlainString();
      return obj.toString();
    }
    catch (InkRunTimeException e) {
      story.errorLog.add(e.getMessage());
      return "ERROR";
    }
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
    if (str.startsWith("#")) {
      String condition = str.substring(1, str.indexOf(":")).trim();
      String text = str.substring(str.indexOf(":")+1);
      String[] options = text.split("[|]");
      int val = 0;
      try {
        Object value = Variable.evaluate(condition, story);
        if (value instanceof Boolean) {
          val = ((Boolean)value) ? 1 : 0;
        }
        else if (value instanceof BigDecimal) {
          val = ((BigDecimal) value).intValue();
        }
        else {
          val = value == null ?  0 : 1;
        }
      } catch (InkRunTimeException e) {
        // NOOP
      }
      if (val >= options.length)
        return options[options.length-1];
      if (val < 0)
        return options[0];
      return options[val];
    }
    // TODO: Not implemented yet
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    int i = count < tokens.length ? count : tokens.length - 1;
    return tokens[i];
  }

  public boolean isKnot() {
    return type == ContentType.KNOT;
  }

  public boolean isFunction() {
    return type == ContentType.FUNCTION;
  }

  public boolean isStitch() {
    return type == ContentType.STITCH;
  }

  public boolean isChoice() {
    return type == ContentType.CHOICE_ONCE || type == ContentType.CHOICE_REPEATABLE;
  }

  public boolean isFallbackChoice() {
    return isChoice() && text.isEmpty();
  }

  public boolean isGather() {
    return type == ContentType.GATHER;
  }

  public boolean isDivert() {
    return text.contains(Symbol.DIVERT) && !isVariable();
  }

  public boolean isConditional() {
    return type == ContentType.CONDITIONAL || type == ContentType.SEQUENCE_CYCLE || type == ContentType.SEQUENCE_ONCE || type == ContentType.SEQUENCE_SHUFFLE || type == ContentType.SEQUENCE_STOP;
  }

  public int getCount() {
    return count;
  }

  public void increment() {
    count ++;
  }

  public boolean isVariable() {
    return type == ContentType.VARIABLE_DECLARATION || type == ContentType.VARIABLE_EXPRESSION || type == ContentType.VARIABLE_RETURN;
  }

  public boolean isVariableReturn() {
    return type == ContentType.VARIABLE_RETURN;
  }

  public boolean isContainer() {
    return isKnot() || isFunction() || isStitch() || isChoice() || isGather();
  }

}
