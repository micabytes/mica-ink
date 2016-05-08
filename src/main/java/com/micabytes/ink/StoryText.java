package com.micabytes.ink;

import java.math.BigDecimal;
import java.util.Random;

@SuppressWarnings("UtilityClass")
public final class StoryText {
  static final String BRACE_RIGHT = ")";
  static final String BRACE_LEFT = "(";
  static final String CBRACE_RIGHT = "}";
  static final String CBRACE_LEFT = "{";
  static final String SBRACE_LEFT = "[";
  static final String SBRACE_RIGHT = "]";

  private StoryText() throws AssertionError {
    throw new AssertionError("StoryText should never be initialized");
  }

  public static String getText(String text, int count, VariableMap variables) {
    String ret = text;
    while (ret.contains(CBRACE_LEFT)) {
      int start = ret.lastIndexOf(CBRACE_LEFT);
      int end = ret.indexOf(CBRACE_RIGHT, start);
      if (end < 0) {
        variables.logException(new InkRunTimeException("Mismatched curly braces in text."));
        return ret;
      }
      String s = ret.substring(start, end + 1);
      String res = evaluateText(s, count, variables);
      ret = ret.replace(s, res);
    }
    return ret;
  }

  private static String evaluateText(String str, int count, VariableMap variables) {
    String s = str.replace(CBRACE_LEFT, "").replace(CBRACE_RIGHT, "");
    if (s.contains(":"))
      return evaluateConditionalText(s, count, variables);
    if (s.startsWith("&"))
      return evaluateCycleText(s, count);
    if (s.startsWith("!"))
      return evaluateOnceOnlyText(s, count);
    if (s.startsWith("~"))
      return evaluateShuffleText(s, count);
    if (s.contains("|"))
      return evaluateSequenceText(s, count);
    return evaluteTextVariable(s, variables);
  }

  private static String evaluteTextVariable(String s, VariableMap variables) {
    try {
      Object obj = Variable.evaluate(s, variables);
      if (obj instanceof BigDecimal) // We don't want BigDecimal canonical form
        return ((BigDecimal) obj).toPlainString();
      return obj.toString();
    }
    catch (InkRunTimeException e) {
      variables.logException(e);
      return "(ERROR:" + s + ")";
    }
  }

  private static String evaluateSequenceText(String str, int count) {
    String[] tokens = str.split("[|]");
    int i = count < tokens.length ? count : tokens.length - 1;
    return tokens[i];
  }

  private static String evaluateShuffleText(String str, int count) {
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    int i = new Random().nextInt(tokens.length);
    return tokens[i];
  }

  private static String evaluateOnceOnlyText(String str, int count) {
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    return count < tokens.length ? tokens[count] : "";
  }

  private static String evaluateCycleText(String str, int count) {
    String s = str.substring(1);
    String[] tokens = s.split("[|]");
    int i = count % tokens.length;
    return tokens[i];
  }

  private static String evaluateConditionalText(String str, int count, VariableMap variables) {
    if (str.startsWith("#")) {
      String condition = str.substring(1, str.indexOf(":")).trim();
      String text = str.substring(str.indexOf(":")+1);
      String[] options = text.split("[|]");
      int val = 0;
      try {
        Object value = Variable.evaluate(condition, variables);
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
        variables.logException(e);
        // TODO: Change?
      }
      if (val >= options.length)
        return options[options.length-1];
      if (val < 0)
        return options[0];
      return options[val];
    }
    // Regular conditional
    String condition = str.substring(0, str.indexOf(":")).trim();
    String text = str.substring(str.indexOf(":")+1);
    String[] options = text.split("[|]");
    if (options.length > 2)
      variables.logException(new InkRunTimeException("Too many options in a conditional text."));
    String ifText = options[0];
    String elseText = options.length == 1 ? "" : options[1];
    try {
      Object obj = Variable.evaluate(condition, variables);
      if (obj instanceof BigDecimal)
        return (((Number) obj).intValue() > 0) ? ifText : elseText;
      if (obj instanceof Boolean)
        return ((Boolean) obj) ? ifText : elseText;
      variables.logException(new InkRunTimeException("Condition in conditional text did not resolve into a number or boolean."));
      return elseText;
    }
    catch (InkRunTimeException e) {
      variables.logException(e);
      return elseText;
    }
  }

}
