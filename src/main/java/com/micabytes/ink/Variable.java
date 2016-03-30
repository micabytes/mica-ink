package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.regex.Pattern;

class Variable extends Content {
  @NonNls public static final String VAR_ = "VAR ";
  @NonNls public static final String TILDE_ = "~ ";
  @NonNls public static final String AND_WS = " and ";
  @NonNls public static final String OR_WS = " or ";
  @NonNls public static final String TRUE_LC = "true";
  @NonNls public static final String TRUE_UC = "TRUE";
  @NonNls public static final String FALSE_LC = "false";
  @NonNls public static final String FALSE_UC = "FALSE";

  Variable(int l, String str, Container parent) {
    lineNumber = l;
    if (str.startsWith(VAR_)) {
      type = ContentType.VARIABLE_DECLARATION;
      text = str.substring(4).trim();
    } else if (str.startsWith(TILDE_)) {
      type = ContentType.VARIABLE_EXPRESSION;
      text = str.substring(2).trim();
    }
    parent.add(this);
  }

  public static boolean isVariableHeader(String str) {
    return str.startsWith(VAR_) || str.startsWith(TILDE_);
  }

  public void evaluate(Story story) throws InkRunTimeException {
    if (type == ContentType.VARIABLE_DECLARATION)
      declareVariable(story);
    else
      calculate(story);
  }

  private static final Pattern EQ_SPLITTER = Pattern.compile("[=]+");

  private void declareVariable(Story story) throws InkRunTimeException {
    String[] tokens = EQ_SPLITTER.split(text);
    if (tokens.length != 2)
      throw new InkRunTimeException("Invalid variable declaration. Expected variables, values, and operators after \'=\'.");
    String variable = tokens[0].trim();
    String value = tokens[1].trim();
    if (value.equals(TRUE_LC))
      story.putVariable(variable, Boolean.TRUE);
    else if (value.equals(FALSE_LC))
      story.putVariable(variable, Boolean.FALSE);
    else if (isInteger(value))
      story.putVariable(variable, Integer.parseInt(value));
    else if (isFloat(value))
      story.putVariable(variable, Float.parseFloat(value));
    else if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
      if (value.contains(Story.DIVERT))
        throw new InkRunTimeException("Line number" + lineNumber + ": String expressions cannot contain diverts (->)");
      story.putVariable(variable, value);
    } else if (value.startsWith(Story.DIVERT)) {
      String address = value.substring(2).trim();
      Container directTo = story.getContainer(address);
      if (directTo != null)
        story.putVariable(variable, directTo);
      else
        throw new InkRunTimeException("Variable " + variable + " declared to equals invalid address " + address);
    }
  }

  private void calculate(Story story) throws InkRunTimeException {
    String[] tokens = EQ_SPLITTER.split(text);
    if (tokens.length != 2)
      throw new InkRunTimeException("Invalid variable expression. Expected variables, values, and operators after \'=\'.");
    String variable = tokens[0].trim();
    String value = tokens[1].trim();
    if (!story.hasVariable(variable))
      throw new InkRunTimeException("Variable " + variable + " is not defined in variable expression on line " + lineNumber);
    if (value.equals(TRUE_LC))
      story.putVariable(variable, Boolean.TRUE);
    else if (value.equals(FALSE_LC))
      story.putVariable(variable, Boolean.FALSE);
    else if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
      story.putVariable(variable, value);
    } else if (value.startsWith(Story.DIVERT)) {
      String address = value.substring(3).trim();
      Container directTo = story.getContainer(address);
      if (directTo != null)
        story.putVariable(variable, directTo);
      else
        throw new InkRunTimeException("Variable " + variable + " declared to equals invalid address " + address);
    } else {
      BigDecimal val = evaluate(value, story);
      story.putVariable(variable, val);
    }
  }

  public static BigDecimal evaluate(String str, Story story) throws InkRunTimeException {
    // TODO: Note that this means that spacing will mess up expressions; needs to be fixed
    String ev = str.replaceAll(AND_WS, " && ").replaceAll(OR_WS, " || ").replaceAll(TRUE_LC, TRUE_UC).replaceAll(FALSE_LC, FALSE_UC);
    Expression ex = new Expression(ev);
    Iterator<String> tokens = ex.getExpressionTokenizer();
    while (tokens.hasNext()) {
      String s = tokens.next();
      if (Character.isAlphabetic(s.charAt(0)) && !isKeyword(s)) {
        ex = ex.with(s, new BigDecimal(story.getValue(s).toString()));
      }
    }
    return ex.eval();
  }

  public static boolean isInteger(String str) {
    // Slow and dirty solution
    try {
      //noinspection ResultOfMethodCallIgnored
      Integer.parseInt(str);
    } catch (NumberFormatException ignored) {
      return false;
    }
    return true;
  }

  private static boolean isFloat(String str) {
    // Slow and dirty solution
    try {
      //noinspection ResultOfMethodCallIgnored
      Float.parseFloat(str);
    } catch (NumberFormatException ignored) {
      return false;
    }
    return true;
  }

  private static boolean isKeyword(@NonNls String s) {
    return "not".equalsIgnoreCase(s) || s.equalsIgnoreCase(TRUE_UC) || s.equalsIgnoreCase(FALSE_UC);
  }

}
