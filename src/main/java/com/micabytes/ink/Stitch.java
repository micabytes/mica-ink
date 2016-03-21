package com.micabytes.ink;

import org.jetbrains.annotations.Nullable;

class Stitch extends Container {

  public Stitch(int l, String str, @Nullable Container current) throws InkParseException {
    lineNumber = l;
    level = 1;
    if (current == null)
      throw new InkParseException("A stitch cannot be defined without a parent Knot");
    parent = current.getContainer(0);
    parent.add(this);
    type = ContentType.STITCH;
    id = new StringBuilder(parent.id)
        .append(InkParser.DOT)
        .append(str.replaceAll(String.valueOf(InkParser.HEADER), "").trim())
        .toString();
  }

  public static boolean isStitchHeader(String str) {
    if (str.length() < 2) return false;
    if (str.charAt(0) == InkParser.HEADER && str.charAt(1) != InkParser.HEADER)
      return true;
    return false;
  }

}
