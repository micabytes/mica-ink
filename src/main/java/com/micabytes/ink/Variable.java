package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.util.regex.Pattern;

class Variable extends Content {
  @NonNls public static final String VAR = "VAR";
  public static final String VAR_ = "VAR ";

  Variable(int l, String str) {
    lineNumber = l;
    if (str.startsWith(VAR)) {
      type = ContentType.VARIABLE_DECLARATION;
      text = str.substring(4);
    }
  }

  public static boolean isVariableHeader(String str) {
    return str.startsWith(VAR_) || str.startsWith("~ ");
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
      throw new InkRunTimeException("Variable declaration contained more than one \'=\' token.");
    String variable = tokens[0].trim();
    String value = tokens[1].trim();
    if (value.equals("true"))
      story.putVariable(variable, Boolean.TRUE);
    else if (value.equals("false"))
      story.putVariable(variable, Boolean.FALSE);
    else if (isInteger(value))
      story.putVariable(variable, Integer.parseInt(value));
    else if (isFloat(value))
      story.putVariable(variable, Float.parseFloat(value));
    else if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length()-1);
      if (value.startsWith(Story.DIVERT)) {
        String address = value.substring(3).trim();
        Container directTo = story.getContainer(address);
        if (directTo != null)
          story.putVariable(value, directTo);
        else
          throw new InkRunTimeException("Variable " + variable + " declared to equals invalid address " + address);
      }
      else {
        story.putVariable(variable, value);
      }
    }

  }

  private void calculate(Story story) {
    // TODO: Not implemented yet
  }


  public static boolean isInteger(String str) {
    if (str == null) {
      return false;
    }
    int length = str.length();
    if (length == 0) {
      return false;
    }
    int i = 0;
    if (str.charAt(0) == '-') {
      if (length == 1) {
        return false;
      }
      i = 1;
    }
    for (; i < length; i++) {
      char c = str.charAt(i);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }

  private boolean isFloat(String str) {
    // Slow and dirty solution
    try {
      //noinspection ResultOfMethodCallIgnored
      Float.parseFloat(str);
    } catch (NumberFormatException ignored) {
      return false;
    }
    return true;
  }

}
