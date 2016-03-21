package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

// TODO: Not actually implemented yet
class Gather extends Container {

  public Gather(int l, String str, @Nullable Container current) throws InkParseException {
    lineNumber = l;
    level = 1;
    String s = str.substring(1).trim();
    while (s.charAt(0) == InkParser.GATHER_DASH) {
      level ++;
      s = s.substring(1).trim();
    }
    if (current == null)
      throw new InkParseException("A gather must be nested within another knot, parent or choice/gather structure");
    parent = current.getContainer(level);
    text = s;
  }

  public static boolean isGatherHeader(String str) {
    if (str.length() < 2) return false;
    if (str.startsWith(Story.DIVERT)) return false;
    return str.charAt(0) == InkParser.GATHER_DASH;
  }

}
