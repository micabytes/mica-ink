package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

public class Gather extends Container {

  protected Gather(int l, String str, @Nullable Container current) throws InkParseException {
    lineNumber = l;
    type = ContentType.GATHER;
    level = 2;
    String s = str.substring(1).trim();
    while (s.charAt(0) == InkParser.GATHER_DASH) {
      level++;
      s = s.substring(1).trim();
    }
    if (current == null)
      throw new InkParseException("A gather must be nested within another knot, parent or choice/gather structure");
    parent = current.getContainer(level - 1);
    parent.add(this);
    addLine(s);
  }

  public void addLine(String str) {
    String s = str;
    if (s.startsWith("(")) {
      id = s.substring(s.indexOf("(") + 1, s.indexOf(")")).trim();
      Container p = parent;
      id = p.id + InkParser.DOT + id;
      s = s.substring(s.indexOf(")") + 1).trim();
    }
    Content res = new Content(lineNumber, s);
    add(res);
  }

  public static boolean isGatherHeader(String str) {
    if (str.length() < 2) return false;
    if (str.startsWith(Story.DIVERT)) return false;
    return str.charAt(0) == InkParser.GATHER_DASH;
  }

}
