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
    // TODO: Optional Name
    text = "";
    addLine(s);
  }

  public void addLine(String str) {
    Content res = new Content(lineNumber, str);
    add(res);
  }

  public static boolean isGatherHeader(String str) {
    if (str.length() < 2) return false;
    if (str.startsWith(Story.DIVERT)) return false;
    return str.charAt(0) == InkParser.GATHER_DASH;
  }

}
