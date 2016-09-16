package com.micabytes.ink;

import java.math.BigDecimal;
import java.util.ArrayList;

class Comment extends Content {
  private ArrayList<String> conditions = null;

  Comment(int l, String str) {
    lineNumber = l;
    char notation = str.charAt(0);
    type = notation == InkParser.CHOICE_DOT ? ContentType.COMMENT_ONCE : ContentType.COMMENT_REPEATABLE;
    String s = str.substring(1).trim();
    addLine(s);
  }

  private void addLine(String str) {
    String s = str.trim();
    if (s.startsWith(StoryText.CBRACE_LEFT) && conditions == null)
      conditions = new ArrayList<>();
    while (s.startsWith("{")) {
      String c = s.substring(s.indexOf(StoryText.CBRACE_LEFT) + 1, s.indexOf(StoryText.CBRACE_RIGHT)).trim();
      conditions.add(c);
      s = s.substring(s.indexOf(StoryText.CBRACE_RIGHT) + 1).trim();
    }
    text = s;
  }

  public boolean evaluateConditions(Story story) throws InkRunTimeException {
    if (conditions == null)
      return true;
    for (String condition : conditions) {
      Object obj = Variable.evaluate(condition, story);
      if (obj == null)
        return false;
      if (obj instanceof Boolean && !(Boolean) obj)
        return false;
      if (obj instanceof BigDecimal && ((BigDecimal)obj).intValue() <= 0)
        return false;
    }
    return true;
  }

  public String getCommentText(Story story) throws InkRunTimeException {
    return StoryText.getText(text, count, story);
  }

}