package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

class Gather extends Container {

  Gather(int l, String str, @Nullable Container current) throws InkParseException {
    lineNumber = l;
    type = ContentType.GATHER;
    level = 2;
    String s = str.substring(1).trim();
    while (s.startsWith("- ")) {
      level++;
      s = s.substring(1).trim();
    }
    if (current == null)
      throw new InkParseException("A gather must be nested within another knot, parent or choice/gather structure");
    parent = current.getContainer(level - 1);
    parent.add(this);
    addLine(s);
  }

  private void addLine(String str) {
    String s = str;
    if (s.startsWith("(")) {
      id = s.substring(s.indexOf(Symbol.BRACE_LEFT) + 1, s.indexOf(Symbol.BRACE_RIGHT)).trim();
      id = (parent != null ? parent.id : null) + InkParser.DOT + id;
      s = s.substring(s.indexOf(Symbol.BRACE_RIGHT) + 1).trim();

    }
    //noinspection ResultOfObjectAllocationIgnored
    new Content(lineNumber, s, this);
  }

  public static boolean isGatherHeader(String str) {
    if (str.length() < 2) return false;
    return str.startsWith("- ");
  }

}
