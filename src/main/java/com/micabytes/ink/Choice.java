package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Choice extends Container {
  private ArrayList<String> conditions;

  protected Choice(int l, String str, @Nullable Container current) throws InkParseException {
    lineNumber = l;
    char notation = str.charAt(0);
    type = notation == InkParser.CHOICE_DOT ? ContentType.CHOICE_ONCE : ContentType.CHOICE_REPEATABLE;
    level = 2;
    String s = str.substring(1).trim();
    while (s.charAt(0) == notation) {
      level++;
      s = s.substring(1).trim();
    }
    if (current == null)
      throw new InkParseException("A choice must be nested within another knot, parent or choice/gather structure");
    parent = current.getContainer(level - 1);
    parent.add(this);
    addLine(s);
  }

  public void addLine(String str) {
    String s = str;
    if (s.startsWith("(")) {
      id = s.substring(s.indexOf(BRACE_LEFT) + 1, s.indexOf(BRACE_RIGHT)).trim();
      Container p = parent;
      assert p != null;
      id = p.id + InkParser.DOT + id;
      s = s.substring(s.indexOf(BRACE_RIGHT) + 1).trim();
    }
    if (s.startsWith(CBRACE_LEFT) && conditions == null)
      conditions = new ArrayList<>();
    while (s.startsWith("{")) {
      String c = s.substring(s.indexOf(CBRACE_LEFT) + 1, s.indexOf(CBRACE_RIGHT)).trim();
      conditions.add(c);
      s = s.substring(s.indexOf(CBRACE_RIGHT) + 1).trim();
    }
    text = getChoiceText(s);
    String result = getResultText(s);
    if (!result.isEmpty()) {
      Content res = new Content(lineNumber, result);
      add(res);
    }
  }

  public static boolean isChoiceHeader(String str) {
    if (str.length() < 2) return false;
    return str.charAt(0) == InkParser.CHOICE_DOT || str.charAt(0) == InkParser.CHOICE_PLUS;
  }

  public String getChoiceText(Story story) throws InkRunTimeException {
    return getText(story);
  }

  private static String getChoiceText(String str) {
    if (str.contains("]")) {
      return str.substring(0, str.indexOf(SBRACE_RIGHT)).replace(SBRACE_LEFT, "").trim();
    }
    return str.trim();
  }

  private static String getResultText(String str) {
    if (str.contains("]")) {
      return str.replaceAll("\\[.*\\]", "").trim();
    }
    return str.trim();
  }

  public boolean evaluateConditions(Story story) throws InkRunTimeException {
    if (count > 0 && type == ContentType.CHOICE_ONCE)
      return false;
    if (conditions == null)
      return true;
    for (String condition : conditions) {
      if (Variable.evaluate(condition, story).intValue() <= 0)
        return false;
    }
    return true;
  }

}
